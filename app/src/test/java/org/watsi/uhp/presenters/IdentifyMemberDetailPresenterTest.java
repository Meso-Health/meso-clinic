package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.fragments.IdentifyMemberDetailFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by michaelliang on 6/13/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberDao.class, IdentifyMemberDetailPresenter.class, ExceptionManager.class })
public class IdentifyMemberDetailPresenterTest {
    // Since unit tests don't have access to database default values, null is the default value.
    private static Boolean DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD = null;

    private IdentifyMemberDetailPresenter identifyMemberDetailPresenter;

    @Mock
    View mockView;

    @Mock
    Context mockContext;

    @Mock
    Member mockMember;

    @Mock
    NavigationManager mockNavigationManager;

    @Mock
    ContentResolver mockContentResolver;

    @Mock
    SessionManager mockSessionManager;

    @Mock
    IdentifyMemberDetailFragment mockIdentifyMemberDetailFragment;

    @Mock
    Member mockThroughMember;

    @Mock
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Mock
    Button mockButton;

    @Mock
    List<Member> mockMemberList;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);
        identifyMemberDetailPresenter = new IdentifyMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockIdentifyMemberDetailFragment,
                mockView, mockContext, mockMember,
                IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, mockThroughMember);
    }

    @Test
    public void preFillIdentificationEventFields_successfulMemberWithoutPhoto() {
        when(mockMember.getPhoto()).thenReturn(null);
        identifyMemberDetailPresenter = new IdentifyMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockIdentifyMemberDetailFragment,
                mockView, mockContext, mockMember,
                IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, mockThroughMember);
        identifyMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = identifyMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), mockMember);
        assertEquals(idEvent.getSearchMethod(), IdentificationEvent.SearchMethodEnum.SCAN_BARCODE);
        assertEquals(idEvent.getThroughMember(), mockThroughMember);
        assertFalse(idEvent.getPhotoVerified());
    }

    @Test
    public void preFillIdentificationEventFields_memberWithPhoto() {
        when(mockMember.getPhoto()).thenReturn(new byte[5]);
        identifyMemberDetailPresenter = new IdentifyMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockIdentifyMemberDetailFragment,
                mockView, mockContext, mockMember, null, null);
        identifyMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = identifyMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), mockMember);
        assertEquals(idEvent.getSearchMethod(), null);
        assertEquals(idEvent.getThroughMember(), null);
        assertEquals(idEvent.getPhotoVerified(), DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD);
    }

    @Test
    public void setMemberActionButton() {
        IdentifyMemberDetailPresenter identifyMemberDetailPresenterSpy = spy(identifyMemberDetailPresenter);
        when(identifyMemberDetailPresenterSpy.getMemberActionButton()).thenReturn(mockButton);

        identifyMemberDetailPresenterSpy.setMemberActionButton();

        verify(mockButton, times(1)).setText(R.string.check_in);
        verify(mockButton, times(1)).setOnClickListener(any(View.OnClickListener.class));
    }
}

