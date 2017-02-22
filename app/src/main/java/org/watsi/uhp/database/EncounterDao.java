package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LineItem;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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

    public static void create(Encounter encounter) throws SQLException {
        encounter.setCreatedAt(Clock.getCurrentTime());
        //TODO: put inside transaction
        getInstance().getEncounterDao().create(encounter);

        for (LineItem lineItem : encounter.getLineItems()) {
            lineItem.setEncounter(encounter);
            LineItemDao.create(lineItem);
        }
    }

    public static List<Encounter> find(Map<String,Object> queryMap) throws SQLException {
        return getInstance().getEncounterDao().queryForFieldValues(queryMap);
    }

    public static Encounter findById(UUID id) throws SQLException {
        return getInstance().getEncounterDao().queryForId(id);
    }

    public static List<Encounter> all() throws SQLException {
        return getInstance().getEncounterDao().queryForAll();
    }
}
