package org.watsi.uhp.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

import static android.R.id.list;

public class BillableContentProvider extends ContentProvider {

    public BillableContentProvider() {}

    @Override
    public boolean onCreate() { return true; }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment().toLowerCase();

        String[] cursorColumns = {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2
        };
        MatrixCursor resultsCursor = new MatrixCursor(cursorColumns);

        try {
            List<Billable> allBillableNames = BillableDao.allNames();

            List<String> names = new ArrayList<String>(allBillableNames.size());
            for (Billable billable : allBillableNames) {
                names.add(billable != null ? billable.getName() : null);
            }

            Set<String> uniqueNames = new HashSet<String>(names);

            List<ExtractedResult> topMatchingNames = FuzzySearch.extractTop(query, (Collection<String>) uniqueNames, 5);

            ArrayList<Billable> matchingBillables = new ArrayList<Billable>();
            for (ExtractedResult result : topMatchingNames) {
                String name = result.getString();
                matchingBillables.addAll(BillableDao.findByName(name));
            }

            for (Billable billable : matchingBillables) {
                String billableDetails = new String();
                if (billable.getAmount() != null) billableDetails += billable.getAmount() + " ";
                if (billable.getUnit() != null) billableDetails += billable.getUnit();
                if (billable.getDepartment() != Billable.DepartmentEnum.UNSPECIFIED) billableDetails += " - " + billable.getDepartment();

                Object[] searchSuggestion = {
                        billable.getId(),
                        billable.getName(),
                        billableDetails
                };
                resultsCursor.addRow(searchSuggestion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultsCursor;

    }

    @Override
    public String getType(@NonNull Uri uri) { return null; }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
