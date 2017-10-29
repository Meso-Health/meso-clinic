package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Diagnosis.TABLE_NAME)
public class Diagnosis extends AbstractModel {
    public static final String TABLE_NAME = "diagnoses";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_DESCRIPTION = "description";
    public static final String FIELD_NAME_SEARCH_ALIASES = "search_aliases";

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, canBeNull = false, id = true)
    private Integer mId;

    @Expose
    @SerializedName(FIELD_NAME_DESCRIPTION)
    @DatabaseField(columnName = FIELD_NAME_DESCRIPTION, canBeNull = false)
    private String mDescription;

    @Expose
    @SerializedName(FIELD_NAME_SEARCH_ALIASES)
    @DatabaseField(columnName = FIELD_NAME_SEARCH_ALIASES)
    private String mSearchAliases;

    public Diagnosis() {
        super();
    }

    public Diagnosis(Integer id, String description, String searchAliases) {
        super();
        mId = id;
        mDescription = description;
        mSearchAliases = searchAliases;
    }

    public Integer getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String toString() {
        return mDescription;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof Diagnosis && ((Diagnosis) object).getId().equals(mId);
    }

    @Override
    public int hashCode() {
        return mId;
    }

}
