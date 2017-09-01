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
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.BaseFragment;
import org.watsi.uhp.fragments.EncounterFragment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private BaseFragment mockCurrentFragment;

    @Mock
    private BaseFragment mockNewFragment;

    @Mock
    private Bundle mockBundle;

    private NavigationManager navigationManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockActivity.getSupportFragmentManager()).thenReturn(mockFragmentManager);
        when(mockFragmentManager.beginTransaction()).thenReturn(mockFragmentTransaction);
        when(mockNewFragment.getName()).thenReturn("MockNewFragment");
        when(mockCurrentFragment.getName()).thenReturn("MockCurrentFragment");
        navigationManager = new NavigationManager(mockActivity, mockFragmentProvider);
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
        when(mockFragmentManager.findFragmentById(R.id.fragment_container)).thenReturn(null);
        when(mockFragmentTransaction.add(R.id.fragment_container, mockNewFragment, "MockNewFragment")).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.addToBackStack("addMockNewFragment")).thenReturn(mockFragmentTransaction);
        navigationManager.setFragment(mockNewFragment);

        verify(mockFragmentManager, never()).popBackStack(any(String.class), any(int.class));

        verify(mockFragmentTransaction, never()).remove(mockCurrentFragment);
        verify(mockFragmentTransaction, times(1)).add(R.id.fragment_container, mockNewFragment, "MockNewFragment");
        verify(mockFragmentTransaction, times(1)).addToBackStack("addMockNewFragment");
        verify(mockFragmentTransaction, times(1)).commit();
    }

    @Test
    public void setFragment_newFragmentNotInBackstack() throws Exception {
        when(mockFragmentManager.findFragmentById(R.id.fragment_container)).thenReturn(mockCurrentFragment);
        when(mockFragmentManager.findFragmentByTag("MockNewFragment")).thenReturn(null);
        when(mockFragmentTransaction.add(R.id.fragment_container, mockNewFragment, "MockNewFragment")).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.remove(mockCurrentFragment)).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.addToBackStack("addMockNewFragment")).thenReturn(mockFragmentTransaction);

        navigationManager.setFragment(mockNewFragment);

        verify(mockFragmentManager, never()).popBackStack(any(String.class), any(int.class));

        verify(mockFragmentTransaction, times(1)).remove(mockCurrentFragment);
        verify(mockFragmentTransaction, times(1)).add(R.id.fragment_container, mockNewFragment, "MockNewFragment");
        verify(mockFragmentTransaction, times(1)).addToBackStack("addMockNewFragment");
        verify(mockFragmentTransaction, times(1)).commit();
    }

    @Test
    public void setFragment_fragmentInBackstack() throws Exception {
        when(mockFragmentManager.findFragmentById(R.id.fragment_container)).thenReturn(mockCurrentFragment);
        when(mockFragmentManager.findFragmentByTag("MockNewFragment")).thenReturn(mockNewFragment);
        when(mockFragmentTransaction.add(R.id.fragment_container, mockNewFragment, "MockNewFragment")).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.remove(mockCurrentFragment)).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.addToBackStack("addMockNewFragment")).thenReturn(mockFragmentTransaction);

        navigationManager.setFragment(mockNewFragment);

        verify(mockFragmentManager, times(1)).popBackStack("addMockNewFragment", 0);

        verify(mockFragmentTransaction, times(1)).remove(mockCurrentFragment);
        verify(mockFragmentTransaction, times(1)).add(R.id.fragment_container, mockNewFragment, "MockNewFragment");
        verify(mockFragmentTransaction, times(1)).addToBackStack("addMockNewFragment");
        verify(mockFragmentTransaction, times(1)).commit();
    }

    @Test
    public void setFragment_customFragmentName() throws Exception {
        when(mockFragmentManager.findFragmentById(R.id.fragment_container)).thenReturn(mockCurrentFragment);
        when(mockFragmentManager.findFragmentByTag("CustomFragmentName")).thenReturn(mockNewFragment);
        when(mockFragmentTransaction.add(R.id.fragment_container, mockNewFragment, "CustomFragmentName")).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.remove(mockCurrentFragment)).thenReturn(mockFragmentTransaction);
        when(mockFragmentTransaction.addToBackStack("addCustomFragmentName")).thenReturn(mockFragmentTransaction);

        navigationManager.setFragment(mockNewFragment, "CustomFragmentName");

        verify(mockFragmentManager, times(1)).popBackStack("addCustomFragmentName", 0);

        verify(mockFragmentTransaction, times(1)).remove(mockCurrentFragment);
        verify(mockFragmentTransaction, times(1)).add(R.id.fragment_container, mockNewFragment, "CustomFragmentName");
        verify(mockFragmentTransaction, times(1)).addToBackStack("addCustomFragmentName");
        verify(mockFragmentTransaction, times(1)).commit();
    }
}
