package org.watsi.uhp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.User;

import java.util.Calendar;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PreferenceManager.class)
public class PreferencesManagerTest {

    @Mock
    Context mockContext;
    @Mock
    SharedPreferences mockSharedPreferences;
    @Mock
    SharedPreferences.Editor mockEditor;

    private PreferencesManager preferencesManager;
    private String expiryString = Clock.ISO_DATE_FORMAT.format(Calendar.getInstance().getTime());

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(PreferenceManager.class);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        preferencesManager = new PreferencesManager(mockContext);
    }

    @Test
    public void setMemberLastModified() throws Exception {
        preferencesManager.setMemberLastModified(expiryString);

        verify(mockEditor, times(1)).putString("membersLastModified", expiryString);
        verify(mockEditor, times(1)).apply();
    }

    @Test
    public void getMemberLastModified() throws Exception {
        preferencesManager.getMemberLastModified();

        verify(mockSharedPreferences, times(1)).getString("membersLastModified", null);
    }

    @Test
    public void setBillablesLastModified() throws Exception {
        preferencesManager.setBillablesLastModified(expiryString);

        verify(mockEditor, times(1)).putString("billablesLastModified", expiryString);
        verify(mockEditor, times(1)).apply();
    }

    @Test
    public void getBillablesLastModified() throws Exception {
        preferencesManager.getBillablesLastModified();

        verify(mockSharedPreferences, times(1)).getString("billablesLastModified", null);
    }

    @Test
    public void setUsername() throws Exception {
        String username = "foo";

        preferencesManager.setUsername(username);

        verify(mockEditor, times(1)).putString("username", username);
        verify(mockEditor, times(1)).apply();
    }

    @Test
    public void getUsername() throws Exception {
        preferencesManager.getUsername();

        verify(mockSharedPreferences, times(1)).getString("username", null);
    }

    @Test
    public void clearUsername() throws Exception {
        preferencesManager.clearUsername();

        verify(mockEditor, times(1)).remove("username");
        verify(mockEditor, times(1)).apply();
    }
}
