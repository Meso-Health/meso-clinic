package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Member.TABLE_NAME_MEMBERS)
public class Member {

    public static final String TABLE_NAME_MEMBERS = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAME = "name";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_NAME)
    private String mName;

    public Member() {
        // empty constructor necessary for ORM
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }
}
