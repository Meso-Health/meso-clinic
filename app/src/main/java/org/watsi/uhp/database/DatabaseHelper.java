package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.User;

import java.sql.SQLException;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 4;

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

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
    }

    @Override
     public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Member.class);
            TableUtils.createTable(connectionSource, Billable.class);
            TableUtils.createTable(connectionSource, IdentificationEvent.class);
            TableUtils.createTable(connectionSource, Encounter.class);
            TableUtils.createTable(connectionSource, EncounterItem.class);
            TableUtils.createTable(connectionSource, User.class);
            Log.d("UHP", "onCreate database helper called");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Rollbar.reportMessage("Migration run from version " + oldVersion + " to " + newVersion);
        try {
            switch (oldVersion) {
                default:
                case 2:
                    TableUtils.dropTable(connectionSource, IdentificationEvent.class, false);
                    TableUtils.dropTable(connectionSource, Encounter.class,false);
                    TableUtils.dropTable(connectionSource, EncounterItem.class, false);

                    TableUtils.createTable(connectionSource, IdentificationEvent.class);
                    TableUtils.createTable(connectionSource, Encounter.class);
                    TableUtils.createTable(connectionSource, EncounterItem.class);
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING;");
                    break;
                case 3:
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING;");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN dirty_fields STRING;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dirty_fields STRING;");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
