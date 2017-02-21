package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.Identification;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * POJO helper for querying Identifications
 */
public class IdentificationDao {

    private static IdentificationDao instance = new IdentificationDao();

    private Dao<Identification, Integer> mIdentificationDao;

    private static synchronized IdentificationDao getInstance() {
        return instance;
    }

    private IdentificationDao() {
    }

    private void setIdentificationDao(Dao identificationDao) {
        this.mIdentificationDao = identificationDao;
    }

    private Dao<Identification, Integer> getIdentificationDao() throws SQLException {
        if (mIdentificationDao == null) {
            setIdentificationDao(DatabaseHelper.getHelper().getDao(Identification.class));
        }

        return mIdentificationDao;
    }

    public static void create(Identification identification) throws SQLException {
        getInstance().getIdentificationDao().create(identification);
    }

    public static List<Identification> find(Map<String,Object> queryMap) throws SQLException {
        return getInstance().getIdentificationDao().queryForFieldValues(queryMap);
    }
}
