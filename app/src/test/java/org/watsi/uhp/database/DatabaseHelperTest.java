package org.watsi.uhp.database;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class DatabaseHelperTest {

    @Mock
    Context fakeContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getHelper_doesNotThrowExceptionIfInitialized() throws Exception {
        DatabaseHelper.init(fakeContext);
        try {
            DatabaseHelper.getHelper();
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void getHelper_throwsExceptionIfNotInitialized() throws Exception {
        try {
            DatabaseHelper.getHelper();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Must initialize DatabaseHelper before acquiring instance");
        }
    }
}
