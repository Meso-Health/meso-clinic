package org.watsi.uhp.managers;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.fragments.CurrentMemberDetailFragment;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class })
public class MenuNavigationManagerTest {

    @Mock
    private AppCompatActivity mockActivity;

    @Mock
    private MenuItem mockMenuItem;

    @Mock
    private Fragment mockGenericFragment;

    @Mock
    private IdentificationEvent mockIdEvent;

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
        mockStatic(ExceptionManager.class);
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

    @Test
    public void getMemberFromFragmentIfExists_notMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        Member member = menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment);
        assertNull(member);
    }

    @Test
    public void getMemberFromFragmentIfExists_currentMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        when(mockCurrentMemberDetailFragment.getMember()).thenReturn(mockMember);
        Member member = menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockCurrentMemberDetailFragment);
        assertEquals(member, mockMember);
    }

    @Test
    public void getMemberFromFragmentIfExists_checkInMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        when(mockCheckInMemberDetailFragment.getMember()).thenReturn(mockMember);
        Member member = menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockCheckInMemberDetailFragment);
        assertEquals(member, mockMember);
    }

    @Test
    public void reportMember_checkInMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doNothing().when(mockCheckInMemberDetailFragment).reportMember();
        menuNavigationManagerSpy.reportMember(mockCheckInMemberDetailFragment);

        verify(mockCheckInMemberDetailFragment, times(1)).reportMember();
    }

    @Test
    public void reportMember_notCheckInMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        menuNavigationManagerSpy.reportMember(mockGenericFragment);

        verifyStatic();
        ExceptionManager.reportErrorMessage(any(String.class));
    }

    @Test
    public void navigateToCompleteEnrollmentFragment_checkInMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockIdEvent).when(mockCheckInMemberDetailFragment).getIdEvent();
        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToCompleteEnrollmentFragment(mockCheckInMemberDetailFragment, mockMember);

        verify(mockNavigationManager, times(1)).setEnrollmentMemberPhotoFragment(mockMember, mockIdEvent);
    }

    @Test
    public void navigateToCompleteEnrollmentFragment_currentMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToCompleteEnrollmentFragment(mockCurrentMemberDetailFragment, mockMember);

        verify(mockNavigationManager, times(1)).setEnrollmentMemberPhotoFragment(mockMember, null);
    }

    @Test
    public void navigateToCompleteEnrollmentFragment_notMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToCompleteEnrollmentFragment(mockGenericFragment, mockMember);

        verifyStatic();
        ExceptionManager.reportErrorMessage(any(String.class));
    }

    @Test
    public void navigateToMemberEditFragment_checkInMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockIdEvent).when(mockCheckInMemberDetailFragment).getIdEvent();
        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToMemberEditFragment(mockCheckInMemberDetailFragment, mockMember);

        verify(mockNavigationManager, times(1)).setMemberEditFragment(mockMember, mockIdEvent, null);
    }

    @Test
    public void navigateToMemberEditFragment_currentMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToMemberEditFragment(mockCurrentMemberDetailFragment, mockMember);

        verify(mockNavigationManager, times(1)).setMemberEditFragment(mockMember, null, null);
    }

    @Test
    public void navigateToMemberEditFragment_notMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);

        doReturn(mockNavigationManager).when(menuNavigationManagerSpy).getNavigationManager();
        menuNavigationManagerSpy.navigateToMemberEditFragment(mockGenericFragment, mockMember);

        verifyStatic();
        ExceptionManager.reportErrorMessage(any(String.class));
    }


}
