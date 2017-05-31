package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.Encounter;

import java.sql.SQLException;
import java.util.UUID;

/**
 * POJO helper for querying Encounters
 */
public class EncounterDao {

    private static EncounterDao instance = new EncounterDao();

    private Dao<Encounter, UUID> mEncounterDao;

    private static synchronized EncounterDao getInstance() {
        return instance;
    }

    private EncounterDao() {
    }

    private void setEncounterDao(Dao encounterDao) {
        this.mEncounterDao = encounterDao;
    }

    private Dao<Encounter, UUID> getEncounterDao() throws SQLException {
        if (mEncounterDao == null) {
            setEncounterDao(DatabaseHelper.getHelper().getDao(Encounter.class));
        }

        return mEncounterDao;
    }

    public static Encounter find(UUID id) throws SQLException {
        return getInstance().getEncounterDao().queryForId(id);
    }
}
