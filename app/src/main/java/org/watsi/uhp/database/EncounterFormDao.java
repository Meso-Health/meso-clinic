package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.EncounterForm;

import java.sql.SQLException;
import java.util.UUID;

/**
 * POJO helper for querying EncounterForms
 */
public class EncounterFormDao {

    private static EncounterFormDao instance = new EncounterFormDao();

    private Dao<EncounterForm, UUID> mEncounterFormDao;

    private static synchronized EncounterFormDao getInstance() {
        return instance;
    }

    private EncounterFormDao() {
    }

    private void setEncounterFormDao(Dao encounterFormDao) {
        this.mEncounterFormDao = encounterFormDao;
    }

    private Dao<EncounterForm, UUID> getEncounterFormDao() throws SQLException {
        if (mEncounterFormDao == null) {
            setEncounterFormDao(DatabaseHelper.getHelper().getDao(EncounterForm.class));
        }

        return mEncounterFormDao;
    }

    public static void create(EncounterForm encounterForm) throws SQLException {
        encounterForm.setCreatedAt(Clock.getCurrentTime());
        getInstance().getEncounterFormDao().create(encounterForm);
    }
}
