package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = Billable.TABLE_NAME)
public class Billable extends AbstractModel {

    public static final String TABLE_NAME = "billables";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_TYPE = "type";
    public static final String FIELD_NAME_COMPOSITION = "composition";
    public static final String FIELD_NAME_UNIT = "unit";
    public static final String FIELD_NAME_PRICE = "price";
    public static final String FIELD_NAME_NAME = "name";

    public enum TypeEnum {
        @SerializedName("drug") DRUG,
        @SerializedName("service") SERVICE,
        @SerializedName("lab") LAB,
        @SerializedName("supply") SUPPLY,
        @SerializedName("vaccine") VACCINE,
        @SerializedName("unspecified") UNSPECIFIED
    }

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @Expose
    @SerializedName(FIELD_NAME_NAME)
    @DatabaseField(columnName = FIELD_NAME_NAME, canBeNull = false)
    private String mName;

    @Expose
    @SerializedName(FIELD_NAME_TYPE)
    @DatabaseField(columnName = FIELD_NAME_TYPE, canBeNull = false)
    private TypeEnum mType;

    @Expose
    @SerializedName(FIELD_NAME_UNIT)
    @DatabaseField(columnName = FIELD_NAME_UNIT)
    private String mUnit;

    @Expose
    @SerializedName(FIELD_NAME_COMPOSITION)
    @DatabaseField(columnName = FIELD_NAME_COMPOSITION)
    private String mComposition;

    @Expose
    @SerializedName(FIELD_NAME_PRICE)
    @DatabaseField(columnName = FIELD_NAME_PRICE, canBeNull = false)
    private Integer mPrice;

    public Billable() {
        super();
    }

    public UUID getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setType(TypeEnum type) {
        this.mType = type;
    }

    public TypeEnum getType() {
        return mType;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        this.mUnit = unit;
    }

    public String getComposition() {
        return mComposition;
    }

    public void setComposition(String composition) {
        this.mComposition = composition;
    }

    public Integer getPrice() {
        return mPrice;
    }

    public void setPrice(Integer price) {
        this.mPrice = price;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        if (dosageDetails() != null) {
            sb.append(" - " + dosageDetails());
        }
        return sb.toString();
    }

    public String dosageDetails() {
        if (getComposition() != null) {
            StringBuilder sb = new StringBuilder();
            if (getUnit() != null) {
                sb.append(getUnit() + " ");
            }
            sb.append(getComposition());
            return sb.toString();
        } else {
            return null;
        }
    }
}
