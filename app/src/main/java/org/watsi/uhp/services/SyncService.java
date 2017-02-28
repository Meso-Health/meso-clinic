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
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

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
                        syncIdentificationEvents(IdentificationEventDao.unsynced());
                        syncEncounters(EncounterDao.unsynced());
                        syncMembers(MemberDao.unsynced());
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
        for (IdentificationEvent event : unsyncedEvents) {
            event.setMemberId(event.getMember().getId());
            String tokenAuthorizationString = "Token " + event.getToken();
            if (event.getThroughMember() != null) {
                event.setThroughMemberId(event.getThroughMember().getId());
            }
            Call<IdentificationEvent> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncIdentificationEvent(tokenAuthorizationString, mProviderId, event);
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

    private void syncMembers(List<Member> unsyncedMembers) throws SQLException, IOException {
        for (Member member : unsyncedMembers) {
            Response<Member> response = member.formatPatchRequest(getApplicationContext()).execute();
            if (response.isSuccessful()) {
                member.setSynced(true);
                MemberDao.update(member);
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("member_id", member.getId().toString());
                Rollbar.reportMessage("Failed to sync member", "warning", reportParams);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
