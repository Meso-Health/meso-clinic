package org.watsi.uhp.managers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.fragments.CurrentMemberDetailFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MenuNavigationManagerTest {

    @Mock
    private AppCompatActivity mockActivity;

    @Mock
    private MenuItem mockMenuItem;

    @Mock
    private Fragment mockGenericFragment;

    @Mock
    private CurrentMemberDetailFragment mockCurrentMemberDetailFragment;

    @Mock
    private CheckInMemberDetailFragment mockCheckInMemberDetailFragment;

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private ClinicActivity mockClinicActivity;

    @Mock
    private Member mockMember;

    @Mock
    private NavigationManager mockNavigationManager;

    private MenuNavigationManager menuNavigationManager;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mockClinicActivity.getNavigationManager()).thenReturn(mockNavigationManager);
        when(mockClinicActivity.getSessionManager()).thenReturn(mockSessionManager);
        menuNavigationManager = new MenuNavigationManager(mockClinicActivity);
    }

    @Test
    public void nextStep_logout() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_logout);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        doNothing().when(menuNavigationManagerSpy).confirmBeforelogout(mockGenericFragment);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).confirmBeforelogout(mockGenericFragment);
        assertTrue(result);
    }

    @Test
    public void nextStep_memberEdit() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_member_edit);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);
        doNothing().when(menuNavigationManagerSpy).navigateToMemberEditFragment(mockGenericFragment, mockMember);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).navigateToMemberEditFragment(mockGenericFragment, mockMember);
        assertTrue(result);
    }

    @Test
    public void nextStep_enrollNewborn() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_enroll_newborn);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);

        doNothing().when(mockNavigationManager).setEnrollNewbornInfoFragment(mockMember, null, null);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(mockNavigationManager, times(1)).setEnrollNewbornInfoFragment(mockMember, null, null);
        assertTrue(result);
    }

    @Test
    public void nextStep_version() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_version);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);

        doNothing().when(mockNavigationManager).setVersionFragment();
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(mockNavigationManager, times(1)).setVersionFragment();
        assertTrue(result);
    }

    @Test
    public void nextStep_completeEnrollment() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_complete_enrollment);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);

        doNothing().when(menuNavigationManagerSpy).navigateToCompleteEnrollmentFragment(mockGenericFragment, mockMember);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).navigateToCompleteEnrollmentFragment(mockGenericFragment, mockMember);
        assertTrue(result);
    }

    @Test
    public void nextStep_reportMember() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_report_member);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);

        doNothing().when(menuNavigationManagerSpy).reportMember(mockGenericFragment);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).reportMember(mockGenericFragment);
        assertTrue(result);
    }

    // TODO write the remaining tests.
}
