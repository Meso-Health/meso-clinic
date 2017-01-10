package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = CheckIn.TABLE_NAME)
public class CheckIn {

    public static final String TABLE_NAME = "check_ins";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_DATE = "date";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_OUTCOME = "outcome";

    public enum OutcomeEnum { TURNED_AWAY, ADMITTED_OUTPATIENT, ADMITTED_INPATIENT }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_DATE)
    private Date mDate;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_OUTCOME, canBeNull = false)
    private OutcomeEnum mOutcome;

    public CheckIn() {
        // empty constructor necessary for ORM
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

    public void setMember(Member member) {
        this.mMember = member;
    }

    public Member getMember() {
        return mMember;
    }

    public void setOutcome(OutcomeEnum outcome) {
        this.mOutcome = outcome;
    }

    public OutcomeEnum getOutcome() {
        return mOutcome;
    }
}
