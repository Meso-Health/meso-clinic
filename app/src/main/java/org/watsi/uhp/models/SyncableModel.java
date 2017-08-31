package org.watsi.uhp.models;

import android.content.Context;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.database.DatabaseHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, id = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_TOKEN)
    private String mToken;

    @DatabaseField(columnName = FIELD_NAME_DIRTY_FIELDS, defaultValue = "[]", canBeNull = false)
    private String mDirtyFields = "[]";

    public SyncableModel refresh() throws SQLException {
        return getDao().queryForId(getId());
    }

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

    protected static <K> Dao<K,UUID> getDao(Class<K> clazz) throws SQLException {
        return (Dao<K, UUID>) DatabaseHelper.getHelper().getDao(clazz);
    }

    Dao<T, UUID> getDao() throws SQLException {
        return (Dao<T, UUID>) getDao(getClass());
    }

    public boolean isSynced() throws SQLException {
        return !isNew() && getDirtyFields().isEmpty();
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

    public void saveChanges(String token) throws SQLException, ValidationException {
        setToken(token);
        if (this.mId == null) this.mId = UUID.randomUUID();
        validate();
        setDirtyFields(changedFields());
        getDao().createOrUpdate((T) this);
        persistAssociations();
    }

    public void updateFromSync(Response<T> response) throws SQLException {
        T responseBody = response.body();
        handleUpdateFromSync(responseBody);
        setDirtyFields(diffFields(responseBody));
        getDao().createOrUpdate((T) this);
    }

    public void delete() throws SQLException {
        getDao().delete((T) this);
    }

    public Response<T> sync(Context context) throws SyncException, SQLException, IOException {
        if (getId() == null) throw new SyncException("Attempted to sync model with no ID set");
        if (!isDirty()) throw new SyncException("Attempted to sync model " + getId().toString() + " with no dirty fields");
        if (getToken() == null) throw new SyncException("Attempted to sync model " + getId().toString() + " with no API token");

        if (isNew()) {
            return postApiCall(context).execute();
        } else {
            return patchApiCall(context).execute();
        }
    }

    public static <K> List<K> unsynced(Class<K> clazz) throws SQLException {
        Dao<K, UUID> dao = getDao(clazz);
        PreparedQuery<K> preparedQuery = dao.queryBuilder().where()
                .not().eq(SyncableModel.FIELD_NAME_DIRTY_FIELDS, "[]")
                .prepare();
        return dao.query(preparedQuery);
    }

    public abstract void validate() throws ValidationException;
    public abstract void handleUpdateFromSync(T response);
    protected abstract Call<T> postApiCall(Context context) throws SQLException, SyncException;
    protected abstract Call<T> patchApiCall(Context context) throws SQLException, SyncException;
    protected abstract void persistAssociations() throws SQLException, ValidationException;

    public static class SyncException extends Exception {
        SyncException(String message) { super(message); }
    }

    public static class UnauthenticatedException extends Exception {
        UnauthenticatedException() {
            super("Current user is not authenticated");
        }
    }
}
