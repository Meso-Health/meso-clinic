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
    private static final int DATABASE_VERSION = 6;

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
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    onUpgrade(database, connectionSource, 4, newVersion);
                    break;
                case 3:
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dirty_fields STRING NOT NULL DEFAULT '[]';");
                case 4:
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN birthdate DATE;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN birthdate_accuracy STRING;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(Member.class).executeRaw("ALTER TABLE `members` ADD COLUMN enrolled_at DATE;");
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN is_new BOOLEAN NOT NULL DEFAULT 0;");
                case 5:
                    getDao(Encounter.class).executeRaw("ALTER TABLE `encounters` ADD COLUMN backdated_occurred_at BOOLEAN NOT NULL DEFAULT 0;");
            }
            Rollbar.reportMessage("Migration run from version " + oldVersion + " to " + newVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
