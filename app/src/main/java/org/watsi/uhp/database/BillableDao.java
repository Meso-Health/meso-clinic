package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
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

    private static Dao<Billable, UUID> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(Billable.class);
    }

    public static List<Billable> all() throws SQLException {
        return getDao().queryForAll();
    }

    public static void create(Billable billable) throws SQLException {
        billable.setCreatedAt(Clock.getCurrentTime());
        getDao().create(billable);
    }

    public static void createOrUpdate(List<Billable> billables) throws SQLException {
        for (Billable billable : billables) {
            getDao().createOrUpdate(billable);
        }
    }

    public static void refresh(Billable billable) throws SQLException {
        getDao().refresh(billable);
    }

    public static void clearBillablesNotCreatedDuringEncounter() throws SQLException {
        DeleteBuilder<Billable, UUID> deleteBuilder = getDao().deleteBuilder();
        deleteBuilder.where().eq(Billable.FIELD_NAME_CREATED_DURING_ENCOUNTER, false);
        deleteBuilder.delete();
    }

    public static Billable findById(UUID id) throws SQLException {
        return getDao().queryForId(id);
    }

    public static List<Billable> findByName(String name) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_NAME, new SelectArg(name));
        return getDao().queryForFieldValues(queryMap);
    }

    public static Set<String> allUniqueDrugNames() throws SQLException {
        PreparedQuery<Billable> pq = getDao()
                .queryBuilder()
                .selectColumns(Billable.FIELD_NAME_NAME)
                .where()
                .eq(Billable.FIELD_NAME_TYPE, Billable.TypeEnum.DRUG)
                .prepare();

        List<Billable> allDrugs = getDao().query(pq);
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
            List<Billable> billablesWithMatchingName = findByName(name);
            for (Billable billable : billablesWithMatchingName) {
                if (billable.getType().equals(Billable.TypeEnum.DRUG)) {
                    topMatchingDrugs.add(billable);
                }
            }
        }

        return topMatchingDrugs;
    }

    public static List<Billable> getBillablesByType(Billable.TypeEnum type) throws SQLException {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put(Billable.FIELD_NAME_TYPE, type);
        return getDao().queryForFieldValues(queryMap);
    }

    public static List<String> getUniqueBillableCompositions() throws SQLException {
        PreparedQuery<Billable> pq = getDao()
                .queryBuilder()
                .distinct()
                .orderBy(Billable.FIELD_NAME_COMPOSITION, false)
                .selectColumns(Billable.FIELD_NAME_COMPOSITION)
                .where()
                .isNotNull(Billable.FIELD_NAME_COMPOSITION)
                .prepare();

        List<Billable> allBillables = getDao().query(pq);
        List<String> compositions = new ArrayList<>();
        for (Billable billable : allBillables) {
            compositions.add(billable.getComposition());
        }
        return compositions;
    }
}
