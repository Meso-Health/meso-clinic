package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

import org.watsi.uhp.managers.Clock;
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

/**
 * POJO helper for querying Members
 */
public class MemberDao {

    private static MemberDao instance = new MemberDao();

    private Dao<Member, UUID> mMemberDao;

    private static synchronized MemberDao getInstance() {
        return instance;
    }

    private MemberDao() {
    }

    public static void create(Member member) throws SQLException {
        member.setCreatedAt(Clock.getCurrentTime());
        getInstance().getMemberDao().create(member);
    }

    private void setMemberDao(Dao memberDao) {
        this.mMemberDao = memberDao;
    }

    private Dao<Member, UUID> getMemberDao() throws SQLException {
        if (mMemberDao == null) {
            setMemberDao(DatabaseHelper.getHelper().getDao(Member.class));
        }

        return mMemberDao;
    }

    public static Member findById(UUID id) throws SQLException {
        return getInstance().getMemberDao().queryForId(id);
    }

    public static Member findByCardId(String cardId) throws SQLException {
        cardId = cardId.replaceAll(" ","");
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_CARD_ID, cardId);
        List<Member> results = getInstance().getMemberDao().queryForFieldValues(queryMap);
        if (results.size() == 0) { throw new SQLException("Record not found."); }
        return results.get(0);
    }

    private static List<Member> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_FULL_NAME, new SelectArg(name));
        return getInstance().getMemberDao().queryForFieldValues(queryMap);
    }

    public static List<Member> withCardIdLike(String query) throws SQLException {
        query = query.replaceAll(" ","");
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .where()
                .like(Member.FIELD_NAME_CARD_ID, "%" + query + "%")
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }

    private static Set<String> allUniqueMemberNames() throws SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .selectColumns(Member.FIELD_NAME_FULL_NAME)
                .prepare();

        List<Member> members = getInstance().getMemberDao().query(pq);
        List<String> names = new ArrayList<>();
        for (Member m : members) {
            names.add(m.getFullName());
        }
        return new HashSet<>(names);
    }

    public static List<Member> fuzzySearchMembers(String query, int number, int threshold)
            throws SQLException {
        List<ExtractedResult> topMatchingNames =
                FuzzySearch.extractTop(query, allUniqueMemberNames(), number, threshold);

        List<Member> topMatchingMembers = new ArrayList<>();
        for (ExtractedResult result : topMatchingNames) {
            String name = result.getString();
            topMatchingMembers.addAll(findByName(name));
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
                getInstance().getMemberDao().queryRaw(rawQuery,
                        new RawRowMapper<String>() {
                            public String mapRow(String[] columnNames, String[] resultColumns) {
                                return resultColumns[0];
                            }
                        });

        List<Member> members = new ArrayList<>();
        for (String id : rawResults.getResults()) {
            members.add(findById(UUID.fromString(id)));
        }
        return members;
    }

    public static List<Member> getRemainingHouseholdMembers(UUID householdId, UUID memberId) throws
            SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .orderBy(Member.FIELD_NAME_AGE, false)
                .where()
                .eq(Member.FIELD_NAME_HOUSEHOLD_ID, householdId)
                .and()
                .not().eq(Member.FIELD_NAME_ID, memberId)
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }

    public static List<Member> membersWithPhotosToFetch() throws SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .where()
                .isNull(Member.FIELD_NAME_CROPPED_PHOTO_BYTES)
                .and()
                .isNotNull(Member.FIELD_NAME_REMOTE_MEMBER_PHOTO_URL)
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }

    public static Set<UUID> allMemberIds() throws SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .selectColumns(Member.FIELD_NAME_ID)
                .prepare();

        List<Member> members = getInstance().getMemberDao().query(pq);
        Set<UUID> ids = new HashSet<>();
        for (Member m : members) {
            ids.add(m.getId());
        }
        return ids;
    }

    //TODO: move to a base Dao class
    public static List<Member> all() throws SQLException {
        return getInstance().getMemberDao().queryForAll();
    }
}
