package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.simprints.libsimprints.Verification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.helpers.SimprintsHelper;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberDao.class, CheckInMemberDetailPresenter.class, ExceptionManager.class, ContextCompat.class})
public class CurrentMemberDetailPresenterTest {

    private CurrentMemberDetailPresenter currentMemberDetailPresenter;

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
    Member mockThroughMember;

    @Mock
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Mock
    Button mockButton;

    @Mock
    TextView mockTextView;

    @Mock
    List<Member> mockMemberList;

    @Mock
    IdentificationEvent mockIdentificationEvent;

    @Mock
    Intent mockFingerprintIntentData;

    @Mock
    Verification mockVerification;

    @Mock
    SimprintsHelper mockSimprintsHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);

        currentMemberDetailPresenter = new CurrentMemberDetailPresenter(mockNavigationManager, mockView, mockContext, mockMember);
    }

    @Test
    public void setMemberActionButton() {
        CurrentMemberDetailPresenter currentMemberDetailPresenterSpy = spy(currentMemberDetailPresenter);
        when(currentMemberDetailPresenterSpy.getMemberActionButton()).thenReturn(mockButton);

        currentMemberDetailPresenterSpy.setMemberActionButton();

        verify(mockButton, times(1)).setText(R.string.detail_create_encounter);
        verify(mockButton, times(1)).setOnClickListener(any(View.OnClickListener.class));
    }

    @Test
    public void setMemberActionLink() {
        CurrentMemberDetailPresenter currentMemberDetailPresenterSpy = spy(currentMemberDetailPresenter);
        when(currentMemberDetailPresenterSpy.getMemberActionLink()).thenReturn(mockTextView);

        currentMemberDetailPresenterSpy.setMemberActionLink();

        verify(mockTextView, times(1)).setVisibility(View.VISIBLE);
        verify(mockTextView, times(1)).setText(R.string.dismiss_patient);
        verify(mockTextView, times(1)).setOnClickListener(any(View.OnClickListener.class));
    }
}