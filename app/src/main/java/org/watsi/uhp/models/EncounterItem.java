package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = EncounterItem.TABLE_NAME)
public class EncounterItem extends AbstractModel {

    public static final String TABLE_NAME = "encounter_items";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_BILLABLE_ID = "billable_id";
    public static final String FIELD_NAME_QUANTITY = "quantity";

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @Expose
    @SerializedName(FIELD_NAME_ENCOUNTER_ID)
    private UUID mEncounterId;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ID, foreign = true)
    private Encounter mEncounter;

    @Expose(deserialize = false)
    @SerializedName(FIELD_NAME_BILLABLE_ID)
    @DatabaseField(columnName = FIELD_NAME_BILLABLE_ID, foreign = true)
    private Billable mBillable;

    @Expose
    @SerializedName(FIELD_NAME_QUANTITY)
    @DatabaseField(columnName = FIELD_NAME_QUANTITY)
    private int mQuantity = 1;

    public EncounterItem() {
    }

    public EncounterItem(Billable billable, int quantity) {
        setBillable(billable);
        setQuantity(quantity);
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
    }

    public Billable getBillable() {
        return mBillable;
    }

    public void setBillable(Billable billable) {
        this.mBillable = billable;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public void setQuantity(Integer quantity) {
        this.mQuantity = quantity;
    }
}