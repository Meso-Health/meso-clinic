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
import org.watsi.uhp.managers.NotificationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class SyncService extends Service {

    private static int SLEEP_TIME = 60 * 1000; // every minute
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
            String tokenAuthorizationString = event.getTokenAuthHeaderString();
            if (event.getThroughMember() != null) {
                event.setThroughMemberId(event.getThroughMember().getId());
            }
            Call<IdentificationEvent> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncIdentificationEvent(tokenAuthorizationString, mProviderId, event);
            Response<IdentificationEvent> response = request.execute();
            if (response.isSuccessful()) {
                try {
                    event.setSynced();
                    IdentificationEventDao.update(event);
                } catch (AbstractModel.ValidationException e) {
                    Rollbar.reportException(e);
                }
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("identification_event.id", event.getId().toString());
                reportParams.put("member.id", event.getMember().getId().toString());
                NotificationManager.requestFailure(
                        "Failed to sync IdentificationEvent",
                        request.request(),
                        response.raw(),
                        reportParams
                );
            }
        }
    }

    private void syncEncounters(List<Encounter> unsyncedEncounters) throws SQLException, IOException {
        for (Encounter encounter : unsyncedEncounters) {
            encounter.setMemberId(encounter.getMember().getId());
            encounter.setIdentificationEventId(encounter.getIdentificationEvent().getId());
            encounter.setEncounterItems(EncounterItemDao.fromEncounter(encounter));
            String tokenAuthorizationString = encounter.getTokenAuthHeaderString();
            Call<Encounter> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncEncounter(tokenAuthorizationString, mProviderId, encounter);
            Response<Encounter> response = request.execute();
            if (response.isSuccessful()) {
                try {
                    encounter.setSynced();
                    EncounterDao.update(encounter);
                } catch (AbstractModel.ValidationException e) {
                    Rollbar.reportException(e);
                }
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("encounter.id", encounter.getId().toString());
                reportParams.put("member.id", encounter.getMember().getId().toString());
                NotificationManager.requestFailure(
                        "Failed to sync Encounter",
                        request.request(),
                        response.raw(),
                        reportParams
                );
            }
        }
    }

    private void syncMembers(List<Member> unsyncedMembers) throws SQLException, IOException {
        for (Member member : unsyncedMembers) {

            if (member.isNew()) {
                enrollMember(member);
            } else {
                updateMember(member);
            }
        }
    }

    private void updateMember(Member member) throws SQLException, IOException {
        Map<String, RequestBody> multiPartBody;
        try {
            multiPartBody = member.formatPatchRequest(getApplicationContext());
        } catch (AbstractModel.ValidationException e) {
            Rollbar.reportException(e);
            return;
        }
        Call<Member> request = ApiService.requestBuilder(getApplicationContext()).syncMember(
                member.getTokenAuthHeaderString(),
                member.getId().toString(),
                multiPartBody
        );
        Response<Member> response = request.execute();
        if (response.isSuccessful()) {
            // if we have updated a photo, remove the local version and fetch the remote one
            if (member.getPhotoUrl() != null &&
                    !member.getPhotoUrl().equals(response.body().getPhotoUrl()) &&
                    !member.dirty(Member.FIELD_NAME_PHOTO)) {
                member.setMemberPhotoUrlFromResponse(response.body().getPhotoUrl());
                member.fetchAndSetPhotoFromUrl();
            }
            if (member.getNationalIdPhotoUrl() != null &&
                    !member.getNationalIdPhotoUrl().equals(
                            response.body().getNationalIdPhotoUrl()) &&
                    !member.dirty(Member.FIELD_NAME_NATIONAL_ID_PHOTO)) {
                member.setNationalIdPhotoUrlFromPatchResponse(
                        response.body().getNationalIdPhotoUrl());
            }
            if (!member.isDirty()) {
                try {
                    member.setSynced();
                } catch (AbstractModel.ValidationException e) {
                    Rollbar.reportException(e);
                }
            }
            MemberDao.update(member);
        } else {
            Map<String,String> reportParams = new HashMap<>();
            reportParams.put("member.id", member.getId().toString());
            NotificationManager.requestFailure(
                    "Failed to sync Member",
                    request.request(),
                    response.raw(),
                    reportParams
            );
        }
    }

    private void enrollMember(Member member) throws SQLException, IOException {
        Map<String, RequestBody> multiPartBody;
        try {
            multiPartBody = member.formatPostRequest(getApplicationContext());
        } catch (AbstractModel.ValidationException e) {
            Rollbar.reportException(e);
            return;
        }
        Call<Member> request = ApiService.requestBuilder(getApplicationContext()).enrollMember(
                member.getTokenAuthHeaderString(),
                member.getId().toString(),
                multiPartBody
        );
        Response<Member> response = request.execute();
        if (response.isSuccessful()) {
            // if we have updated a photo, remove the local version and fetch the remote one
            if (member.getPhotoUrl() != null &&
                    !member.getPhotoUrl().equals(response.body().getPhotoUrl()) &&
                    !member.dirty(Member.FIELD_NAME_PHOTO)) {
                member.setMemberPhotoUrlFromResponse(response.body().getPhotoUrl());
                member.fetchAndSetPhotoFromUrl();
            }
            try {
                member.setSynced();
            } catch (AbstractModel.ValidationException e) {
                Rollbar.reportException(e);
            }
            MemberDao.update(member);
        } else {
            Map<String,String> reportParams = new HashMap<>();
            reportParams.put("member.id", member.getId().toString());
            NotificationManager.requestFailure(
                    "Failed to enroll Member",
                    request.request(),
                    response.raw(),
                    reportParams
            );
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
