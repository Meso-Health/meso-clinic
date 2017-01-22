package org.watsi.uhp.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.math.BigDecimal;

@DatabaseTable(tableName = Billable.TABLE_NAME)
public class Billable {

    public static final String TABLE_NAME = "billables";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_TYPE = "type";
    public static final String FIELD_NAME_UNIT = "unit";
    public static final String FIELD_NAME_AMOUNT = "amount";
    public static final String FIELD_NAME_PRICE = "price";

    public enum BillableTypeEnum { SUPPLY, LAB, SERVICE }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_TYPE, canBeNull = false)
    private BillableTypeEnum mType;

    @DatabaseField(columnName = FIELD_NAME_UNIT)
    private String mUnit;

    @DatabaseField(columnName = FIELD_NAME_AMOUNT, dataType = DataType.BIG_DECIMAL_NUMERIC)
    private BigDecimal mAmount;

    @DatabaseField(columnName = FIELD_NAME_PRICE)
    private Integer mPrice;

    public Billable() {
        // empty constructor necessary for ORM
    }

    public int getId() {
        return mId;
    }

    public void setType(BillableTypeEnum type) {
        this.mType = type;
    }

    public BillableTypeEnum getType() {
        return mType;
    }

    public String getUnit() {
        return mUnit;
    }

    public void setUnit(String mUnit) {
        this.mUnit = mUnit;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public void setAmount(BigDecimal amount) {
        this.mAmount = amount;
    }

    public Integer getPrice() {
        return mPrice;
    }

    public void setPrice(Integer price) {
        this.mPrice = price;
    }
}
