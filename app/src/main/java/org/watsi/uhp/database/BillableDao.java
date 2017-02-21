package org.watsi.uhp.database;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * POJO helper for querying Billables
 */
public class BillableDao {

    private static BillableDao instance = new BillableDao();

    private Dao<Billable, Integer> mBillableDao;

    private static synchronized BillableDao getInstance() {
        return instance;
    }

    private BillableDao() {
    }

    private void setBillableDao(Dao billableDao) {
        this.mBillableDao = billableDao;
    }

    private Dao<Billable, Integer> getBillableDao() throws SQLException {
        if (mBillableDao == null) {
            setBillableDao(DatabaseHelper.getHelper().getDao(Billable.class));
        }

        return mBillableDao;
    }

    public static void create(List<Billable> billables) throws SQLException {
        getInstance().getBillableDao().create(billables);
    }

    public static Billable findById(String billableId) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_ID, billableId);
        return getInstance().getBillableDao().queryForFieldValues(queryMap).get(0);
    }

    public static List<Billable> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_NAME, name);
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }

    public static Set<String> allUniqueDrugNames() throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_CATEGORY, Billable.CategoryEnum.DRUGS)
                .prepare();

        List<Billable> allDrugs = getInstance().getBillableDao().query(pq);
        List<String> names = new ArrayList<>();
        for (Billable billable : allDrugs) {
            names.add(billable.getName());
        }
        return new HashSet<>(names);
    }

    public static List<Billable> fuzzySearchDrugs(String query, int number, int threshold)
            throws SQLException {
        List<ExtractedResult> topMatchingNames =
                FuzzySearch.extractTop(query, allUniqueDrugNames(), number, threshold);

        List<Billable> topMatchingDrugs = new ArrayList<>();
        for (ExtractedResult result : topMatchingNames) {
            String name = result.getString();
            topMatchingDrugs.addAll(findByName(name));
        }

        return topMatchingDrugs;
    }

    public static Cursor fuzzySearchDrugsCursor(String query, int number, int threshold) throws SQLException {
        List<Billable> topMatchingDrugs = fuzzySearchDrugs(query, number, threshold);

        String[] cursorColumns = {
                Member.FIELD_NAME_ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
        };
        MatrixCursor resultsCursor = new MatrixCursor(cursorColumns);

        for (Billable drug : topMatchingDrugs) {
            Object[] searchSuggestion = {
                    drug.getId(),
                    drug.getName(),
                    drug.getDisplayDetails()
            };
            resultsCursor.addRow(searchSuggestion);
        }

        return resultsCursor;
    }

    public static Cursor getBillablesByCategoryCursor(Billable.CategoryEnum category) throws
            SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_ID, Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_CATEGORY, category)
                .prepare();

        CloseableIterator<Billable> iterator = getInstance().getBillableDao().iterator(pq);
        AndroidDatabaseResults results = (AndroidDatabaseResults)iterator.getRawResults();
        return results.getRawCursor();
    }
}
