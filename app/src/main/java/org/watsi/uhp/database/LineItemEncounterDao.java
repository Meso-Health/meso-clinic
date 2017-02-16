package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.LineItemEncounter;

import java.sql.SQLException;

/**
 * POJO helper for querying LineItemEncounters
 */
public class LineItemEncounterDao {

    private static LineItemEncounterDao instance = new LineItemEncounterDao();

    private Dao<LineItemEncounter, Integer> mLineItemEncounterDao;

    private static synchronized LineItemEncounterDao getInstance() {
        return instance;
    }

    private LineItemEncounterDao() {
    }

    private void setLineItemEncounterDao(Dao LineItemEncounterDao) {
        this.mLineItemEncounterDao = LineItemEncounterDao;
    }

    private Dao<LineItemEncounter, Integer> getLineItemEncounterDao() throws SQLException {
        if (mLineItemEncounterDao == null) {
            setLineItemEncounterDao(DatabaseHelper.getHelper().getDao(LineItemEncounter.class));
        }

        return mLineItemEncounterDao;
    }

    public static void create(LineItemEncounter lineItemEncounter) throws SQLException {
        getInstance().getLineItemEncounterDao().create(lineItemEncounter);
    }
}
