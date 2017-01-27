package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

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

    public static List<Billable> findByCategory(Billable.CategoryEnum category) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("category", category);
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }
}
