package org.watsi.uhp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static android.os.UserHandle.readFromParcel;

@DatabaseTable(tableName = LineItem.TABLE_NAME)
public class LineItem extends AbstractModel implements Parcelable {

    public static final String TABLE_NAME = "line_items";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_ENCOUNTER_ID = "encounter_id";
    public static final String FIELD_NAME_BILLABLE_ID = "billable_id";
    public static final String FIELD_NAME_QUANTITY = "quantity";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_ENCOUNTER_ID, foreign = true)
    private Encounter mEncounter;

    @DatabaseField(columnName = FIELD_NAME_BILLABLE_ID, foreign = true)
    private Billable mBillable;

    @DatabaseField(columnName = FIELD_NAME_QUANTITY)
    private int mQuantity = 1;

    public LineItem() {
        super();
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
        setQuantity(getQuantity() + 1);
    }

    public void decreaseQuantity() {
        if (getQuantity() > 1) {
            setQuantity(getQuantity() - 1);
        }
    }

    @SuppressWarnings("unused")
    public LineItem(Parcel in) {
        this();
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public final Parcelable.Creator<LineItem> CREATOR = new Parcelable.Creator<LineItem>() {
        public LineItem createFromParcel(Parcel in) {
            return new LineItem(in);
        }

        public LineItem[] newArray(int size) {
            return new LineItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mBillable.getName());
        dest.writeInt(mBillable.getPrice());
        dest.writeString(mBillable.getAmount());
        dest.writeString(mBillable.getUnit());
        dest.writeInt(mQuantity);
    }
}
