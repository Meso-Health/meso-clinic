package org.watsi.uhp.services;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
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

    @Override
    public boolean performSync() {
        PreferencesManager preferencesManager = fetchPreferencesManager();
        try {
            String authenticationToken = getAuthenticationToken(preferencesManager);
            if (authenticationToken != null) {
                fetchMembers(authenticationToken, preferencesManager);
                fetchBillables(authenticationToken, preferencesManager);
            }
            return true;
        } catch (SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        } catch (IOException e) {
            ExceptionManager.reportExceptionWarning(e);
            return false;
        }
    }

    protected PreferencesManager fetchPreferencesManager() {
        return new PreferencesManager(this);
    }

    protected SessionManager fetchSessionManager(PreferencesManager preferencesManager) {
        return new SessionManager(preferencesManager, AccountManager.get(this));
    }

    protected String getAuthenticationToken(PreferencesManager preferencesManager) {
        SessionManager sessionManager = fetchSessionManager(preferencesManager);
        AccountManagerFuture<Bundle> tokenFuture = sessionManager.fetchToken();
        try {
            if (tokenFuture != null) {
                Bundle tokenBundle = tokenFuture.getResult();
                return tokenBundle.getString(AccountManager.KEY_AUTHTOKEN);
            }
        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
            ExceptionManager.reportException(e);
        }
        return null;
    }

    protected void fetchMembers(String authToken, PreferencesManager preferencesManager)
            throws IOException, SQLException, IllegalStateException {
        String tokenHeader = "Token " + authToken;
        Call<List<Member>> request = ApiService.requestBuilder(this).members(
                tokenHeader, BuildConfig.PROVIDER_ID);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            List<Member> fetchedMembers = response.body();
            deleteMembersDueToProviderAssignmentEnding(fetchedMembers);
            createOrUpdateMembers(fetchedMembers);
            preferencesManager.updateMembersLastModified();
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
     * This deletes the members that are still on the device, but are not contained in the list
     * returned from backend.
     *
     * @param fetchedMembers Most recent list of members returned by server
     * @throws SQLException Error querying data from the db
     */
    protected void deleteMembersDueToProviderAssignmentEnding(List<Member> fetchedMembers) throws SQLException {
        Set<UUID> previousMemberIds = MemberDao.allMemberIds();
        Set<UUID> fetchedMemberIds = new HashSet<>();
        for (Member member: fetchedMembers) {
            fetchedMemberIds.add(member.getId());
        }

        // Should only leave unsynced newborns and members whose ProviderAssignment ended - so in
        // the next loop, we only delete the ones whose assignment ended by checking if they are synced
        previousMemberIds.removeAll(fetchedMemberIds);

        for (UUID prevMemberId : previousMemberIds) {
            Member memberToDelete = Member.find(prevMemberId, Member.class);
            if (memberToDelete.isSynced()) {
                Map<String, String> params = new HashMap<>();
                params.put("member.id", memberToDelete.toString());
                ExceptionManager.reportMessage(
                        "Member deleted due to provider assignment ending.",
                        ExceptionManager.MESSAGE_LEVEL_INFO, params);
                memberToDelete.destroy();
            }
        }
    }

    protected void createOrUpdateMembers(List<Member> fetchedMembers) throws SQLException {
        Iterator<Member> iterator = fetchedMembers.iterator();
        while (iterator.hasNext()) {
            Member fetchedMember = iterator.next();
            fetchedMember.updateFromFetch();
            iterator.remove();
        }
    }

    protected void fetchBillables(String authToken, PreferencesManager preferencesManager)
            throws IOException, SQLException {
        String tokenHeader = "Token " + authToken;
        Call<List<Billable>> request = ApiService.requestBuilder(this)
                .billables(tokenHeader, BuildConfig.PROVIDER_ID);
        Response<List<Billable>> response = request.execute();
        if (response.isSuccessful()) {
            List<Billable> billables = response.body();
            BillableDao.clearBillablesWithoutUnsyncedEncounter();
            BillableDao.createOrUpdate(billables);
            preferencesManager.updateBillableLastModified();
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