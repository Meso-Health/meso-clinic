package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

public class MemberDao {

    private static Dao<Member, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(Member.class);
    }

    public static Member findByCardId(String cardId) throws SQLException {
        cardId = cardId.replaceAll(" ","");
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_CARD_ID, cardId);
        List<Member> results = getDao().queryForFieldValues(queryMap);
        if (results.size() == 0) { throw new SQLException("Record not found."); }
        return results.get(0);
    }

    private static List<Member> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_FULL_NAME, new SelectArg(name));
        return getDao().queryForFieldValues(queryMap);
    }

    public static List<Member> withCardIdLike(String query) throws SQLException {
        query = query.replaceAll(" ","");
        PreparedQuery<Member> pq = getDao()
                .queryBuilder()
                .where()
                .like(Member.FIELD_NAME_CARD_ID, "%" + query + "%")
                .prepare();
        return getDao().query(pq);
    }

    private static Set<String> allUniqueMemberNames() throws SQLException {
        PreparedQuery<Member> pq = getDao()
                .queryBuilder()
                .selectColumns(Member.FIELD_NAME_FULL_NAME)
                .prepare();

        List<Member> members = getDao().query(pq);
        List<String> names = new ArrayList<>();
        for (Member m : members) {
            names.add(m.getFullName());
        }
        return new HashSet<>(names);
    }

    public static List<Member> fuzzySearchMembers(String query)
            throws SQLException {
        Set<String> allUniqueNames = allUniqueMemberNames();
        List<ExtractedResult> topMatchingNames = FuzzySearch.extractTop(query, allUniqueNames, 20, 60);
        List<Member> topMatchingMembers = new ArrayList<>();
        for (ExtractedResult result : topMatchingNames) {
            topMatchingMembers.addAll(findByName(result.getString()));
        }
        return topMatchingMembers;
    }

    public static List<Member> getCheckedInMembers() throws SQLException {
        String rawQuery = "SELECT members.id\n" +
                "FROM members\n" +
                "INNER JOIN (\n" +
                "   SELECT id, member_id, max(occurred_at) AS occurred_at\n" +
                "   FROM identifications\n" +
                "   WHERE accepted = 1\n" +
                "   AND dismissed = 0\n" +
                "   GROUP BY member_id\n" +
                ") last_identifications on last_identifications.member_id = members.id\n" +
                "LEFT OUTER JOIN encounters ON encounters.identification_event_id = last_identifications.id\n" +
                "WHERE encounters.identification_event_id IS NULL\n" +
                "ORDER BY last_identifications.occurred_at";

        GenericRawResults<String> rawResults =
                getDao().queryRaw(rawQuery,
                        new RawRowMapper<String>() {
                            public String mapRow(String[] columnNames, String[] resultColumns) {
                                return resultColumns[0];
                            }
                        });

        List<Member> members = new ArrayList<>();
        for (String id : rawResults.getResults()) {
            members.add(getDao().queryForId(UUID.fromString(id)));
        }
        return members;
    }

    public static List<Member> getRemainingHouseholdMembers(UUID householdId, UUID memberId) throws
            SQLException {
        PreparedQuery<Member> pq = getDao()
                .queryBuilder()
                .orderBy(Member.FIELD_NAME_AGE, false)
                .where()
                .eq(Member.FIELD_NAME_HOUSEHOLD_ID, householdId)
                .and()
                .not().eq(Member.FIELD_NAME_ID, memberId)
                .prepare();
        return getDao().query(pq);
    }

    public static List<Member> membersWithPhotosToFetch() throws SQLException {
        PreparedQuery<Member> pq = getDao()
                .queryBuilder()
                .where()
                .isNull(Member.FIELD_NAME_CROPPED_PHOTO_BYTES)
                .and()
                .isNotNull(Member.FIELD_NAME_REMOTE_MEMBER_PHOTO_URL)
                .prepare();
        return getDao().query(pq);
    }

    public static Set<UUID> allMemberIds() throws SQLException {
        PreparedQuery<Member> pq = getDao()
                .queryBuilder()
                .selectColumns(Member.FIELD_NAME_ID)
                .prepare();

        List<Member> members = getDao().query(pq);
        Set<UUID> ids = new HashSet<>();
        for (Member m : members) {
            ids.add(m.getId());
        }
        return ids;
    }
}
