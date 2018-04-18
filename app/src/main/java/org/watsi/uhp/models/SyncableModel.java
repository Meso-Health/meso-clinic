package org.watsi.uhp.models;

import android.content.Context;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;

/**
 * Abstract class for specifying any fields and behavior that all
 * models that need to be synced to the back-end should share
 */
public abstract class SyncableModel<T extends SyncableModel<T>> extends AbstractModel<T, UUID> {

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_TOKEN = "token";
    public static final String FIELD_NAME_DIRTY_FIELDS = "dirty_fields";

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, id = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_TOKEN)
    private String mToken;

    @DatabaseField(columnName = FIELD_NAME_DIRTY_FIELDS, defaultValue = "[]", canBeNull = false, index = true)
    private String mDirtyFields = "[]";

    public UUID getId() {
        return this.mId;
    }

    public void setId(UUID id) {
        this.mId = id;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    protected void setDirtyFields(Set<String> dirtyFields) {
        this.mDirtyFields = new Gson().toJson(dirtyFields);
    }

    protected Set<String> getDirtyFields() {
        if (this.mDirtyFields == null) {
            return new HashSet<>();
        } else {
            return new Gson().fromJson(this.mDirtyFields, Set.class);
        }
    }

    boolean dirty(String fieldName) {
        return getDirtyFields().contains(fieldName);
    }

    boolean isDirty() {
        return !getDirtyFields().isEmpty();
    }

    void clearDirtyFields() {
        setDirtyFields(new HashSet<String>());
    }

    public String getTokenAuthHeaderString() {
        return "Token " + getToken();
    }

    Set<String> diffFields(T refModel) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Map<String, Object> selfMap = gson.fromJson(gson.toJson(this), Map.class);
        Map<String, Object> refMap;
        if (refModel == null) {
            refMap = new HashMap<>();
        } else {
            refMap = gson.fromJson(gson.toJson(refModel), Map.class);
        }
        MapDifference<String, Object> diff = Maps.difference(selfMap, refMap);

        Set<String> diffSet = new HashSet<>();
        diffSet.addAll(diff.entriesDiffering().keySet());
        diffSet.addAll(diff.entriesOnlyOnLeft().keySet());
        diffSet.addAll(diff.entriesOnlyOnRight().keySet());
        return diffSet;
    }

    public abstract void validate() throws ValidationException;
    protected abstract Call<T> postApiCall(Context context) throws SQLException, SyncException;
    protected abstract Call<T> patchApiCall(Context context) throws SQLException, SyncException;

    public static class SyncException extends Exception {
        SyncException(String message) { super(message); }
    }

    public static class UnauthenticatedException extends Exception {
        UnauthenticatedException() {
            super("Current user is not authenticated");
        }
    }
}
