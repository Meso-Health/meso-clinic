package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.EncounterItem;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * POJO helper for querying LineItems
 */
public class EncounterItemDao {

    private static EncounterItemDao instance = new EncounterItemDao();

    private Dao<EncounterItem, UUID> mLineItemDao;

    private static synchronized EncounterItemDao getInstance() {
        return instance;
    }

    private EncounterItemDao() {
    }

    private void setLineItemDao(Dao lineItemDao) {
        this.mLineItemDao = lineItemDao;
    }

    private Dao<EncounterItem, UUID> getLineItemDao() throws SQLException {
        if (mLineItemDao == null) {
            setLineItemDao(DatabaseHelper.getHelper().getDao(EncounterItem.class));
        }

        return mLineItemDao;
    }

    public static void create(EncounterItem encounterItem) throws SQLException {
        getInstance().getLineItemDao().create(encounterItem);
    }

    public static void create(List<EncounterItem> encounterItems) throws SQLException {
        getInstance().getLineItemDao().create(encounterItems);
    }

    public static EncounterItem findById(UUID id) throws SQLException {
        return getInstance().getLineItemDao().queryForId(id);
    }

    public static List<EncounterItem> all() throws SQLException {
        return getInstance().getLineItemDao().queryForAll();
    }
}
