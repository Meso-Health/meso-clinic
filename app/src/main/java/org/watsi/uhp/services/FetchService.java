package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NotificationManager;
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
 * Service class that continuously polls the UHP API
 * to refresh the locally-stored member data
 */
public class FetchService extends Service {

    private static int SLEEP_TIME = 10 * 60 * 1000; // 10 minutes
    private static int WAIT_FOR_LOGIN_SLEEP_TIME = 60 * 1000; // 1 minute
    private int mProviderId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper.init(getApplicationContext());
        mProviderId = BuildConfig.PROVIDER_ID;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if (ConfigManager.getLoggedInUserToken(getApplicationContext()) == null) {
                        try {
                            Thread.sleep(WAIT_FOR_LOGIN_SLEEP_TIME);
                            continue;
                        } catch (InterruptedException e) {
                            Rollbar.reportException(e);
                        }
                    }

                    try {
                        fetchNewMemberData();
                        fetchBillables();
                    } catch (IOException | SQLException | IllegalStateException e) {
                        Rollbar.reportException(e);
                    }
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Rollbar.reportException(e);
                    }

                }
            }
        }).start();
        return Service.START_REDELIVER_INTENT;
    }

    private void fetchNewMemberData() throws IOException, SQLException, IllegalStateException {
        String lastModifiedTimestamp = ConfigManager.getMemberLastModified(getApplicationContext());
        Call<List<Member>> request = ApiService.requestBuilder(getApplicationContext())
                .members(lastModifiedTimestamp, mProviderId);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            Log.d("UHP", "updating member data");
            List<Member> members = response.body();
            notifyAboutMembersToBeDeleted(members);
            createOrUpdateMembers(members);
            ConfigManager.setMemberLastModified(
                    response.headers().get("last-modified"),
                    getApplicationContext()
            );
        } else {
            if (response.code() != 304) {
                NotificationManager.requestFailure(
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
     * @param members
     * @throws SQLException
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
            Rollbar.reportMessage("Member synced on device but not in backend", "warning", params);
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
                Rollbar.reportException(e);
            }

            iterator.remove();
        }
    }

    private void fetchBillables() throws IOException, SQLException {
        String lastModifiedTimestamp = ConfigManager.getBillablesLastModified(getApplicationContext());
        Call<List<Billable>> request = ApiService.requestBuilder(getApplicationContext())
                .billables(lastModifiedTimestamp, mProviderId);
        Response<List<Billable>> response = request.execute();
        if (response.isSuccessful()) {
            Log.d("UHP", "updating billables data");
            List<Billable> billables = response.body();
            BillableDao.clear();
            BillableDao.create(billables);
            ConfigManager.setBillablesLastModified(
                    response.headers().get("last-modified"),
                    getApplicationContext()
            );
        } else {
            if (response.code() != 304) {
                NotificationManager.requestFailure(
                        "Failed to fetch billables",
                        request.request(),
                        response.raw()
                );
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
