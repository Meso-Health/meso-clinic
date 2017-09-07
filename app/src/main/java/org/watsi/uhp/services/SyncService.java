package org.watsi.uhp.services;

import android.util.Log;

import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class SyncService extends AbstractSyncJobService {

    @Override
    public boolean performSync() {
        Log.i("UHP", "SyncService.performSync is called");
        try {
            syncIdentificationEvents(IdentificationEvent.unsynced(IdentificationEvent.class));
            syncEncounters(Encounter.unsynced(Encounter.class));
            syncEncounterForms(EncounterForm.unsynced(EncounterForm.class));
            syncMembers(Member.unsynced(Member.class));
            return true;
        } catch (SQLException | IllegalStateException e) {
            ExceptionManager.reportException(e);
            return false;
        } catch (IOException e) {
            ExceptionManager.reportExceptionWarning(e);
            return false;
        }
    }

    protected void syncIdentificationEvents(List<IdentificationEvent> unsyncedEvents)
            throws SQLException, IOException {
        for (IdentificationEvent event : unsyncedEvents) {
            event.setMemberId(event.getMember().getId());
            try {
                Response<IdentificationEvent> response = event.sync(this);
                if (response.isSuccessful()) {
                    event.updateFromSync(response);
                } else {
                    Map<String,String> reportParams = new HashMap<>();
                    reportParams.put("identification_event.id", event.getId().toString());
                    reportParams.put("member.id", event.getMember().getId().toString());
                    ExceptionManager.requestFailure(
                            "Failed to sync IdentificationEvent", response.raw().request(),
                            response.raw(), reportParams);
                }
            } catch (SyncableModel.SyncException e) {
                ExceptionManager.reportException(e);
            }
        }
    }

    protected void syncEncounters(List<Encounter> unsyncedEncounters)
            throws SQLException, IOException {
        for (Encounter encounter : unsyncedEncounters) {
            try {
                Response<Encounter> response = encounter.sync(this);
                if (response.isSuccessful()) {
                    encounter.updateFromSync(response);
                } else {
                    Map<String,String> reportParams = new HashMap<>();
                    reportParams.put("encounter.id", encounter.getId().toString());
                    reportParams.put("member.id", encounter.getMember().getId().toString());
                    ExceptionManager.requestFailure(
                            "Failed to sync Encounter", response.raw().request(),
                            response.raw(), reportParams);
                }
            } catch (SyncableModel.SyncException e) {
                ExceptionManager.reportException(e);
            }
        }
    }

    protected void syncEncounterForms(List<EncounterForm> unsyncedEncounterForms)
            throws SQLException, IOException {
        for (EncounterForm encounterForm : unsyncedEncounterForms) {
            Encounter encounter = EncounterDao.find(encounterForm.getEncounter().getId());
            if (!encounter.isSynced()) {
                // do not push encounter form until related encounter is synced
                continue;
            }

            byte[] image = encounterForm.getPhoto().bytes(this);
            if (image == null) {
                ExceptionManager.reportMessage("Null image for form on encounter: " +
                        encounter.getId().toString());
                encounterForm.delete();
                continue;
            }
            try {
                Response<EncounterForm> response = encounterForm.sync(this);
                if (response.isSuccessful()) {
                    encounterForm.updateFromSync(response);
                } else {
                    Map<String,String> reportParams = new HashMap<>();
                    reportParams.put("encounter_form.id", encounterForm.getId().toString());
                    reportParams.put("encounter.id", encounterForm.getEncounter().getId().toString());
                    ExceptionManager.requestFailure(
                            "Failed to sync EncounterForm", response.raw().request(),
                            response.raw(), reportParams);
                }
            } catch (SyncableModel.SyncException e) {
                ExceptionManager.reportException(e);
            }
        }
    }

    protected void syncMembers(List<Member> unsyncedMembers) throws SQLException, IOException {
        for (Member member : unsyncedMembers) {
            try {
                Response<Member> response = member.sync(this);
                if (response.isSuccessful()) {
                    member.updateFromSync(response);
                } else {
                    Map<String, String> params = new HashMap<>();
                    params.put("member.id", member.getId().toString());
                    ExceptionManager.requestFailure(
                            "Failed to sync member", response.raw().request(),
                            response.raw(), params);
                }
            } catch (SyncableModel.SyncException e) {
                ExceptionManager.reportException(e);
            }
        }
    }
}
