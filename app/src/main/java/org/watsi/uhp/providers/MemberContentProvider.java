package org.watsi.uhp.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.j256.ormlite.dao.Dao;

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
//        String query = uri.getLastPathSegment().toLowerCase();
//        AndroidDatabaseResults dbResults = (AndroidDatabaseResults) memberDao.iterator().getRawResults();

        String[] cursorColumns = {"_ID", "SUGGEST_COLUMN_TEXT_1", "SUGGEST_COLUMN_TEXT_2"};
        MatrixCursor resultsCursor = new MatrixCursor(cursorColumns);
        List<Member> matchingMembers = new ArrayList<Member>();
//        try {
//            matchingMembers = memberDao.queryForAll();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        for (Member member : matchingMembers) {
//            String idString = String.valueOf(member.getId());
//            Object[] searchSuggestion = { idString, member.getName(), idString };
//            resultsCursor.addRow(searchSuggestion);
//        }
        resultsCursor.addRow(new Object[]{1l, "foo", "bar"});
        resultsCursor.addRow(new Object[]{2l, "foot", "barb"});
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
