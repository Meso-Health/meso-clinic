package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Encounter.TABLE_NAME)
public class Encounter extends AbstractModel {

    public static final String TABLE_NAME = "encounters";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_ID_METHOD = "id_method";

    public enum IdMethodEnum { SEARCH, BARCODE, RECENT }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_ID_METHOD, canBeNull = false)
    private IdMethodEnum mIdMethod;

    public Encounter() {
        super();
    }

    public int getId() {
        return mId;
    }

    public void setMember(Member member) {
        this.mMember = member;
    }

    public Member getMember() {
        return mMember;
    }

    public IdMethodEnum getIdMethod() {
        return mIdMethod;
    }

    public void setIdMethod(IdMethodEnum idMethod) {
        this.mIdMethod = idMethod;
    }
}
