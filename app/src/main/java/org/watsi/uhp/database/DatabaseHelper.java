package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.User;

import java.sql.SQLException;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 10;

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
            instance = new DatabaseHelper(context.getApplicationContext());
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
            TableUtils.createTable(connectionSource, EncounterForm.class);
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
                case 6:
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dismissed BOOLEAN NOT NULL DEFAULT 0;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN dismissal_reason STRING;");
                case 7:
                    TableUtils.createTable(connectionSource, EncounterForm.class);
                case 8:
                    // After talking with @pete and @byronium we need this no-op here because in the past, we had to update
                    // some phones's data directly. Those methods no longer will compile here, and all phones should be > version 8 now.
                case 9:
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_result_code INT;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_tier STRING;");
                    getDao(IdentificationEvent.class).executeRaw("ALTER TABLE `identifications` ADD COLUMN fingerprints_verification_confidence FLOAT;");
            }
            ExceptionManager.reportMessage("Migration run from version " + oldVersion + " to " +
                    newVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearDatabase() {
        try {
            ConnectionSource connectionSource = getConnectionSource();
            TableUtils.clearTable(connectionSource, Member.class);
            TableUtils.clearTable(connectionSource, Billable.class);
            TableUtils.clearTable(connectionSource, IdentificationEvent.class);
            TableUtils.clearTable(connectionSource, Encounter.class);
            TableUtils.clearTable(connectionSource, EncounterItem.class);
            TableUtils.clearTable(connectionSource, EncounterForm.class);
            TableUtils.clearTable(connectionSource, User.class);
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
