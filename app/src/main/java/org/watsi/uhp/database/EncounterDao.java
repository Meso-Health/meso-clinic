package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.Encounter;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * POJO helper for querying Encounters
 */
public class EncounterDao {

    private static EncounterDao instance = new EncounterDao();

    private Dao<Encounter, Integer> mEncounterDao;

    private static EncounterDao getInstance() {
        return instance;
    }

    private EncounterDao() {
    }

    private void setEncounterDao(Dao encounterDao) {
        this.mEncounterDao = encounterDao;
    }

    private Dao<Encounter, Integer> getEncounterDao() throws SQLException {
        if (mEncounterDao == null) {
            setEncounterDao(DatabaseHelper.getHelper().getDao(Encounter.class));
        }

        return mEncounterDao;
    }

    public static void create(Encounter encounter) throws SQLException {
        getInstance().getEncounterDao().create(encounter);
    }

    public static List<Encounter> find(Map<String,Object> queryMap) throws SQLException {
        return getInstance().getEncounterDao().queryForFieldValues(queryMap);
    }
}
