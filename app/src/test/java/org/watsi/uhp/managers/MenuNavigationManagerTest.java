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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
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
    private Member mockNewBorn;

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
        doNothing().when(menuNavigationManagerSpy).confirmBeforeLogout(mockGenericFragment);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).confirmBeforeLogout(mockGenericFragment);
        assertTrue(result);
    }

    @Test
    public void nextStep_memberEdit() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_member_edit);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);

        doNothing().when(menuNavigationManagerSpy).editMember(mockGenericFragment);
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(menuNavigationManagerSpy, times(1)).editMember(mockGenericFragment);
        assertTrue(result);
    }

    @Test
    public void nextStep_enrollNewborn() throws Exception {
        when(mockMenuItem.getItemId()).thenReturn(R.id.menu_enroll_newborn);
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        when(menuNavigationManagerSpy.getMemberFromFragmentIfExists(mockGenericFragment)).thenReturn(mockMember);
        when(mockMember.createNewborn()).thenReturn(mockNewBorn);

        doNothing().when(mockNavigationManager).setEnrollNewbornInfoFragment(eq(mockNewBorn), any(IdentificationEvent.class));
        boolean result = menuNavigationManagerSpy.nextStep(mockGenericFragment, mockMenuItem);

        verify(mockNavigationManager, times(1)).setEnrollNewbornInfoFragment(eq(mockNewBorn), any(IdentificationEvent.class));
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
    public void editMember_memberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        menuNavigationManagerSpy.editMember(mockCheckInMemberDetailFragment);

        verify(mockCheckInMemberDetailFragment, times(1)).navigateToMemberEditFragment();
    }

    @Test
    public void editMember_notMemberDetailFragment() throws Exception {
        MenuNavigationManager menuNavigationManagerSpy = spy(menuNavigationManager);
        menuNavigationManagerSpy.editMember(mockGenericFragment);

        verifyStatic();
        ExceptionManager.reportErrorMessage(any(String.class));
    }
}
