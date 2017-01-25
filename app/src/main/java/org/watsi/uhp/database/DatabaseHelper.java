package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.BillableEncounter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Member;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton for managing access to local Sqlite DB
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "org.watsi.db";
    private static final int DATABASE_VERSION = 2;

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
            TableUtils.dropTable(connectionSource, Billable.class, true);
            TableUtils.dropTable(connectionSource, Encounter.class, true);
            TableUtils.dropTable(connectionSource, BillableEncounter.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadBillables(Context context) throws SQLException, IOException {
        TableUtils.clearTable(DatabaseHelper.getHelper().getConnectionSource(), Billable.class);

        // setup enum conversion maps
        Map<String, Billable.CategoryEnum> categoryMap = new HashMap<>();
        categoryMap.put("Drugs & Supplies", Billable.CategoryEnum.DRUGS_AND_SUPPLIES);
        categoryMap.put("Labs", Billable.CategoryEnum.LABS);
        categoryMap.put("Services", Billable.CategoryEnum.SERVICES);

        Map<String, Billable.DepartmentEnum> departmentMap = new HashMap<>();
        departmentMap.put("Ante-natal", Billable.DepartmentEnum.ANTE_NATAL);
        departmentMap.put("ART Clinic", Billable.DepartmentEnum.ART_CLINIC);
        departmentMap.put("Natural family planning", Billable.DepartmentEnum.FAMILY_PLANNING);
        departmentMap.put("Immunisation", Billable.DepartmentEnum.IMMUNISATION);
        departmentMap.put("Maternity", Billable.DepartmentEnum.MATERNITY);
        departmentMap.put("Post-natal", Billable.DepartmentEnum.POST_NATAL);
        departmentMap.put("", Billable.DepartmentEnum.UNSPECIFIED);
        departmentMap.put("?", Billable.DepartmentEnum.UNSPECIFIED);

        // parse CSV
        List<Billable> billables = new ArrayList<>();
        InputStream inputStream = context.getResources().openRawResource(R.raw.price_list);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                Billable billable = new Billable();
                billable.setCategory(categoryMap.get(row[0]));
                billable.setName(row[1]);
                String unit = row[4];
                if (unit.length() > 0 && !"?".equals(unit)) {
                    billable.setUnit(unit);
                    billable.setAmount(row[3]);
                }
                billable.setDepartment(departmentMap.get(row[5]));
                billables.add(billable);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        } finally {
            inputStream.close();
        }
        BillableDao.create(billables);
    }

    @Override
    public void close() {
        super.close();
    }
}
