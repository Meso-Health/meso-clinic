package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

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

    public static void createOrUpdate(List<Billable> billables) throws SQLException {
        for (Billable billable : billables) {
            getDao().createOrUpdate(billable);
        }
    }

    public static void clearBillablesWithoutUnsyncedEncounter() throws SQLException {
        DeleteBuilder<Billable, UUID> deleteBuilder = getDao().deleteBuilder();

        String rawQuery =
                "SELECT billables.id\n" +
                        "FROM billables\n" +
                        "INNER JOIN encounter_items ON encounter_items.billable_id = billables.id\n" +
                        "INNER JOIN encounters ON encounters.id = encounter_items.encounter_id\n" +
                        "WHERE encounters.dirty_fields != \"[]\" \n";

        GenericRawResults<String[]> results = getDao().queryRaw(rawQuery);

        Set<UUID> billableIdsWithUnsyncedEncounter = new HashSet<>();
        for (String[] result : results) {
            billableIdsWithUnsyncedEncounter.add(UUID.fromString(result[0]));
        }

        // we need to protect against the case where the next call to FetchBillables removes existing
        // new billables, which would cause an error when we query them as part of the encounter sync serialization
        deleteBuilder.where().notIn(Billable.FIELD_NAME_ID, billableIdsWithUnsyncedEncounter);
        deleteBuilder.delete();
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
