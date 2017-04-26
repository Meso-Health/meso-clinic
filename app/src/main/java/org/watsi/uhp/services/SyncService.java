package org.watsi.uhp.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterFormDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class SyncService extends Service {

    private static int SLEEP_TIME = 60 * 1000; // every minute
    private int mProviderId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DatabaseHelper.init(getApplicationContext());
        mProviderId = BuildConfig.PROVIDER_ID;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        syncIdentificationEvents(IdentificationEventDao.unsynced());
                        syncEncounters(EncounterDao.unsynced());
                        syncEncounterForms(EncounterFormDao.unsynced());
                        syncMembers(MemberDao.unsynced());
                    } catch (IOException | SQLException | IllegalStateException e) {
                        ExceptionManager.reportException(e);
                    }
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        ExceptionManager.reportException(e);
                    }
                }
            }
        }).start();
        return Service.START_REDELIVER_INTENT;
    }

    private void syncIdentificationEvents(List<IdentificationEvent> unsyncedEvents) throws SQLException, IOException {
        for (IdentificationEvent event : unsyncedEvents) {
            event.setMemberId(event.getMember().getId());
            Response<IdentificationEvent> response;
            if (event.isNew()) {
                response = postIdentificationEvent(event);
            } else {
                if (!event.getDismissed()) {
                    // we should only be patching dismissed identification events
                    Map<String, String> params = new HashMap<>();
                    params.put("identification_event.id", event.getId().toString());
                    params.put("identification_event.json", new Gson().toJson(event));
                    ExceptionManager.reportMessage(
                            "Attempted to sync non-dismissed IdentificationEvent", "warning", params);
                    continue;
                }
                response = patchIdentificationEvent(event);
            }
            if (response.isSuccessful()) {
                try {
                    event.setSynced();
                    IdentificationEventDao.update(event);
                } catch (AbstractModel.ValidationException e) {
                    ExceptionManager.reportException(e);
                }
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("identification_event.id", event.getId().toString());
                reportParams.put("member.id", event.getMember().getId().toString());
                ExceptionManager.requestFailure(
                        "Failed to sync IdentificationEvent",
                        response.raw().request(),
                        response.raw(),
                        reportParams
                );
            }
        }
    }

    private Response<IdentificationEvent> postIdentificationEvent(IdentificationEvent idEvent) throws IOException {
        String tokenAuthorizationString = idEvent.getTokenAuthHeaderString();
        if (idEvent.getThroughMember() != null) {
            idEvent.setThroughMemberId(idEvent.getThroughMember().getId());
        }
        Call<IdentificationEvent> request =
                ApiService.requestBuilder(getApplicationContext())
                        .postIdentificationEvent(tokenAuthorizationString, mProviderId, idEvent);
        return request.execute();
    }

    private Response<IdentificationEvent> patchIdentificationEvent(IdentificationEvent idEvent) throws IOException {
        String tokenAuthorizationString = idEvent.getTokenAuthHeaderString();
        // convert to json so serialization is consistent with POST request
        JsonObject json = new Gson().toJsonTree(idEvent, IdentificationEvent.class).getAsJsonObject();
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        requestBodyMap.put(IdentificationEvent.FIELD_NAME_DISMISSED,
                RequestBody.create(MultipartBody.FORM,
                        json.get(IdentificationEvent.FIELD_NAME_DISMISSED).getAsString()));
        requestBodyMap.put(IdentificationEvent.FIELD_NAME_DISMISSAL_REASON,
                RequestBody.create(MultipartBody.FORM,
                        json.get(IdentificationEvent.FIELD_NAME_DISMISSAL_REASON).getAsString()));
        Call<IdentificationEvent> request =
                ApiService.requestBuilder(getApplicationContext())
                        .patchIdentificationEvent(
                                tokenAuthorizationString, idEvent.getId(), requestBodyMap);
        return request.execute();
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
                    ExceptionManager.reportException(e);
                }
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("encounter.id", encounter.getId().toString());
                reportParams.put("member.id", encounter.getMember().getId().toString());
                ExceptionManager.requestFailure(
                        "Failed to sync Encounter",
                        request.request(),
                        response.raw(),
                        reportParams
                );
            }
        }
    }

    private void syncEncounterForms(List<EncounterForm> unsyncedEncounterForms) throws SQLException, IOException {
        for (EncounterForm encounterForm : unsyncedEncounterForms) {
            String tokenAuthorizationString = encounterForm.getTokenAuthHeaderString();
            Encounter encounter = EncounterDao.find(encounterForm.getEncounter().getId());
            if (!encounter.isSynced()) {
                // do not push encounter form until related encounter is synced
                continue;
            }

            byte[] image = FileManager.readFromUri(Uri.parse(encounterForm.getUrl()), getApplicationContext());
            if (image == null) {
                ExceptionManager.reportMessage("No image saved for form for encounter: " + encounter
                        .getId().toString());
                try {
                    encounterForm.setSynced();
                    EncounterFormDao.update(encounterForm);
                } catch (AbstractModel.ValidationException e) {
                    ExceptionManager.reportException(e);
                }
                continue;
            }
            RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), image);
            Call<Encounter> request =
                    ApiService.requestBuilder(getApplicationContext())
                            .syncEncounterForm(tokenAuthorizationString, encounter.getId(), body);
            Response<Encounter> response = request.execute();
            if (response.isSuccessful()) {
                try {
                    new File(encounterForm.getUrl()).delete();
                    encounterForm.setSynced();
                    EncounterFormDao.update(encounterForm);
                } catch (AbstractModel.ValidationException e) {
                    ExceptionManager.reportException(e);
                }
            } else {
                Map<String,String> reportParams = new HashMap<>();
                reportParams.put("encounter_form.id", encounterForm.getId().toString());
                reportParams.put("encounter.id", encounterForm.getEncounter().getId().toString());
                ExceptionManager.requestFailure(
                        "Failed to sync EncounterForm",
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
            ExceptionManager.reportException(e);
            return;
        }
        Call<Member> request = ApiService.requestBuilder(getApplicationContext()).syncMember(
                member.getTokenAuthHeaderString(),
                member.getId(),
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
                    ExceptionManager.reportException(e);
                }
            }
            MemberDao.update(member);
        } else {
            Map<String,String> reportParams = new HashMap<>();
            reportParams.put("member.id", member.getId().toString());
            ExceptionManager.requestFailure(
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
            ExceptionManager.reportException(e);
            return;
        }
        Call<Member> request = ApiService.requestBuilder(getApplicationContext()).enrollMember(
                member.getTokenAuthHeaderString(),
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
                ExceptionManager.reportException(e);
            }
            MemberDao.update(member);
        } else {
            Map<String,String> reportParams = new HashMap<>();
            reportParams.put("member.id", member.getId().toString());
            ExceptionManager.requestFailure(
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
