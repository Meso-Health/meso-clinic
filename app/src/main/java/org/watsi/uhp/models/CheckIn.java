package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;

@DatabaseTable(tableName = CheckIn.TABLE_NAME)
public class CheckIn {

    public static final String TABLE_NAME = "check_ins";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_DATE = "date";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_OUTCOME = "outcome";

    private enum OutcomeEnum { TURNED_AWAY, ADMITTED_OUTPATIENT, ADMITTED_INPATIENT }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_DATE, generatedId = true)
    private Date mDate;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private int mMemberId;

    @DatabaseField(columnName = FIELD_NAME_OUTCOME)
    private OutcomeEnum mOutcome;

    public CheckIn() {
        this.mDate = Calendar.getInstance().getTime();
    }

    public int getId() {
        return mId;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public Date getDate() {
        return mDate;
    }

    public void setMemberId(int memberId) {
        this.mMemberId = memberId;
    }

    public int getMemberId() {
        return mMemberId;
    }

    public void setOutcome(OutcomeEnum outcome) {
        this.mOutcome = outcome;
    }

    public OutcomeEnum getOutcome() {
        return mOutcome;
    }
}
