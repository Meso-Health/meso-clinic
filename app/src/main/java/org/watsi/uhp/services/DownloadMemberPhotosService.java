package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ReportManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Service class to handle downloading member photos
 */
public class DownloadMemberPhotosService extends Service {

    private static int SLEEP_TIME = 5 * 60 * 1000; // 5 minutes
    private static int MAX_FETCH_FAILURE_ATTEMPTS = 5;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper.init(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        fetchMemberPhotos();
                    } catch (SQLException | IllegalStateException e) {
                        ReportManager.handleException(e);
                    }
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        ReportManager.handleException(e);
                    }

                }
            }
        }).start();
        return Service.START_REDELIVER_INTENT;
    }

    private void fetchMemberPhotos() throws SQLException {
        List<Member> membersWithPhotosToFetch = MemberDao.membersWithPhotosToFetch();
        Iterator<Member> iterator = membersWithPhotosToFetch.iterator();
        int photosToFetch = membersWithPhotosToFetch.size();
        int fetchFailures = 0;
        while (iterator.hasNext()) {
            Log.d("UHP", "members with photos to fetch: " + photosToFetch);

            Member member = iterator.next();
            try {
                if (!FileManager.isLocal(member.getPhotoUrl())) {
                    member.fetchAndSetPhotoFromUrl();
                    MemberDao.update(member);
                }
            } catch (IOException | SQLException e) {
                ReportManager.handleException(e);
                fetchFailures++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }

            iterator.remove();
            photosToFetch--;
            if (fetchFailures == MAX_FETCH_FAILURE_ATTEMPTS) {
                return;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
