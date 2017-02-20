package org.watsi.uhp.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Identification;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class MemberContentProvider extends ContentProvider {

    public MemberContentProvider() {}

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment().toLowerCase();

        String[] cursorColumns = {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
        };
        MatrixCursor resultsCursor = new MatrixCursor(cursorColumns);

        List<Member> matchingMembers;
        String idMethod;
        try {
            if (containsNumber(query)) {
                matchingMembers = MemberDao.withCardIdLike(query);
                idMethod = Identification.IdMethodEnum.SEARCH_ID.toString();
            } else {
                matchingMembers = MemberDao.fuzzySearchMembers(query, 5, 50);
                idMethod = Identification.IdMethodEnum.SEARCH_NAME.toString();
            }

            for (Member member : matchingMembers) {
                Object[] searchSuggestion = {
                        member.getId(),
                        member.getFullName(),
                        member.getCardId(),
                        member.getId(),
                        idMethod

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
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private boolean containsNumber(String str) {
        return str.matches(".*\\d+.*");
    }
}
