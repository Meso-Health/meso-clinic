package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.greenrobot.eventbus.EventBus;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Queue;
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
        Log.d("UHP", "fetching new member data");
        int facilityId = ConfigManager.getFacilityId(getApplicationContext());
        Call<List<Member>> request = ApiService.requestBuilder(getApplicationContext())
                .members(MemberDao.lastModifiedString(), facilityId);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            EventBus.getDefault().post(new OfflineNotificationEvent(false));
            final List<Member> members = response.body();
            MemberDao.clear();
            MemberDao.create(members);
            MemberDao.setLastModifiedAt(response.headers().get("last-modified"));
        } else {
            if (response.code() == 304) {
                EventBus.getDefault().post(new OfflineNotificationEvent(false));
            } else {
                EventBus.getDefault().post(new OfflineNotificationEvent(true));
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
