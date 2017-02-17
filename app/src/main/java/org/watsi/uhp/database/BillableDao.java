package org.watsi.uhp.database;

import android.database.Cursor;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POJO helper for querying Billables
 */
public class BillableDao {

    private static BillableDao instance = new BillableDao();

    private Dao<Billable, Integer> mBillableDao;

    private static synchronized BillableDao getInstance() {
        return instance;
    }

    private BillableDao() {
    }

    private void setBillableDao(Dao billableDao) {
        this.mBillableDao = billableDao;
    }

    private Dao<Billable, Integer> getBillableDao() throws SQLException {
        if (mBillableDao == null) {
            setBillableDao(DatabaseHelper.getHelper().getDao(Billable.class));
        }

        return mBillableDao;
    }

    public static void create(List<Billable> billables) throws SQLException {
        getInstance().getBillableDao().create(billables);
    }

    public static Billable findById(String billableId) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("_id", billableId);
        return getInstance().getBillableDao().queryForFieldValues(queryMap).get(0);
    }

    public static List<Billable> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("name", name);
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }

    public static List<Billable> allDrugNames() throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_CATEGORY, Billable.CategoryEnum.DRUGS)
                .prepare();

        return getInstance().getBillableDao().query(pq);
    }

    public static Cursor getBillablesByCategoryCursor(Billable.CategoryEnum category) throws
            SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_ID, Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_CATEGORY, category)
                .prepare();

        CloseableIterator<Billable> iterator = getInstance().getBillableDao().iterator(pq);
        AndroidDatabaseResults results = (AndroidDatabaseResults)iterator.getRawResults();
        return results.getRawCursor();
    }
}
