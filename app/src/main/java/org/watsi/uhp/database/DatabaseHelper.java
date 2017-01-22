package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.BillableEncounter;
import org.watsi.uhp.models.CheckIn;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getHelper() {
        if (instance == null) {
            throw new RuntimeException("Must initialize DatabaseHelper before acquiring instance");
        }
        return instance;
    }

    public static void init(Context context) {
        instance = new DatabaseHelper(context);
    }

    @Override
     public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Member.class);
            TableUtils.createTable(connectionSource, CheckIn.class);
            TableUtils.createTable(connectionSource, Billable.class);
            TableUtils.createTable(connectionSource, Encounter.class);
            TableUtils.createTable(connectionSource, BillableEncounter.class);
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
            TableUtils.dropTable(connectionSource, Billable.class, true);
            TableUtils.dropTable(connectionSource, Encounter.class, true);
            TableUtils.dropTable(connectionSource, BillableEncounter.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void seedDb(Context context) throws SQLException, IOException {
        TableUtils.clearTable(getConnectionSource(), Member.class);

        List<Member> newMembers = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        for (int i = 0; i < 10; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            member.setBirthdate(new Date(cal.getTimeInMillis()));
            member.setPhotoUrl("https://d3w52z135jkm97.cloudfront.net/uploads/profile/photo/11325/profile_638x479_177b9aad-88b7-49c5-860b-52bf97a0e7d9.jpg");
            newMembers.add(member);
        }
        MemberDao.create(newMembers);
        Member firstMember = newMembers.get(0);
        firstMember.fetchAndSetPhotoFromUrl(context);
    }

    @Override
    public void close() {
        super.close();
    }
}
