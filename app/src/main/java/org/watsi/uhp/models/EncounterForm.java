package org.watsi.uhp.models;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.api.ApiService;

import java.sql.SQLException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;

@DatabaseTable(tableName = EncounterForm.TABLE_NAME)
public class EncounterForm extends SyncableModel {

    public static final String TABLE_NAME = "encounter_forms";

    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_PHOTO = "photo_id";
    public static final String FIELD_NAME_URL = "url";

    @Expose(deserialize = false)
    @SerializedName(FIELD_NAME_ENCOUNTER_ID)
    private UUID mEncounterId;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ID, foreign = true, canBeNull = false)
    private Encounter mEncounter;

    @Expose(deserialize = false)
    @SerializedName(FIELD_NAME_PHOTO)
    @DatabaseField(columnName = FIELD_NAME_PHOTO, foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private Photo mPhoto;

    @SerializedName(FIELD_NAME_URL)
    @DatabaseField(columnName = FIELD_NAME_URL)
    private String mUrl;

    public UUID getEncounterId() {
        return mEncounterId;
    }

    public void setEncounterId(UUID encounterId) {
        this.mEncounterId = encounterId;
    }

    public Encounter getEncounter() {
        return mEncounter;
    }

    public void setEncounter(Encounter encounter) {
        this.mEncounter = encounter;
        setEncounterId(encounter.getId());
    }

    public Photo getPhoto() {
        return mPhoto;
    }

    public void setPhoto(Photo photo) {
        this.mPhoto = photo;
    }

    public String getUrl() {
        return mUrl;
    }

    public void validate() throws ValidationException {
        // no-op
    }

    @Override
    public void handleUpdateFromSync(SyncableModel responseModel) throws SQLException {
        // set the ID, URL and encounter ID from the model onto the response so that they do
        //  not get marked as dirty fields when the models are diffed in the sync logic
        EncounterForm response = (EncounterForm) responseModel;
        response.setId(getId());
        response.setEncounterId(getEncounterId());
        ((EncounterForm) responseModel).setPhoto(getPhoto());
    }

    @Override
    protected Call postApiCall(Context context) throws SQLException {
        RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), getPhoto().bytes(context));
        return ApiService.requestBuilder(context).syncEncounterForm(
                getTokenAuthHeaderString(), getEncounter().getId(), body);
    }

    @Override
    protected void persistAssociations() {
        // no-op
    }

    @Override
    protected Call patchApiCall(Context context) throws SQLException, SyncException {
        throw new SyncException("Tried to patch an EncounterForm with dirty fields: " + getDirtyFields().toString());
    }
}
