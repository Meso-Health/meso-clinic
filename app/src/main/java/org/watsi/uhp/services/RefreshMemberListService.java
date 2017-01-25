package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.greenrobot.eventbus.EventBus;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service class that continuously polls the UHP API
 * to refresh the locally-stored member data
 */
public class RefreshMemberListService extends Service {

    private static int SLEEP_TIME = 10000;
    private UhpApi mUhpApi;
    private Integer mFacilityId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFacilityId = intent.getExtras().getInt("facilityId");
        String apiHost = intent.getExtras().getString("apiHost");
        if (apiHost == null) {
            Log.w("UHP", "no api host provided, will not fetch data");
        } else {
            mUhpApi = new Retrofit.Builder()
                    .baseUrl(intent.getExtras().getString("apiHost"))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(UhpApi.class);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                            Thread.sleep(SLEEP_TIME);
                            fetchNewMemberData();
                        } catch (IOException | SQLException | InterruptedException e) {
                            Rollbar.reportException(e);
                        }
                    }
                }
            }).start();
        }
        return Service.START_REDELIVER_INTENT;
    }

    private void fetchNewMemberData() throws IOException, SQLException {
        Call<List<Member>> request = mUhpApi.members(MemberDao.lastModifiedString(), mFacilityId);
        Response<List<Member>> response = request.execute();
        if (response.isSuccessful()) {
            EventBus.getDefault().post(new OfflineNotificationEvent(false));
            MemberDao.setLastModifiedAt(response.headers().get("last-modified"));
            List<Member> members = response.body();
            MemberDao.clear();
            MemberDao.create(members);
        } else {
            if (response.code() == 304) {
                EventBus.getDefault().post(new OfflineNotificationEvent(false));
            } else {
                EventBus.getDefault().post(new OfflineNotificationEvent(true));
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
