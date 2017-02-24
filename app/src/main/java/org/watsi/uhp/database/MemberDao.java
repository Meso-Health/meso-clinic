package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.table.TableUtils;

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

    private void setMemberDao(Dao memberDao) {
        this.mMemberDao = memberDao;
    }

    private Dao<Member, UUID> getMemberDao() throws SQLException {
        if (mMemberDao == null) {
            setMemberDao(DatabaseHelper.getHelper().getDao(Member.class));
        }

        return mMemberDao;
    }

    public static void create(List<Member> members) throws SQLException {
        getInstance().getMemberDao().create(members);
    }

    public static Member findById(UUID id) throws SQLException {
        return getInstance().getMemberDao().queryForId(id);
    }

    public static Member findByCardId(String cardId) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_CARD_ID, cardId);

        List<Member> results = getInstance().getMemberDao().queryForFieldValues(queryMap);
        if (results.size() == 0) { throw new SQLException("Record not found."); }
        return results.get(0);
    }

    public static List<Member> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Member.FIELD_NAME_FULL_NAME, name);
        return getInstance().getMemberDao().queryForFieldValues(queryMap);
    }

    public static void update(Member member) throws SQLException {
        getInstance().getMemberDao().update(member);
    }

    public static List<Member> withCardIdLike(String query) throws SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .where()
                .like(Member.FIELD_NAME_CARD_ID, "%" + query + "%")
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }

    public static Set<String> allUniqueMemberNames() throws SQLException {
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

    public static ArrayList<Member> fuzzySearchMembers(String query, int number, int threshold)
            throws SQLException {
        List<ExtractedResult> topMatchingNames =
                FuzzySearch.extractTop(query, allUniqueMemberNames(), number, threshold);

        ArrayList<Member> topMatchingMembers = new ArrayList<>();
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
                "   SELECT id, member_id, max(created_at) \n" +
                "   FROM identifications\n" +
                "   WHERE accepted = 1\n" +
                "   GROUP BY member_id\n" +
                ") last_identifications on last_identifications.member_id = members.id\n" +
                "LEFT OUTER JOIN encounters ON encounters.identification_id = last_identifications.id\n" +
                "WHERE encounters.identification_id IS NULL";

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
                .where()
                .eq(Member.FIELD_NAME_HOUSEHOLD_ID, householdId)
                .and()
                .not().eq(Member.FIELD_NAME_ID, memberId)
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }

    public static void clear() throws SQLException {
        TableUtils.clearTable(getInstance().getMemberDao().getConnectionSource(), Member.class);
    }

    public static List<Member> membersWithPhotosToFetch() throws SQLException {
        PreparedQuery<Member> pq = getInstance().getMemberDao()
                .queryBuilder()
                .where()
                .isNull(Member.FIELD_NAME_PHOTO)
                .and()
                .isNotNull(Member.FIELD_NAME_PHOTO_URL)
                .prepare();
        return getInstance().getMemberDao().query(pq);
    }
}
