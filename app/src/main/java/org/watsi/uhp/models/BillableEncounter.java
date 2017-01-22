package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = BillableEncounter.TABLE_NAME)
public class BillableEncounter {

    public static final String TABLE_NAME = "billables_encounters";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_BILLABLE = "billable_id";
    public static final String FIELD_NAME_ENCOUNTER = "encounter_id";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_BILLABLE, foreign = true)
    private Billable mBillable;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER, foreign = true)
    private Encounter mEncounter;

    public BillableEncounter() {
        // empty constructor necessary for ORM
    }

    public BillableEncounter(Billable billable, Encounter encounter) {
        this.mBillable = billable;
        this.mEncounter = encounter;
    }

    public int getId() {
        return mId;
    }
}
