package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.IdentificationEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * POJO helper for querying Identifications
 */
public class IdentificationEventDao {

    private static IdentificationEventDao instance = new IdentificationEventDao();

    private Dao<IdentificationEvent, UUID> mIdentificationDao;

    private static synchronized IdentificationEventDao getInstance() {
        return instance;
    }

    private IdentificationEventDao() {
    }

    private void setIdentificationDao(Dao identificationDao) {
        this.mIdentificationDao = identificationDao;
    }

    private Dao<IdentificationEvent, UUID> getIdentificationDao() throws SQLException {
        if (mIdentificationDao == null) {
            setIdentificationDao(DatabaseHelper.getHelper().getDao(IdentificationEvent.class));
        }

        return mIdentificationDao;
    }

    public static void create(IdentificationEvent identificationEvent) throws SQLException {
        identificationEvent.setCreatedAt(Clock.getCurrentTime());
        getInstance().getIdentificationDao().create(identificationEvent);
    }

    public static List<IdentificationEvent> unsynced() throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(IdentificationEvent.FIELD_NAME_SYNCED, false);
        return getInstance().getIdentificationDao().queryForFieldValues(queryMap);
    }

    public static void update(IdentificationEvent identificationEvent) throws SQLException {
        getInstance().getIdentificationDao().update(identificationEvent);
    }

    public static IdentificationEvent openCheckIn(UUID memberId) throws SQLException {
        String rawQuery = "SELECT identifications.id\n" +
                "FROM identifications\n" +
                "LEFT OUTER JOIN encounters ON encounters.identification_event_id = identifications.id\n" +
                "WHERE encounters.identification_event_id IS NULL\n" +
                "AND identifications.member_id = '" + memberId.toString() + "'\n" +
                "AND identifications.dismissed = 0";

        GenericRawResults<String> rawResults =
                getInstance().getIdentificationDao().queryRaw(rawQuery,
                        new RawRowMapper<String>() {
                            public String mapRow(String[] columnNames, String[] resultColumns) {
                                return resultColumns[0];
                            }
                        });

        String result = rawResults.getFirstResult();
        if (result == null) {
            return null;
        } else {
            return getInstance().getIdentificationDao().queryForId(UUID.fromString(result));
        }
    }

    public static void deleteById(Set<UUID> memberIdsToDelete) throws SQLException {
        for (UUID id : memberIdsToDelete) {
            getInstance().getIdentificationDao().deleteById(id);
        }
    }
}
