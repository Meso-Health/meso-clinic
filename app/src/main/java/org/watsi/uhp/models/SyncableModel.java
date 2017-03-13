package org.watsi.uhp.models;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for specifying any fields and behavior that all
 * models that need to be synced to the back-end should share
 */
public abstract class SyncableModel extends AbstractModel implements Serializable {

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

    public void setSynced() throws ValidationException {
        if (isDirty()) {
            String dirtyField = (String) getDirtyFields().toArray()[0];
            throw new ValidationException(dirtyField, "Cannot mark dirty model as synced");
        } else {
            setToken(null);
            this.mSynced = true;
        }
    }

    public void setUnsynced(String token) {
        setToken(token);
        this.mSynced = false;
    }

    private void setDirtyFields(Set<String> dirtyFields) {
        this.mDirtyFields = new Gson().toJson(dirtyFields);
    }

    private Set<String> getDirtyFields() {
        if (this.mDirtyFields == null) {
            return new HashSet<>();
        } else {
            return new Gson().fromJson(this.mDirtyFields, Set.class);
        }
    }

    public boolean dirty(String fieldName) {
        return getDirtyFields().contains(fieldName);
    }

    void addDirtyField(String field) {
        Set<String> currentFields = getDirtyFields();
        currentFields.add(field);
        setDirtyFields(currentFields);
    }

    void removeDirtyField(String field) {
        Set<String> currentFields = getDirtyFields();
        currentFields.remove(field);
        setDirtyFields(currentFields);
    }

    public boolean isDirty() {
        return !getDirtyFields().isEmpty();
    }

    public String getTokenAuthHeaderString() {
        return "Token " + getToken();
    }
}
