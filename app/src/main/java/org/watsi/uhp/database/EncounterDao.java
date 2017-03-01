package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.sql.SQLException;
import java.util.HashMap;
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
        // TODO: put inside transaction
        encounter.setCreatedAt(Clock.getCurrentTime());
        getInstance().getEncounterDao().create(encounter);

        for (EncounterItem encounterItem : encounter.getEncounterItems()) {
            Billable billable = encounterItem.getBillable();
            if (billable.getId() == null) {
                billable.generateId();
                BillableDao.create(billable);
            }

            encounterItem.setEncounter(encounter);
            EncounterItemDao.create(encounterItem);
        }
    }

    public static void refresh(Encounter encounter) throws SQLException {
        getInstance().getEncounterDao().refresh(encounter);
    }

    public static List<Encounter> unsynced() throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(Encounter.FIELD_NAME_SYNCED, false);
        return getInstance().getEncounterDao().queryForFieldValues(queryMap);
    }

    public static void update(Encounter encounter) throws SQLException {
        getInstance().getEncounterDao().update(encounter);
    }

    public static void delete(Encounter encounter) throws SQLException {
        getInstance().getEncounterDao().delete(encounter);
    }
}
