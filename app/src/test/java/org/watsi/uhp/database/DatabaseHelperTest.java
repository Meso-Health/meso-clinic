package org.watsi.uhp.database;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DatabaseHelperTest {

    @Mock
    Context context;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getHelper_doesNotThrowExceptionIfInitialized() throws Exception {
        DatabaseHelper.init(context);

        DatabaseHelper.getHelper();
    }

    @Test(expected=RuntimeException.class)
    public void getHelper_throwsExceptionIfNotInitialized() throws Exception {
        DatabaseHelper.getHelper();
    }
}
