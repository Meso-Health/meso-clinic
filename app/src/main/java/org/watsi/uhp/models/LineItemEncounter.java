package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = LineItemEncounter.TABLE_NAME)
public class LineItemEncounter {

    public static final String TABLE_NAME = "line_items_encounters";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_LINE_ITEM = "line_item_id";
    public static final String FIELD_NAME_ENCOUNTER = "encounter_id";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_LINE_ITEM, foreign = true)
    private LineItem mLineItem;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER, foreign = true)
    private Encounter mEncounter;

    public LineItemEncounter() {
        // empty constructor necessary for ORM
    }

    public LineItemEncounter(LineItem lineItem, Encounter encounter) {
        this.mLineItem = lineItem;
        this.mEncounter = encounter;
    }

    public int getId() {
        return mId;
    }
}
