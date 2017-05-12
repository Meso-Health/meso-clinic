package org.watsi.uhp.models;

import android.content.Context;
import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.managers.FileManager;

import java.util.UUID;

@DatabaseTable(tableName = EncounterForm.TABLE_NAME)
public class EncounterForm extends SyncableModel {

    public static final String TABLE_NAME = "encounter_forms";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_URL = "url";

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

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

    public UUID getId() {
        return mId;
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
}
