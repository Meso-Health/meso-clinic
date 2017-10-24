package org.watsi.uhp.models;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.managers.Clock;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Abstract class for specifying any fields and behavior that all models should share
 */
public abstract class AbstractModel<T extends AbstractModel<T,K>, K> implements Serializable {

    public static final String FIELD_NAME_CREATED_AT = "created_at";

    @DatabaseField(columnName = FIELD_NAME_CREATED_AT)
    private Date mCreatedAt;

    public Date getCreatedAt() {
        return this.mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.mCreatedAt = createdAt;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String fieldName, String reason) {
            super(fieldName + ": " + reason);
        }
    }

    public static <T, K> Dao<T,K> getDao(Class<T> clazz) throws SQLException {
        return (Dao<T, K>) DatabaseHelper.fetchDao(clazz);
    }

    Dao<T, K> getDao() throws SQLException {
        return (Dao<T, K>) getDao(getClass());
    }

    public static <K,T> T find(K id, Class<T> clazz) throws SQLException {
        return getDao(clazz).queryForId(id);
    }

    public boolean create() throws SQLException {
        setCreatedAt(Clock.getCurrentTime());
        return getDao().create((T) this) == 1;
    }

    public boolean update() throws SQLException {
        return getDao().update((T) this) == 1;
    }

    public boolean destroy() throws SQLException {
        return getDao().delete((T) this) == 1;
    }

    public boolean refresh() throws SQLException {
        return getDao().refresh((T) this) == 1;
    }

    public static <T> List<T> all(Class<T> clazz) throws SQLException {
        return getDao(clazz).queryForAll();
    }
}
