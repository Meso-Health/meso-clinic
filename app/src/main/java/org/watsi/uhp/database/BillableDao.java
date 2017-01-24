package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.List;

/**
 * POJO helper for querying Billables
 */
public class BillableDao {

    private static BillableDao instance = new BillableDao();

    private Dao<Billable, Integer> mBillableDao;

    private static BillableDao getInstance() {
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

    public static List<Billable> all() throws SQLException {
        return getInstance().getBillableDao().queryForAll();
    }

    public static void create(List<Billable> billables) throws SQLException {
        getInstance().getBillableDao().create(billables);
    }

    public static List<Billable> findByDepartmentAndCategory(Billable.DepartmentEnum department, Billable.CategoryEnum category) throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .where()
                .eq(Billable.FIELD_NAME_CATEGORY, category)
                .and()
                .in(Billable.FIELD_NAME_DEPARTMENT, department, Billable.DepartmentEnum.UNSPECIFIED)
                .prepare();
        return getInstance().getBillableDao().query(pq);
    }
}
