package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

/**
 * POJO helper for querying Members
 */
public class MemberDao {

    private static Dao<Member, Integer> getMemberDao() throws SQLException {
        return DatabaseHelper.getHelper().getMemberDao();
    }

    public static void create(Member member) throws SQLException {
        getMemberDao().create(member);
    }

    public static void create(List<Member> members) throws SQLException {
        getMemberDao().create(members);
    }

    public static List<Member> all() throws SQLException {
        return getMemberDao().queryForAll();
    }

    public static Member findById(int memberId) throws SQLException {
        return getMemberDao().queryForId(memberId);
    }

    public static void update(Member member) throws SQLException {
        getMemberDao().update(member);
    }

    public static void refresh(Member member) throws SQLException {
        getMemberDao().refresh(member);
    }

    public static List<Member> withNameLike(String query) throws SQLException {
        PreparedQuery<Member> pq = getMemberDao()
                .queryBuilder()
                .where()
                .like(Member.FIELD_NAME_NAME, "%" + query + "%")
                .prepare();
        return getMemberDao().query(pq);
    }
}
