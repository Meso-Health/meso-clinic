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

public class EncounterItemDao {

    private static Dao<EncounterItem, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(EncounterItem.class);
    }

    public static List<EncounterItem> fromEncounter(Encounter encounter) throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(EncounterItem.FIELD_NAME_ENCOUNTER_ID, encounter.getId());
        List<EncounterItem> encounterItems = getDao().queryForFieldValues(queryMap);
        for (EncounterItem encounterItem : encounterItems) {
            encounterItem.setEncounterId(encounterItem.getEncounter().getId());
            encounterItem.getBillable().refresh();
        }
        return encounterItems;
    }

    public static List<EncounterItem> getDefaultEncounterItems(
            IdentificationEvent.ClinicNumberTypeEnum type) throws SQLException {
        List<EncounterItem> defaultLineItems = new ArrayList<>();

        if (type == IdentificationEvent.ClinicNumberTypeEnum.OPD) {
            defaultLineItems.add(new EncounterItem(BillableDao.findByName("Consultation").get(0), 1));
            defaultLineItems.add(new EncounterItem(BillableDao.findByName("Medical Form").get(0), 1));
        }

        return defaultLineItems;
    }
}
