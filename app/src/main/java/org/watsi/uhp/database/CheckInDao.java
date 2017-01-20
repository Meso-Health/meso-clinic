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

    public static Dao<CheckIn, Integer> getCheckInDao() throws SQLException {
        return DatabaseHelper.getHelper().getCheckInDao();
    }

    public static void create(CheckIn checkIn) throws SQLException {
        getCheckInDao().create(checkIn);
    }

    public static List<CheckIn> all() throws SQLException {
        return getCheckInDao().queryForAll();
    }

    public static CheckIn findById(int checkInId) throws SQLException {
        return getCheckInDao().queryForId(checkInId);
    }

    public static void update(CheckIn checkIn) throws SQLException {
        getCheckInDao().update(checkIn);
    }

    public static List<CheckIn> find(Map<String,Object> queryMap) throws SQLException {
        return getCheckInDao().queryForFieldValues(queryMap);
    }
}
