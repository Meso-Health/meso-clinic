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

public class OfflineNotificationService extends Service {

    private static int SLEEP_TIME = 5000;
    private RequestQueue mQueue = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        Rollbar.reportException(e);
                    }
                    EventBus.getDefault().post(new OfflineNotificationEvent(!pingService()));
                }
            }
        }).start();
        return Service.START_STICKY;
    }

    public boolean pingService() {
        RequestFuture<String> future = RequestFuture.newFuture();
        // TODO: replace this with a ping request to the UHP back-end service
        String url = "https://watsi.org";
        final StringRequest request = new StringRequest(url, future, future);
        getRequestQueue().add(request);
        try {
            future.get(10, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException e) {
            Log.d("UHP", e.getMessage());
        } catch (ExecutionException e) {
            Log.d("UHP", e.getMessage());
        } catch (TimeoutException e) {
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
