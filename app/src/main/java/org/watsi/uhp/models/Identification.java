package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = Identification.TABLE_NAME)
public class Identification {

    public static final String TABLE_NAME = "identifications";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_ID_METHOD = "id_method";
    public static final String FIELD_NAME_SUCCESSFUL = "successful";

    public enum IdMethodEnum { BARCODE, SEARCH_ID, SEARCH_NAME }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_ID_METHOD, canBeNull = false)
    private IdMethodEnum mIdMethod;

    @DatabaseField(columnName = FIELD_NAME_SUCCESSFUL, canBeNull = false)
    private boolean mSuccessful;

    public Identification() {
        // empty constructor necessary for ORM
    }

    public int getId() {
        return mId;
    }

    public Member getMember() {
        return mMember;
    }

    public void setMember(Member member) {
        this.mMember = member;
    }

    public IdMethodEnum getIdMethod() {
        return mIdMethod;
    }

    public void setIdMethod(IdMethodEnum idMethod) {
        this.mIdMethod = idMethod;
    }

    public boolean getSuccessful() {
        return mSuccessful;
    }

    public void setSuccessful(boolean successful) {
        this.mSuccessful = successful;
    }
}
