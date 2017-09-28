package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.TableUtils;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * POJO helper for querying Billables
 */
public class BillableDao {

    private static BillableDao instance = new BillableDao();

    private Dao<Billable, UUID> mBillableDao;

    private static synchronized BillableDao getInstance() {
        return instance;
    }

    private BillableDao() {
    }

    private void setBillableDao(Dao billableDao) {
        this.mBillableDao = billableDao;
    }

    private Dao<Billable, UUID> getBillableDao() throws SQLException {
        if (mBillableDao == null) {
            setBillableDao(DatabaseHelper.getHelper().getDao(Billable.class));
        }

        return mBillableDao;
    }

    public static void create(Billable billable) throws SQLException {
        billable.setCreatedAt(Clock.getCurrentTime());
        getInstance().getBillableDao().create(billable);
    }

    public static void create(List<Billable> billables) throws SQLException {
        Date createdAt = Clock.getCurrentTime();
        for (Billable billable : billables) {
            billable.setCreatedAt(createdAt);
        }
        getInstance().getBillableDao().create(billables);
    }

    public static void refresh(Billable billable) throws SQLException {
        getInstance().getBillableDao().refresh(billable);
    }

    public static void clear() throws SQLException {
        TableUtils.clearTable(getInstance().getBillableDao().getConnectionSource(), Billable.class);
    }

    public static Billable findById(UUID id) throws SQLException {
        return getInstance().getBillableDao().queryForId(id);
    }

    public static List<Billable> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_NAME, new SelectArg(name));
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }

    public static Set<String> allUniqueDrugNames() throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_TYPE, Billable.TypeEnum.DRUG)
                .prepare();

        List<Billable> allDrugs = getInstance().getBillableDao().query(pq);
        List<String> names = new ArrayList<>();
        for (Billable billable : allDrugs) {
            names.add(billable.getName());
        }
        return new HashSet<>(names);
    }

    public static List<Billable> fuzzySearchDrugs(String query) throws SQLException {
        List<ExtractedResult> topMatchingNames =
                FuzzySearch.extractTop(query, allUniqueDrugNames(), 5, 50);

        List<Billable> topMatchingDrugs = new ArrayList<>();
        for (ExtractedResult result : topMatchingNames) {
            String name = result.getString();
            topMatchingDrugs.addAll(findByName(name));
        }

        return topMatchingDrugs;
    }

    public static List<Billable> getBillablesByCategory(Billable.TypeEnum category) throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_TYPE, category);
        return getInstance().getBillableDao().queryForFieldValues(queryMap);
    }

    public static List<String> getUniqueBillableCompositions() throws SQLException {
        PreparedQuery<Billable> pq = getInstance().getBillableDao()
                .queryBuilder()
                .distinct()
                .orderBy(Billable.FIELD_NAME_COMPOSITION, false)
                .selectColumns(Billable.FIELD_NAME_COMPOSITION)
                .where()
                .isNotNull(Billable.FIELD_NAME_COMPOSITION)
                .prepare();

        List<Billable> allBillables = getInstance().getBillableDao().query(pq);
        List<String> compositions = new ArrayList<>();
        for (Billable billable : allBillables) {
            compositions.add(billable.getComposition());
        }
        return compositions;
    }
}
