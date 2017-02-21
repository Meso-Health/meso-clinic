package org.watsi.uhp.models;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = User.TABLE_NAME)
public class User extends AbstractModel {

    public static final String TABLE_NAME = "users";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAME = "name";
    public static final String FIELD_NAME_USERNAME = "username";
    public static final String FIELD_NAME_ROLE = "role";

    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, id = true, canBeNull = false)
    private UUID mId;

    @SerializedName(FIELD_NAME_NAME)
    @DatabaseField(columnName = FIELD_NAME_NAME, canBeNull = false)
    private String mName;

    @SerializedName(FIELD_NAME_USERNAME)
    @DatabaseField(columnName = FIELD_NAME_USERNAME, canBeNull = false)
    private String mUsername;

    @SerializedName(FIELD_NAME_ROLE)
    @DatabaseField(columnName = FIELD_NAME_ROLE, canBeNull = false)
    private String mRole;

    public User() {
        super();
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getRole() {
        return mRole;
    }

    public void setRole(String role) {
        this.mRole = role;
    }
}
