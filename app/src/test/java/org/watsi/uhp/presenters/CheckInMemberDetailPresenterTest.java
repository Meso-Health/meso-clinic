package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by michaelliang on 6/13/17.
 */

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ CheckInMemberDetailPresenter.class })
public class CheckInMemberDetailPresenterTest {
    // Since unit tests don't have access to database default values, null is the default value.
    private static Boolean DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD = null;

    private CheckInMemberDetailPresenter checkInMemberDetailPresenter;

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
    CheckInMemberDetailFragment mockCheckInMemberDetailFragment;

    @Mock
    Member mockThroughMember;

    @Mock
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void preFillIdentificationEventFields_successfulMemberWithoutPhoto() {
        Member member = new Member();
        Member memberSpy = spy(member);
        when(memberSpy.getPhoto()).thenReturn(null);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, memberSpy,
                IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, mockThroughMember);
        checkInMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = checkInMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), memberSpy);
        assertEquals(idEvent.getSearchMethod(), IdentificationEvent.SearchMethodEnum.SCAN_BARCODE);
        assertEquals(idEvent.getThroughMember(), mockThroughMember);
        assertFalse(idEvent.getPhotoVerified());
    }

    @Test
    public void preFillIdentificationEventFields_memberWithPhoto() {
        Member member = new Member();
        Member memberSpy = spy(member);
        when(memberSpy.getPhoto()).thenReturn(new byte[5]);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, memberSpy, null, null);
        checkInMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = checkInMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), memberSpy);
        assertEquals(idEvent.getSearchMethod(), null);
        assertEquals(idEvent.getThroughMember(), null);
        assertEquals(idEvent.getPhotoVerified(), DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD);
    }
}

