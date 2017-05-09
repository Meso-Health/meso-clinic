package org.watsi.uhp.services;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterFormDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
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
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class SyncService extends AbstractSyncJobService {

    @Override
    public boolean performSync() {
        try {
            syncIdentificationEvents(IdentificationEventDao.unsynced());
            syncEncounters(EncounterDao.unsynced());
            syncEncounterForms(EncounterFormDao.unsynced());
            syncMembers(MemberDao.unsynced());
            return true;
        } catch (IOException | SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    protected void syncIdentificationEvents(List<IdentificationEvent> unsyncedEvents)
            throws SQLException, IOException {
        for (IdentificationEvent event : unsyncedEvents) {
            event.setMemberId(event.getMember().getId());
            Response<IdentificationEvent> response;
            response = event.isNew() ?
                    postIdentificationEvent(event) : patchIdentificationEvent(event);
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
                        "Failed to sync IdentificationEvent", response.raw().request(),
                        response.raw(), reportParams);
            }
        }
    }

    protected Response<IdentificationEvent> postIdentificationEvent(IdentificationEvent idEvent)
            throws IOException {
        String tokenAuthorizationString = idEvent.getTokenAuthHeaderString();
        if (idEvent.getThroughMember() != null) {
            idEvent.setThroughMemberId(idEvent.getThroughMember().getId());
        }
        Call<IdentificationEvent> request =
                ApiService.requestBuilder(this).postIdentificationEvent(
                        tokenAuthorizationString, BuildConfig.PROVIDER_ID, idEvent);
        return request.execute();
    }

    protected Response<IdentificationEvent> patchIdentificationEvent(IdentificationEvent idEvent)
            throws IOException {
        String tokenAuthorizationString = idEvent.getTokenAuthHeaderString();
        Map<String, RequestBody> requestBodyMap =
                idEvent.constructIdentificationEventPatchRequest();
        Call<IdentificationEvent> request =
                ApiService.requestBuilder(this).patchIdentificationEvent(
                        tokenAuthorizationString, idEvent.getId(), requestBodyMap);
        return request.execute();
    }

    protected void syncEncounters(List<Encounter> unsyncedEncounters)
            throws SQLException, IOException {
        for (Encounter encounter : unsyncedEncounters) {
            encounter.setMemberId(encounter.getMember().getId());
            encounter.setIdentificationEventId(encounter.getIdentificationEvent().getId());
            encounter.setEncounterItems(EncounterItemDao.fromEncounter(encounter));
            String tokenAuthorizationString = encounter.getTokenAuthHeaderString();
            Call<Encounter> request =
                    ApiService.requestBuilder(this).syncEncounter(
                            tokenAuthorizationString, BuildConfig.PROVIDER_ID, encounter);
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
                        "Failed to sync Encounter", request.request(),
                        response.raw(), reportParams);
            }
        }
    }

    protected void syncEncounterForms(List<EncounterForm> unsyncedEncounterForms)
            throws SQLException, IOException {
        for (EncounterForm encounterForm : unsyncedEncounterForms) {
            String tokenAuthorizationString = encounterForm.getTokenAuthHeaderString();
            Encounter encounter = EncounterDao.find(encounterForm.getEncounter().getId());
            if (!encounter.isSynced()) {
                // do not push encounter form until related encounter is synced
                continue;
            }

            byte[] image = encounterForm.getImage(this);
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
                    ApiService.requestBuilder(this)
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
                        "Failed to sync EncounterForm", request.request(),
                        response.raw(), reportParams);
            }
        }
    }

    protected void syncMembers(List<Member> unsyncedMembers) {
        for (Member member : unsyncedMembers) {
            try {
                member.syncMember(this);
            } catch (SQLException | IOException | AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }
        }
    }
}
