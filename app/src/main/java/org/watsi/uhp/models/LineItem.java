package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = LineItem.TABLE_NAME)
public class LineItem {

    public static final String TABLE_NAME = "line_items";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_BILLABLE_ID = "billable_id";
    public static final String FIELD_NAME_QUANTITY = "quantity";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_BILLABLE_ID, foreign = true)
    private Billable mBillable;

    @DatabaseField(columnName = FIELD_NAME_QUANTITY)
    private int mQuantity = 1;

    public LineItem() {
        // empty constructor necessary for ORM
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

    public void increaseQuantity() {
        this.mQuantity++;
    }

    public void decreaseQuantity() {
        if (mQuantity > 1) {
            this.mQuantity--;
        }
    }
}
