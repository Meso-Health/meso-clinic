package org.watsi.uhp.providers;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MemberContentProvider extends ContentProvider {

    private Dao<Member, Integer> memberDao;

    public MemberContentProvider() {}

    @Override
    public boolean onCreate() {
        try {
            this.memberDao = new DatabaseHelper(getContext()).getMemberDao();
            return true;
        } catch (SQLException e) {
            return false;
        }
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
            PreparedQuery<Member> pq = memberDao.queryBuilder().where().like(Member.FIELD_NAME_NAME, "%" + query + "%").prepare();
            List<Member> matchingMembers = memberDao.query(pq);

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
