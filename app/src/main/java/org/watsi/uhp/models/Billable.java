package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Billable.TABLE_NAME)
public class Billable extends AbstractModel {

    public static final String TABLE_NAME = "billables";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_CATEGORY = "category";
    public static final String FIELD_NAME_DEPARTMENT = "department";
    public static final String FIELD_NAME_UNIT = "unit";
    public static final String FIELD_NAME_AMOUNT = "amount";
    public static final String FIELD_NAME_PRICE = "price";
    public static final String FIELD_NAME_NAME = "name";

    public enum CategoryEnum {
        DRUGS,
        SERVICES,
        LABS,
        SUPPLIES,
        VACCINES,
        UNSPECIFIED
    }

    //TODO: remove
    public enum DepartmentEnum {
        ART_CLINIC,
        ANTENATAL,
        UNSPECIFIED
    }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_NAME, canBeNull = false)
    private String mName;

    @DatabaseField(columnName = FIELD_NAME_CATEGORY, canBeNull = false)
    private CategoryEnum mCategory;

    @DatabaseField(columnName = FIELD_NAME_DEPARTMENT, canBeNull = false)
    private DepartmentEnum mDepartment;

    @DatabaseField(columnName = FIELD_NAME_UNIT)
    private String mUnit;

    @DatabaseField(columnName = FIELD_NAME_AMOUNT)
    private String mAmount;

    @DatabaseField(columnName = FIELD_NAME_PRICE, canBeNull = false)
    private Integer mPrice;

    public Billable() {
        super();
    }

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setCategory(CategoryEnum category) {
        this.mCategory = category;
    }

    public CategoryEnum getCategory() {
        return mCategory;
    }

    public DepartmentEnum getDepartment() {
        return mDepartment;
    }

    public void setDepartment(DepartmentEnum department) {
        this.mDepartment = department;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String unit) {
        this.mUnit = unit;
    }

    public String getAmount() {
        return mAmount;
    }

    public void setAmount(String amount) {
        this.mAmount = amount;
    }

    public Integer getPrice() {
        return mPrice;
    }

    public void setPrice(Integer price) {
        this.mPrice = price;
    }

    public String getDisplayName() {
        if (getUnit() != null) {
            return getName() + " - " + getDisplayDetails();
        } else {
            return getName();
        }
    }

    public String getDisplayDetails() {
        String billableDetails = "";
        if (getAmount() != null) billableDetails += getAmount() + " ";
        if (getUnit() != null) billableDetails += getUnit();
        return billableDetails;
    }
}
