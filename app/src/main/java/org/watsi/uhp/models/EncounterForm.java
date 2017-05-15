package org.watsi.uhp.models;

import android.content.Context;
import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.FileManager;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

@DatabaseTable(tableName = EncounterForm.TABLE_NAME)
public class EncounterForm extends SyncableModel {

    public static final String TABLE_NAME = "encounter_forms";

    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_URL = "url";

    @Expose
    @SerializedName(FIELD_NAME_ENCOUNTER_ID)
    private UUID mEncounterId;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ID, foreign = true, canBeNull = false)
    private Encounter mEncounter;

    @Expose
    @SerializedName(FIELD_NAME_URL)
    @DatabaseField(columnName = FIELD_NAME_URL, canBeNull = false)
    private String mUrl;

    public EncounterForm() {
        super();
    }

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

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public byte[] getImage(Context context) {
        return FileManager.readFromUri(Uri.parse(getUrl()), context);
    }

    @Override
    public void handleUpdateFromSync(Response response) {
        new File(getUrl()).delete();
    }

    @Override
    protected Call postApiCall(Context context) throws SQLException {
        RequestBody body = RequestBody.create(MediaType.parse("image/jpg"), getImage(context));
        return ApiService.requestBuilder(context).syncEncounterForm(
                getTokenAuthHeaderString(), getId(), body);
    }

    @Override
    protected void persistAssociations() {
        // no-op
    }

    @Override
    protected Call patchApiCall(Context context) throws SQLException  {
        // no-op
        return null;
    }

    public void destroy() throws SQLException {
        getDao().deleteById(getId());
    }
}
