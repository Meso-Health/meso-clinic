package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.LineItem;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * POJO helper for querying LineItems
 */
public class LineItemDao {

    private static LineItemDao instance = new LineItemDao();

    private Dao<LineItem, UUID> mLineItemDao;

    private static synchronized LineItemDao getInstance() {
        return instance;
    }

    private LineItemDao() {
    }

    private void setLineItemDao(Dao lineItemDao) {
        this.mLineItemDao = lineItemDao;
    }

    private Dao<LineItem, UUID> getLineItemDao() throws SQLException {
        if (mLineItemDao == null) {
            setLineItemDao(DatabaseHelper.getHelper().getDao(LineItem.class));
        }

        return mLineItemDao;
    }

    public static void create(LineItem lineItem) throws SQLException {
        getInstance().getLineItemDao().create(lineItem);
    }

    public static void create(List<LineItem> lineItems) throws SQLException {
        getInstance().getLineItemDao().create(lineItems);
    }

    public static LineItem findById(UUID id) throws SQLException {
        return getInstance().getLineItemDao().queryForId(id);
    }

    public static List<LineItem> all() throws SQLException {
        return getInstance().getLineItemDao().queryForAll();
    }
}
