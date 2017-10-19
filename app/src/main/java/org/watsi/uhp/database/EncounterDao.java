package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.models.Encounter;

import java.sql.SQLException;
import java.util.UUID;

/**
 * POJO helper for querying Encounters
 */
public class EncounterDao {

    private static Dao<Encounter, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(Encounter.class);
    }

    public static Encounter find(UUID id) throws SQLException {
        return getDao().queryForId(id);
    }
}
