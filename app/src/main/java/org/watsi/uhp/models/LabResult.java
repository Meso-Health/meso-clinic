package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.helpers.StringUtils;

import java.util.UUID;

@DatabaseTable(tableName = LabResult.TABLE_NAME)
public class LabResult extends AbstractModel {
    public static final String TABLE_NAME = "lab_results";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_RESULT = "result";
    public static final String FIELD_NAME_ENCOUNTER_ITEM_ID = "encounter_item_id";

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, canBeNull = false, generatedId = true)
    private UUID mId;

    @Expose
    @SerializedName(FIELD_NAME_RESULT)
    @DatabaseField(columnName = FIELD_NAME_RESULT)
    private LabResultEnum mResult;

    public enum LabResultEnum {
        @SerializedName("positive") POSITIVE,
        @SerializedName("negative") NEGATIVE,
        @SerializedName("unspecified") UNSPECIFIED;

        public static LabResultEnum fromString(String resultString) {
            return LabResultEnum.valueOf(resultString.toUpperCase());
        }

        public String toString() {
            return StringUtils.titleCase(name());
        }
    }

    @Expose(serialize = false)
    @SerializedName(FIELD_NAME_ENCOUNTER_ITEM_ID)
    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ITEM_ID, foreign = true, foreignAutoRefresh = true, unique = true)
    private EncounterItem mEncounterItem;

    public LabResult() {
        super();
    }

    public LabResult(EncounterItem encounterItem, LabResultEnum result) {
        this();
        mEncounterItem = encounterItem;
        mResult = result;
    }

    public LabResultEnum getResult() {
        return mResult;
    }

    public String toString() {
        return mResult.toString();
    }
}
