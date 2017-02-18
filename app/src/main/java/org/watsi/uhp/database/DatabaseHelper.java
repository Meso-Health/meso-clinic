package org.watsi.uhp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LineItem;
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
            TableUtils.createTable(connectionSource, LineItem.class);
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
            TableUtils.dropTable(connectionSource, LineItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadBillables(Context context) throws SQLException, IOException {
        TableUtils.clearTable(DatabaseHelper.getHelper().getConnectionSource(), Billable.class);

        // setup enum conversion maps
        Map<String, Billable.CategoryEnum> categoryMap = new HashMap<>();
        categoryMap.put("Services", Billable.CategoryEnum.SERVICES);
        categoryMap.put("Labs", Billable.CategoryEnum.LABS);
        categoryMap.put("Supplies", Billable.CategoryEnum.SUPPLIES);
        categoryMap.put("Vaccines", Billable.CategoryEnum.VACCINES);
        categoryMap.put("Drugs", Billable.CategoryEnum.DRUGS);

        // TODO: remove
        Map<String, Billable.DepartmentEnum> departmentMap = new HashMap<>();
        departmentMap.put("Antenatal", Billable.DepartmentEnum.ANTENATAL);
        departmentMap.put("ART", Billable.DepartmentEnum.ART_CLINIC);
        departmentMap.put("", Billable.DepartmentEnum.UNSPECIFIED);

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
                billable.setName(row[1].trim());
                String unit = row[2];
                if (unit.length() > 0) {
                    billable.setUnit(unit);
                }
                String amount = row[3];
                if (amount.length() > 0) {
                    billable.setAmount(amount);
                }
                billable.setDepartment(departmentMap.get(row[4]));
                int price = 0;
                if (row[5].length() > 0) {
                    price = Integer.parseInt(row[5]);
                }
                billable.setPrice(price);
                billables.add(billable);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            inputStream.close();
        }
        try {
            BillableDao.create(billables);
        } catch (SQLException ex) {
            throw new RuntimeException("Error in creating billables: " + ex);
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
