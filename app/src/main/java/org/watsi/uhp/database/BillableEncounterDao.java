package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.BillableEncounter;

import java.sql.SQLException;

/**
 * POJO helper for querying BillableEncounters
 */
public class BillableEncounterDao {

    private static BillableEncounterDao instance = new BillableEncounterDao();

    private Dao<BillableEncounter, Integer> mBillableEncounterDao;

    private static synchronized BillableEncounterDao getInstance() {
        return instance;
    }

    private BillableEncounterDao() {
    }

    private void setBillableEncounterDao(Dao billableEncounterDao) {
        this.mBillableEncounterDao = billableEncounterDao;
    }

    private Dao<BillableEncounter, Integer> getBillableEncounterDao() throws SQLException {
        if (mBillableEncounterDao == null) {
            setBillableEncounterDao(DatabaseHelper.getHelper().getDao(BillableEncounter.class));
        }

        return mBillableEncounterDao;
    }

    public static void create(BillableEncounter billableEncounter) throws SQLException {
        getInstance().getBillableEncounterDao().create(billableEncounter);
    }
}
