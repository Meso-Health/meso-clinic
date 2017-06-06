package org.watsi.uhp.services;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.util.Log;

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

    private static String LAST_MODIFIED_HEADER = "last-modified";

    @Override
    public boolean performSync() {
        Log.i("UHP", "FetchService.performSync is called.");
        PreferencesManager preferencesManager = new PreferencesManager(this);
        try {
            String authenticationToken = getAuthenticationToken(preferencesManager);
            if (authenticationToken != null) {
                fetchMembers(authenticationToken, preferencesManager);
                fetchBillables(authenticationToken, preferencesManager);
            }
            return true;
        } catch (IOException | SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    protected String getAuthenticationToken(PreferencesManager preferencesManager) {
        SessionManager sessionManager = new SessionManager(
                preferencesManager, AccountManager.get(this));
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
                tokenHeader, preferencesManager.getMemberLastModified(), BuildConfig.PROVIDER_ID);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            List<Member> members = response.body();
            notifyAboutMembersToBeDeleted(members);
            createOrUpdateMembers(members);
            preferencesManager.setMemberLastModified(response.headers().get(LAST_MODIFIED_HEADER));
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
     * @param fetchedMembers Most recent list of members returned by server
     * @throws SQLException Error querying data from the db
     */
    protected void notifyAboutMembersToBeDeleted(List<Member> fetchedMembers) throws SQLException {
        Set<UUID> previousMemberIds = MemberDao.allMemberIds();
        for (Member member : fetchedMembers) {
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
            ExceptionManager.reportMessage(
                    "Member synced on device but not in backend",
                    ExceptionManager.MESSAGE_LEVEL_WARNING, params);
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
        String lastModifiedTimestamp = preferencesManager.getBillablesLastModified();
        Call<List<Billable>> request = ApiService.requestBuilder(this)
                .billables(tokenHeader, lastModifiedTimestamp, BuildConfig.PROVIDER_ID);
        Response<List<Billable>> response = request.execute();
        if (response.isSuccessful()) {
            List<Billable> billables = response.body();
            BillableDao.clear();
            BillableDao.create(billables);
            preferencesManager.setBillablesLastModified(
                    response.headers().get(LAST_MODIFIED_HEADER));
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
