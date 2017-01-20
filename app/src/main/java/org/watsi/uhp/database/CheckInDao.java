package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.CheckIn;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * POJO helper for querying CheckIns
 */
public class CheckInDao {

    private Dao<CheckIn, Integer> mCheckInDao;

    private static CheckInDao getInstance() {
        return new CheckInDao();
    }

    private CheckInDao() {
    }

    private void setMemberDao(Dao checkInDao) {
        this.mCheckInDao = checkInDao;
    }

    private Dao<CheckIn, Integer> getCheckInDao() throws SQLException {
        if (mCheckInDao == null) {
            setMemberDao(DatabaseHelper.getHelper().getDao(CheckIn.class));
        }

        return mCheckInDao;
    }

    public static void create(CheckIn checkIn) throws SQLException {
        getInstance().getCheckInDao().create(checkIn);
    }

    public static List<CheckIn> all() throws SQLException {
        return getInstance().getCheckInDao().queryForAll();
    }

    public static CheckIn findById(int checkInId) throws SQLException {
        return getInstance().getCheckInDao().queryForId(checkInId);
    }

    public static void update(CheckIn checkIn) throws SQLException {
        getInstance().getCheckInDao().update(checkIn);
    }

    public static List<CheckIn> find(Map<String,Object> queryMap) throws SQLException {
        return getInstance().getCheckInDao().queryForFieldValues(queryMap);
    }
}
