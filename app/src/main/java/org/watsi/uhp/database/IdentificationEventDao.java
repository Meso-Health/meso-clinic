package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.IdentificationEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * POJO helper for querying Identifications
 */
public class IdentificationEventDao {

    private static IdentificationEventDao instance = new IdentificationEventDao();

    private Dao<IdentificationEvent, UUID> mIdentificationDao;

    private static synchronized IdentificationEventDao getInstance() {
        return instance;
    }

    private IdentificationEventDao() {
    }

    private void setIdentificationDao(Dao identificationDao) {
        this.mIdentificationDao = identificationDao;
    }

    private Dao<IdentificationEvent, UUID> getIdentificationDao() throws SQLException {
        if (mIdentificationDao == null) {
            setIdentificationDao(DatabaseHelper.getHelper().getDao(IdentificationEvent.class));
        }

        return mIdentificationDao;
    }

    public static void create(IdentificationEvent identificationEvent) throws SQLException {
        getInstance().getIdentificationDao().create(identificationEvent);
    }

    public static List<IdentificationEvent> find(Map<String,Object> queryMap) throws SQLException {
        return getInstance().getIdentificationDao().queryForFieldValues(queryMap);
    }

    public static IdentificationEvent findById(UUID id) throws SQLException {
        return getInstance().getIdentificationDao().queryForId(id);
    }

    public static List<IdentificationEvent> all() throws SQLException {
        return getInstance().getIdentificationDao().queryForAll();
    }
}
