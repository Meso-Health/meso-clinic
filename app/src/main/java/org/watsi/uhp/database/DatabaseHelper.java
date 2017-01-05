package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Member, Integer> mMemberDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
     public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Member.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO: figure out better way to handle upgrades than drop/re-create
        try {
            TableUtils.dropTable(connectionSource, Member.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMemberDao(Dao memberDao) {
        this.mMemberDao = memberDao;
    }

    public Dao<Member, Integer> getMemberDao() throws SQLException {
        if (mMemberDao == null) {
            setMemberDao(getDao(Member.class));
        }

        return mMemberDao;
    }

    @Override
    public void close() {
        mMemberDao = null;

        super.close();
    }
}
