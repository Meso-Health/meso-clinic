package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = User.TABLE_NAME_USERS)
public class User {

    public static final String TABLE_NAME_USERS = "users";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAME = "name";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_NAME)
    private String mName;

    public User() {
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
