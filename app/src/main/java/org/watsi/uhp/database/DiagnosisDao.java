package org.watsi.uhp.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Diagnosis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

public class DiagnosisDao {

    private static Dao<Diagnosis, Integer> getDao() throws SQLException {
        return DatabaseHelper.fetchDao(Diagnosis.class);
    }

    /**
     * This search prioritizes exact matches of alias over fuzzy matches with description.
     */
    public static List<Diagnosis> searchByFuzzyDescriptionAndSearchAlias(String query) throws SQLException {
        List<ExtractedResult> topMatchingNames =
                FuzzySearch.extractTop(query, allUniqueDiagnosisDescriptions(), 6, 60);

        // This sorts the fuzzy search results by decreasing score, increasing alphabetical order.
        Collections.sort(topMatchingNames, new Comparator<ExtractedResult>() {
            @Override
            public int compare(ExtractedResult o1, ExtractedResult o2) {
                return o2.getScore() == o1.getScore() ?
                        o1.getString().compareTo(o2.getString())
                        : Integer.compare(o2.getScore(), o1.getScore());
            }
        });

        LinkedHashSet<Diagnosis> topMatchingDiagnoses = new LinkedHashSet<>();

        List<Diagnosis> topMatchingDiagnosesViaSearchAliases = findBySearchAliases(query);
        for (Diagnosis diagnosis: topMatchingDiagnosesViaSearchAliases) {
            topMatchingDiagnoses.add(diagnosis);
        }

        for (ExtractedResult result : topMatchingNames) {
            String name = result.getString();
            Diagnosis diagnosis = findDiagnosisByDescription(name);
            topMatchingDiagnoses.add(diagnosis);
        }

        return new ArrayList<>(topMatchingDiagnoses);
    }

    public static List<Diagnosis> findBySearchAliases(String searchAlias) throws SQLException {
        PreparedQuery<Diagnosis> pq = getDao()
                .queryBuilder()
                .orderBy(Diagnosis.FIELD_NAME_DESCRIPTION, true)
                .where()
                .like(Diagnosis.FIELD_NAME_SEARCH_ALIASES, '%' + searchAlias + '%')
                .prepare();
        List<Diagnosis> matchedDiagnoses = getDao().query(pq);

        return matchedDiagnoses;
    }

    public static Diagnosis findDiagnosisByDescription(String description) throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Diagnosis.FIELD_NAME_DESCRIPTION, new SelectArg(description));
        List<Diagnosis> matchedDiagnoses = getDao().queryForFieldValues(queryMap);
        if (matchedDiagnoses.size() != 1) {
            throw new SQLException("Found " + matchedDiagnoses.size() + " diagnosis with description " + description);
        }
        return matchedDiagnoses.get(0);
    }

    public static Collection<String> allUniqueDiagnosisDescriptions() throws SQLException {
        List<Diagnosis> diagnoses = getDao().queryForAll();
        Set<String> result = new HashSet<>();
        for (Diagnosis diagnosis: diagnoses) {
            result.add(diagnosis.getDescription());
        }
        return result;
    }
}

