package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.models.CheckIn;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Member, Integer> mMemberDao;
    private Dao<CheckIn, Integer> mCheckInDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
     public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Member.class);
            TableUtils.createTable(connectionSource, CheckIn.class);
            Log.d("UHP", "onCreate database helper called");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // TODO: figure out better way to handle upgrades than drop/re-create
        Log.d("UHP", "onUpgrade database helper called");

        try {
            TableUtils.dropTable(connectionSource, Member.class, true);
            TableUtils.dropTable(connectionSource, CheckIn.class, true);
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

    public void setCheckInDao(Dao checkInDao) {
        this.mCheckInDao = checkInDao;
    }

    public Dao<CheckIn, Integer> getCheckInDao() throws SQLException {
        if (mCheckInDao == null) {
            setCheckInDao(getDao(CheckIn.class));
        }

        return mCheckInDao;
    }

    @Override
    public void close() {
        mMemberDao = null;
        mCheckInDao = null;
        super.close();
    }
}
