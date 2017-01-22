package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.rollbar.android.Rollbar;

import org.greenrobot.eventbus.EventBus;
import org.watsi.uhp.events.OfflineNotificationEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service class that starts upon application start-up that
 * polls the UHP API to determine if the device is online
 */
public class OfflineNotificationService extends Service {

    private static int SLEEP_TIME = 5000;
    private RequestQueue mQueue = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String apiHost = intent.getExtras().getString("apiHost");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Rollbar.reportException(e);
                    }
                    EventBus.getDefault().post(new OfflineNotificationEvent(!pingService(apiHost)));
                }
            }
        }).start();
        return Service.START_REDELIVER_INTENT;
    }

    public boolean pingService(String apiHost) {
        RequestFuture<String> future = RequestFuture.newFuture();
        String url = apiHost + "status";
        final StringRequest request = new StringRequest(url, future, future);
        getRequestQueue().add(request);
        try {
            future.get(10, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e.getMessage() != null) {
                Log.d("UHP", e.getMessage());
            }
        }
        return false;
    }

    public RequestQueue getRequestQueue() {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getBaseContext());
            mQueue.start();
        }
        return mQueue;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
