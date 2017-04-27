package org.watsi.uhp.services;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Service class that polls the UHP API and updates the device with updated member and billables data
 */
public class FetchService extends AbstractSyncJobService {

    private PreferencesManager mPreferencesManager;

    @Override
    public boolean performSync() {
        mPreferencesManager = new PreferencesManager(this);

        try {
            fetchNewMemberData();
            fetchBillables();
            return true;
        } catch (IOException | SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    private void fetchNewMemberData() throws IOException, SQLException, IllegalStateException {
        String lastModifiedTimestamp = mPreferencesManager.getMemberLastModified();
        Call<List<Member>> request = ApiService.requestBuilder(getApplicationContext())
                .members(lastModifiedTimestamp, BuildConfig.PROVIDER_ID);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            List<Member> members = response.body();
            notifyAboutMembersToBeDeleted(members);
            createOrUpdateMembers(members);
            mPreferencesManager.setMemberLastModified(response.headers().get("last-modified"));
        } else {
            if (response.code() != 304) {
                ExceptionManager.requestFailure(
                        "Failed to fetch members",
                        request.request(),
                        response.raw()
                );
            }
        }
    }

    /**
     * This reports to Rollbar the IDs of any members who are marked as synced
     * locally on the device, but are not in the list of members returned by
     * the server.
     *
     * These members should be safe to delete, but for now we are choosing
     * the safer route of first creating notifications of their existence
     * @param members Most recent list of members returned by server
     * @throws SQLException Error querying data from the db
     */
    private void notifyAboutMembersToBeDeleted(List<Member> members) throws SQLException {
        Set<UUID> previousMemberIds = MemberDao.allMemberIds();
        for (Member member : members) {
            previousMemberIds.remove(member.getId());
        }
        Set<UUID> unsyncedPrevMembers = new HashSet<>();
        for (UUID prevMemberId : previousMemberIds) {
            Member member = MemberDao.findById(prevMemberId);
            if (!member.isSynced()) unsyncedPrevMembers.add(prevMemberId);
        }
        previousMemberIds.removeAll(unsyncedPrevMembers);
        for (UUID toBeDeleted : previousMemberIds) {
            Map<String, String> params = new HashMap<>();
            params.put("member.id", toBeDeleted.toString());
            ExceptionManager.reportMessage("Member synced on device but not in backend", "warning",
                    params);
        }
    }

    private void createOrUpdateMembers(List<Member> members) throws SQLException {
        Iterator<Member> iterator = members.iterator();
        while (iterator.hasNext()) {
            Member member = iterator.next();

            Member persistedMember = MemberDao.findById(member.getId());
            if (persistedMember != null) {
                // if the persisted member has not been synced to the back-end, assume it is
                // the most up-to-date and do not update it with the fetched member attributes
                if (!persistedMember.isSynced()) {
                    iterator.remove();
                    continue;
                }

                // if the existing member record has a photo and the fetched member record has
                // the same photo url as the existing record, copy the photo to the new record
                // so we do not have to re-download it
                if (persistedMember.getPhoto() != null && persistedMember.getPhotoUrl() != null &&
                        persistedMember.getPhotoUrl().equals(member.getPhotoUrl())) {
                    member.setPhoto(persistedMember.getPhoto());
                }
            }

            try {
                member.setSynced();
                MemberDao.createOrUpdate(member);
            } catch (AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }

            iterator.remove();
        }
    }

    private void fetchBillables() throws IOException, SQLException {
        String lastModifiedTimestamp = mPreferencesManager.getBillablesLastModified();
        Call<List<Billable>> request = ApiService.requestBuilder(getApplicationContext())
                .billables(lastModifiedTimestamp, BuildConfig.PROVIDER_ID);
        Response<List<Billable>> response = request.execute();
        if (response.isSuccessful()) {
            List<Billable> billables = response.body();
            BillableDao.clear();
            BillableDao.create(billables);
            mPreferencesManager.setBillablesLastModified(response.headers().get("last-modified"));
        } else {
            if (response.code() != 304) {
                ExceptionManager.requestFailure(
                        "Failed to fetch billables",
                        request.request(),
                        response.raw()
                );
            }
        }
    }
}
