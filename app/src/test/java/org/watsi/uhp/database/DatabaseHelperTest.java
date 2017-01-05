package org.watsi.uhp.database;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.watsi.uhp.models.Member;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DatabaseHelperTest {

    DatabaseHelper databaseHelper;

    @Mock
    Context fakeContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        databaseHelper = new DatabaseHelper(fakeContext);
    }

    @Test
    public void getMemberDao_mUserDaoIsNull_callsGetDao() throws Exception {
        databaseHelper.setMemberDao(null);
        DatabaseHelper databaseHelperSpy = spy(databaseHelper);
        databaseHelperSpy.getMemberDao();
        verify(databaseHelperSpy, times(1)).getDao(Member.class);
    }

    @Test
    public void getUserDao_mUserDaoIsNotNull_doesNotCallGetDao() throws Exception {
        databaseHelper.setMemberDao(mock(Dao.class));
        DatabaseHelper databaseHelperSpy = spy(databaseHelper);
        databaseHelperSpy.getMemberDao();
        verify(databaseHelperSpy, times(0)).getDao(Member.class);
    }
}
