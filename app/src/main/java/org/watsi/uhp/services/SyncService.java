package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SyncService extends Service {

    private static int SLEEP_TIME = 10 * 60 * 1000; // 10 minutes
    private int mProviderId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper.init(getApplicationContext());
        mProviderId = ConfigManager.getProviderId(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        List<IdentificationEvent> events = IdentificationEventDao.unsynced();
                        if (events.size() > 0) {
                            syncIdentificationEvents(events);
                        }
                        List<Encounter> encounters = EncounterDao.unsynced();
                        if (encounters.size() > 0) {
                            syncEncounters(encounters);
                        }
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

    private void syncIdentificationEvents(List<IdentificationEvent> unsyncedEvents) throws SQLException, IOException {
        int providerId = ConfigManager.getProviderId(getApplicationContext());
        for (IdentificationEvent event : unsyncedEvents) {
            event.setMemberId(event.getMember().getId());
            String tokenAuthorizationString = "Token " + event.getToken();
            Call<IdentificationEvent> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncIdentificationEvent(tokenAuthorizationString, providerId, event);
            Response<IdentificationEvent> response = request.execute();
            if (response.isSuccessful()) {
                event.setSynced(true);
                IdentificationEventDao.update(event);
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("identification_event_id", event.getId().toString());
                reportParams.put("member_id", event.getMember().getId().toString());
                Rollbar.reportMessage("Failed to sync identification", "warning", reportParams);
            }
        }
    }

    private void syncEncounters(List<Encounter> unsyncedEncounters) throws SQLException, IOException {
        for (Encounter encounter : unsyncedEncounters) {
            encounter.setMemberId(encounter.getMember().getId());
            encounter.setIdentificationEventId(encounter.getIdentificationEvent().getId());
            encounter.setEncounterItems(EncounterItemDao.fromEncounter(encounter));
            String tokenAuthorizationString = "Token " + encounter.getToken();
            Call<Encounter> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncEncounter(tokenAuthorizationString, mProviderId, encounter);
            Response<Encounter> response = request.execute();
            if (response.isSuccessful()) {
                encounter.setSynced(true);
                EncounterDao.update(encounter);
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("encounter_id", encounter.getId().toString());
                reportParams.put("member_id", encounter.getMember().getId().toString());
                Rollbar.reportMessage("Failed to sync encounter", "warning", reportParams);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
