package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Service class that continuously polls the UHP API
 * to refresh the locally-stored member data
 */
public class RefreshMemberListService extends Service {

    private static int SLEEP_TIME = 10 * 60 * 1000; // 10 minutes

    private final Queue<Member> fetchPhotoQueue = new LinkedBlockingDeque<>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper.init(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        fetchNewMemberData();
                        fetchMemberPhotos();
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
        int facilityId = ConfigManager.getFacilityId(getApplicationContext());
        String lastModifiedTimestamp = ConfigManager.getMemberLastModified(getApplicationContext());
        Call<List<Member>> request = ApiService.requestBuilder(getApplicationContext())
                .members(lastModifiedTimestamp, facilityId);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            Log.d("UHP", "updating member data");
            List<Member> members = response.body();
            MemberDao.clear();
            copyUnchangedMemberPhotos(members, previousMembersById());
            MemberDao.create(members);
            ConfigManager.setMemberLastModified(
                    response.headers().get("last-modified"),
                    getApplicationContext()
            );
        } else {
            if (response.code() != 304) {
                // TODO: request failed
            }
        }
    }

    private void fetchMemberPhotos() throws SQLException {
        for (Member member : MemberDao.membersWithPhotosToFetch()) {
            if (!fetchPhotoQueue.contains(member)) {
                fetchPhotoQueue.add(member);
            }
        }
        Log.d("UHP", "members with photos to fetch: " + fetchPhotoQueue.size());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Member member = fetchPhotoQueue.poll();
                while (member != null) {
                    try {
                        member.fetchAndSetPhotoFromUrl();
                        MemberDao.update(member);
                    } catch (IOException | SQLException e) {
                        Rollbar.reportException(e);
                    }
                    Log.d("UHP", "photos left to fetch: " + fetchPhotoQueue.size());
                    member = fetchPhotoQueue.poll();
                }
            }
        }).start();
    }

    private Map<UUID, Member> previousMembersById() throws SQLException {
        List<Member> currentMembers = MemberDao.all();
        Map<UUID, Member> memberByIdMap = new HashMap<>();
        for (Member member : currentMembers) {
            memberByIdMap.put(member.getId(), member);
        }
        return memberByIdMap;
    }

    private void copyUnchangedMemberPhotos(List<Member> newMemberData, Map<UUID, Member> oldMembers) {
        for (Member member : newMemberData) {
            Member prevMember = oldMembers.get(member.getId());
            if (prevMember != null) {
                if (prevMember.getPhoto() != null &&
                        prevMember.getPhotoUrl() == member.getPhotoUrl()) {
                    member.setPhoto(prevMember.getPhoto());
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
