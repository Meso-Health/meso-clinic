package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

import org.watsi.uhp.models.IdentificationEvent;

import java.sql.SQLException;
import java.util.UUID;

public class IdentificationEventDao {

    private static Dao<IdentificationEvent, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(IdentificationEvent.class);
    }

    public static IdentificationEvent openCheckIn(UUID memberId) throws SQLException {
        String rawQuery = "SELECT identifications.id\n" +
                "FROM identifications\n" +
                "LEFT OUTER JOIN encounters ON encounters.identification_event_id = identifications.id\n" +
                "WHERE encounters.identification_event_id IS NULL\n" +
                "AND identifications.member_id = '" + memberId.toString() + "'\n" +
                "AND identifications.dismissed = 0 " +
                "AND identifications.accepted = 1";

        GenericRawResults<String> rawResults =
                getDao().queryRaw(rawQuery,
                        new RawRowMapper<String>() {
                            public String mapRow(String[] columnNames, String[] resultColumns) {
                                return resultColumns[0];
                            }
                        });

        String result = rawResults.getFirstResult();
        return (result == null) ? null : getDao().queryForId(UUID.fromString(result));
    }
}
