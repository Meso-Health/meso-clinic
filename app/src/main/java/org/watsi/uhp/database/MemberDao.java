package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
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

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * POJO helper for querying Members
 */
public class MemberDao {

    private static MemberDao instance = new MemberDao();

    private Dao<Member, Integer> mMemberDao;
    private String mLastModifiedAtString;

    private static synchronized MemberDao getInstance() {
        return instance;
    }

    private MemberDao() {
    }

    private void setMemberDao(Dao memberDao) {
        this.mMemberDao = memberDao;
    }

    private Dao<Member, Integer> getMemberDao() throws SQLException {
        if (mMemberDao == null) {
            setMemberDao(DatabaseHelper.getHelper().getDao(Member.class));
        }

        return mMemberDao;
    }

    private String getLastModifiedAtString() {
        return mLastModifiedAtString;
    }

    public static void create(Member member) throws SQLException {
        getInstance().getMemberDao().create(member);
    }

    public static void create(List<Member> members) throws SQLException {
        getInstance().getMemberDao().create(members);
    }

    public static List<Member> all() throws SQLException {
        return getInstance().getMemberDao().queryForAll();
    }

    public static Member findById(String memberId) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("_id", memberId);
        return getInstance().getMemberDao().queryForFieldValues(queryMap).get(0);
    }

    public static List<Member> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put("full_name", name);
        return getInstance().getMemberDao().queryForFieldValues(queryMap);
    }

    public static void update(Member member) throws SQLException {
        getInstance().getMemberDao().update(member);
    }

    public static void refresh(Member member) throws SQLException {
        getInstance().getMemberDao().refresh(member);
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
            names.add(m.getName());
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

    public static List<Member> recentMembers() throws SQLException {
        // TODO: query for only recently checked-in members
        return getInstance().getMemberDao().queryForAll();
    }

    public static void clear() throws SQLException {
        TableUtils.clearTable(getInstance().getMemberDao().getConnectionSource(), Member.class);
    }

    public static String lastModifiedString() throws SQLException {
        return getInstance().getLastModifiedAtString();
    }

    public static void setLastModifiedAt(String lastModifiedAtString) {
        getInstance().mLastModifiedAtString = lastModifiedAtString;
    }
}
