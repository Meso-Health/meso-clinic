package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Service class to handle downloading member photos
 */
public class FetchMemberPhotosService extends Service {

    private static int SLEEP_TIME = 5 * 60 * 1000; // 5 minutes

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

    private void fetchMemberPhotos() throws SQLException {
        List<Member> membersWithPhotosToFetch = MemberDao.membersWithPhotosToFetch();
        Iterator<Member> iterator = membersWithPhotosToFetch.iterator();
        int photosToFetch = membersWithPhotosToFetch.size();
        while (iterator.hasNext()) {
            Log.d("UHP", "members with photos to fetch: " + photosToFetch);

            Member member = iterator.next();
            try {
                member.fetchAndSetPhotoFromUrl();
                MemberDao.update(member);
            } catch (IOException | SQLException e) {
                Rollbar.reportException(e);
            }

            iterator.remove();
            photosToFetch--;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
