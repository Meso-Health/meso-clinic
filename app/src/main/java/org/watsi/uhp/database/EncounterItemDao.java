package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * POJO helper for querying LineItems
 */
public class EncounterItemDao {

    private static EncounterItemDao instance = new EncounterItemDao();

    private Dao<EncounterItem, UUID> mEncounterItemDao;

    private static synchronized EncounterItemDao getInstance() {
        return instance;
    }

    private EncounterItemDao() {
    }

    private void setEncounterItemDao(Dao encounterItemDao) {
        this.mEncounterItemDao = encounterItemDao;
    }

    private Dao<EncounterItem, UUID> getEncounterItemDao() throws SQLException {
        if (mEncounterItemDao == null) {
            setEncounterItemDao(DatabaseHelper.getHelper().getDao(EncounterItem.class));
        }

        return mEncounterItemDao;
    }

    public static void create(EncounterItem encounterItem) throws SQLException {
        encounterItem.setCreatedAt(Clock.getCurrentTime());
        getInstance().getEncounterItemDao().create(encounterItem);
    }

    public static List<EncounterItem> fromEncounter(Encounter encounter) throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(EncounterItem.FIELD_NAME_ENCOUNTER_ID, encounter.getId());
        List<EncounterItem> encounterItems =
                getInstance().getEncounterItemDao().queryForFieldValues(queryMap);
        for (EncounterItem encounterItem : encounterItems) {
            encounterItem.setEncounterId(encounterItem.getEncounter().getId());
            BillableDao.refresh(encounterItem.getBillable());
        }
        return encounterItems;
    }

    public static ArrayList<EncounterItem> getDefaultEncounterItems(
            IdentificationEvent.ClinicNumberTypeEnum type) throws SQLException {
        ArrayList<EncounterItem> defaultLineItems = new ArrayList<>();

        if (type == IdentificationEvent.ClinicNumberTypeEnum.OPD) {
            defaultLineItems.add(new EncounterItem(BillableDao.findByName("Consultation").get(0), 1));
            defaultLineItems.add(new EncounterItem(BillableDao.findByName("Medical Form").get(0), 1));
        }

        return defaultLineItems;
    }
}
