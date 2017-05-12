package org.watsi.uhp.models;

import android.content.Context;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.database.DatabaseHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Abstract class for specifying any fields and behavior that all
 * models that need to be synced to the back-end should share
 */
public abstract class SyncableModel<T extends SyncableModel<T>> extends AbstractModel {

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_TOKEN = "token";
    public static final String FIELD_NAME_DIRTY_FIELDS = "dirty_fields";

    protected static final Set<String> SYNCABLE_DIFF_IGNORE_FIELDS = Sets.newHashSet(new String[]{
            "mToken","mDirtyFields"});

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_TOKEN)
    private String mToken;

    @DatabaseField(columnName = FIELD_NAME_DIRTY_FIELDS, defaultValue = "[]", canBeNull = false)
    private String mDirtyFields = "[]";

    public UUID getId() {
        return this.mId;
    }

    protected void setId(UUID id) {
        this.mId = id;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

    public boolean isSynced() throws SQLException {
        return !isNew() && getDirtyFields().isEmpty();
    }

    private void setDirtyFields(Set<String> dirtyFields) {
        this.mDirtyFields = new Gson().toJson(dirtyFields);
    }

    protected Set<String> getDirtyFields() {
        if (this.mDirtyFields == null) {
            return new HashSet<>();
        } else {
            return new Gson().fromJson(this.mDirtyFields, Set.class);
        }
    }

    protected Dao<T,UUID> getDao() throws SQLException {
        return (Dao<T, UUID>) DatabaseHelper.getHelper().getDao(this.getClass());
    }

    public Boolean isNew() throws SQLException {
        return getId() == null || dirty(FIELD_NAME_ID) ||
                (getId() != null && !getDao().idExists(getId()));
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

    Set<String> changedFields() throws SQLException {
        if (isNew()) {
            return diffFields(null);
        } else {
            return diffFields(getDao().queryForId(getId()));
        }
    }

    Set<String> diffFields(T refModel) {
        Gson gson = new Gson();
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
        diffSet.removeAll(diffIgnoreFields());
        return diffSet;
    }

    public void saveChanges(String token) throws SQLException, UnauthenticatedException {
        if (token == null) {
            throw new UnauthenticatedException();
        }
        setToken(token);
        if (this.mId == null) this.mId = UUID.randomUUID();
        setDirtyFields(changedFields());
        getDao().createOrUpdate((T) this);
        persistAssociations();
    }

    public void updateFromSync() throws SQLException {
        handleUpdate();
        clearDirtyFields();
        getDao().createOrUpdate((T) this);
    }

    public Response<T> sync(Context context)
            throws UnauthenticatedException, SyncException, SQLException, IOException {
        if (getToken() == null) {
            throw new UnauthenticatedException();
        }
        if (!isDirty()) {
            throw new SyncException();
        }
        if (isNew()) {
            return postApiCall(context, getToken(), BuildConfig.PROVIDER_ID, this).execute();
        } else {
            return patchApiCall(context, getToken(), getId(), patchRequestBody(context)).execute();
        }
    }

    protected Set<String> diffIgnoreFields() {
        return SYNCABLE_DIFF_IGNORE_FIELDS;
    }

    public abstract void handleUpdate();
    protected abstract Map<String, RequestBody> patchRequestBody(Context context);
    protected abstract Call<T> postApiCall(
            Context context, String token, int providerId, SyncableModel<T> model);
    protected abstract Call<T> patchApiCall(
            Context context, String token, UUID id, Map<String, RequestBody> params);
    protected abstract void persistAssociations();

    private static class SyncException extends Exception {
        SyncException() {
            super("Model does not have any fields that need to be synced");
        }
    }

    public static class UnauthenticatedException extends Exception {
        UnauthenticatedException() {
            super("Current user is not authenticated");
        }
    }
}
