package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;

/**
 * Abstract class for specifying any fields and behavior that all
 * models that need to be synced to the back-end should share
 */
public abstract class SyncableModel extends AbstractModel {

    public static final String FIELD_NAME_TOKEN = "token";
    public static final String FIELD_NAME_SYNCED = "synced";

    @DatabaseField(columnName = FIELD_NAME_TOKEN)
    private String mToken;

    @DatabaseField(columnName = FIELD_NAME_SYNCED, defaultValue = "false", canBeNull = false)
    private boolean mSynced;

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public boolean isSynced() {
        return mSynced;
    }

    public void setSynced(boolean synced) {
        this.mSynced = synced;
    }

    public String getTokenAuthHeaderString() {
        return "Token " + getToken();
    }
}
