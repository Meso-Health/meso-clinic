package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
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

    public static List<Billable> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("name", name);
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }

    public static List<Billable> withNameLike(String query) throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .where()
                .like(Billable.FIELD_NAME_NAME, "%" + query + "%")
                .prepare();
        return getInstance().getBillableDao().query(pq);
    }

    public static List<Billable> allBillables() throws SQLException {
        return getInstance().getBillableDao().queryForAll();
    }

    public static List<Billable> allNames() throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_NAME)
                .prepare();

        return getInstance().getBillableDao().query(pq);
    }
}
