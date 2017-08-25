package org.watsi.uhp.managers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.watsi.uhp.fragments.EncounterFragment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class NavigationManagerTest {

    @Mock
    private AppCompatActivity mockActivity;
    @Mock
    private FragmentManager mockFragmentManager;
    @Mock
    private FragmentTransaction mockFragmentTransaction;
    @Mock
    private NavigationManager.FragmentProvider mockFragmentProvider;

    @Mock
    private Bundle mockBundle;

    private NavigationManager navMgr;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);
        when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        navMgr = new NavigationManager(mockActivity, mockFragmentProvider);
    }

    @Test
    public void fragmentProvider() throws Exception {
        NavigationManager.FragmentProvider fragmentProvider =
                new NavigationManager.FragmentProvider();

        Fragment fragment = fragmentProvider.createFragment(EncounterFragment.class);
        assertThat(fragment, instanceOf(EncounterFragment.class));
    }

    @Test
    public void setFragment_noCurrentFragment() throws Exception {

    }

    @Test
    public void setFragment_newFragmentNotInBackstack() throws Exception {

    }

    @Test
    public void setFragment_fragmentInBackstack() throws Exception {

    }
}
