package org.watsi.uhp.managers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.AddNewBillableFragment;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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
    public void setCurrentPatientsFragment() throws Exception {
        Fragment mockFragment = mock(Fragment.class);
        when(mockFragmentManager.findFragmentByTag("home")).thenReturn(mockFragment);
        when(mockFragmentTransaction.remove(mockFragment)).thenReturn(mockFragmentTransaction);

        navMgr.setCurrentPatientsFragment();

        verify(mockFragmentTransaction, never()).addToBackStack(null);
        verify(mockFragmentManager).popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        verify(mockFragmentTransaction).remove(mockFragment);
        verify(mockFragmentTransaction).replace(
                anyInt(),
                any(CurrentPatientsFragment.class),
                anyString()
        );
        verify(mockFragmentTransaction, times(2)).commit();
    }

    @Test
    public void setBarcodeFragment() throws Exception {
        BarcodeFragment fragment = mock(BarcodeFragment.class);
        when(mockFragmentProvider.createFragment(BarcodeFragment.class)).thenReturn(fragment);

        navMgr.setBarcodeFragment();
        addsToBackStackButDoesNotPopBackStack(fragment);
    }

    @Test
    public void setSearchMemberFragment() throws Exception {
        SearchMemberFragment fragment = mock(SearchMemberFragment.class);
        when(mockFragmentProvider.createFragment(SearchMemberFragment.class)).thenReturn(fragment);

        navMgr.setSearchMemberFragment();
        addsToBackStackButDoesNotPopBackStack(fragment);
    }

    @Test
    public void setEncounterFragment() throws Exception {
        EncounterFragment fragment = mock(EncounterFragment.class);
        when(mockFragmentProvider.createFragment(EncounterFragment.class)).thenReturn(fragment);

        navMgr.setEncounterFragment();
        addsToBackStackButDoesNotPopBackStack(fragment);
    }

    @Test
    public void setAddNewBillableFragment() throws Exception {
        AddNewBillableFragment fragment = mock(AddNewBillableFragment.class);
        when(mockFragmentProvider.createFragment(AddNewBillableFragment.class)).thenReturn(fragment);

        navMgr.setAddNewBillableFragment();
        addsToBackStackButDoesNotPopBackStack(fragment);
    }

    private void addsToBackStackButDoesNotPopBackStack(Fragment fragment) {
        verify(mockFragmentManager, never()).popBackStack(anyString(), anyInt());
        verify(mockFragmentTransaction).addToBackStack(null);
        verify(mockFragmentTransaction).replace(
                R.id.fragment_container,
                fragment,
                null
        );
        verify(mockFragmentTransaction).commit();
    }
}
