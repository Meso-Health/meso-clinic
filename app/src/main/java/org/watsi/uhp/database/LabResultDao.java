package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.LabResult;

import java.sql.SQLException;
import java.util.UUID;

public class LabResultDao {
    private static Dao<LabResult, Integer> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(LabResult.class);
    }

    public static void create(LabResult labResult) throws SQLException {
        labResult.setCreatedAt(Clock.getCurrentTime());
        getDao().create(labResult);
    }

    public static LabResult findByEncounterItemId(UUID encounterItemId) throws SQLException {
        PreparedQuery<LabResult> pq = getDao()
                .queryBuilder()
                .where()
                .eq(LabResult.FIELD_NAME_ENCOUNTER_ITEM_ID, encounterItemId)
                .prepare();

        return getDao().queryForFirst(pq);
    }
}
