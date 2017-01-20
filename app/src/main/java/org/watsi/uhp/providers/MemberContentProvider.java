package org.watsi.uhp.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.watsi.uhp.database.MemberDao;
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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String query = uri.getLastPathSegment().toLowerCase();

        String[] cursorColumns = {
                BaseColumns._ID,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_INTENT_DATA
        };
        MatrixCursor resultsCursor = new MatrixCursor(cursorColumns);

        try {
            List<Member> matchingMembers = MemberDao.withNameLike(query);

            for (Member member : matchingMembers) {
                String idString = String.valueOf(member.getId());
                Object[] searchSuggestion = { idString, member.getName(), idString, idString };
                resultsCursor.addRow(searchSuggestion);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultsCursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
