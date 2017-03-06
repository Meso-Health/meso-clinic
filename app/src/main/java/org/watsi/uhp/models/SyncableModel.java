package org.watsi.uhp.models;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for specifying any fields and behavior that all
 * models that need to be synced to the back-end should share
 */
public abstract class SyncableModel extends AbstractModel {

    public static final String FIELD_NAME_TOKEN = "token";
    public static final String FIELD_NAME_SYNCED = "synced";
    public static final String FIELD_NAME_DIRTY_FIELDS = "dirty_fields";

    @DatabaseField(columnName = FIELD_NAME_TOKEN)
    private String mToken;

    @DatabaseField(columnName = FIELD_NAME_SYNCED, defaultValue = "false", canBeNull = false)
    private boolean mSynced;

    @DatabaseField(columnName = FIELD_NAME_DIRTY_FIELDS, defaultValue = "[]", canBeNull = false)
    private String mDirtyFields = "[]";

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public boolean isSynced() {
        return mSynced;
    }

    public void setSynced() {
        setToken(null);
        clearDirtyFields();
        this.mSynced = true;
    }

    public void setUnsynced(String token) {
        setToken(token);
        this.mSynced = false;
    }

    public void setDirtyFields(Set<String> dirtyFields) {
        this.mDirtyFields = new Gson().toJson(dirtyFields);
    }

    public Set<String> getDirtyFields() {
        if (this.mDirtyFields == null) {
            return new HashSet<>();
        } else {
            return new Gson().fromJson(this.mDirtyFields, Set.class);
        }
    }

    public boolean dirty(String fieldName) {
        return getDirtyFields().contains(fieldName);
    }

    public void addDirtyField(String field) {
        Set<String> currentFields = getDirtyFields();
        currentFields.add(field);
        setDirtyFields(currentFields);
    }

    private void clearDirtyFields() {
        setDirtyFields(new HashSet<String>());
    }

    public String getTokenAuthHeaderString() {
        return "Token " + getToken();
    }
}
