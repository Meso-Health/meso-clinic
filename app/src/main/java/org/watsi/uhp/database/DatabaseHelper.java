package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.LabResult;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.Photo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 14;

    private static DatabaseHelper instance;

    private final Map<Class, Dao> daoMap = new HashMap<>();

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

    public static synchronized void reset() {
        instance.close();
        instance = null;
    }

    public static synchronized Dao fetchDao(Class clazz) throws SQLException {
        if (!instance.daoMap.containsKey(clazz)) {
            instance.daoMap.put(clazz, instance.getDao(clazz));
        }
        return instance.daoMap.get(clazz);
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
            TableUtils.createTable(connectionSource, Photo.class);
            TableUtils.createTable(connectionSource, Diagnosis.class);
            TableUtils.createTable(connectionSource, LabResult.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // no-op
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
            TableUtils.clearTable(connectionSource, Photo.class);
            TableUtils.clearTable(connectionSource, Diagnosis.class);
            TableUtils.clearTable(connectionSource, LabResult.class);
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
