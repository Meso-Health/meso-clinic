package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by michaelliang on 6/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ MemberDetailPresenter.class })
public class MemberDetailPresenterTest {
    private MemberDetailPresenter memberDetailPresenter;

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
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Before
    public void setup() {
        initMocks(this);
        memberDetailPresenter = new MemberDetailPresenter(mockView, mockContext, mockMember, mockNavigationManager);
    }

    @Test
    public void setPatientCardNotifications_absentee() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doNothing().when(memberDetailPresenterSpy).showAbsenteeNotification();
        when(mockMember.isAbsentee()).thenReturn(true);

        memberDetailPresenterSpy.setPatientCardNotifications();

        verify(memberDetailPresenterSpy, times(1)).showAbsenteeNotification();
    }

    @Test
    public void setPatientCardNotifications_notAbsentee() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doNothing().when(memberDetailPresenterSpy).showReplaceCardNotification();
        when(mockMember.isAbsentee()).thenReturn(false);

        memberDetailPresenterSpy.setPatientCardNotifications();

        verify(memberDetailPresenterSpy, times(1)).showReplaceCardNotification();
    }

    @Test
    public void setPatientCardPhoto_noPhoto() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockMember.getPhotoBitmap(mockContentResolver)).thenReturn(null);

        doNothing().when(memberDetailPresenterSpy).setPatientCardPhotoAsDefault();

        memberDetailPresenterSpy.setPatientCardPhoto();

        verify(memberDetailPresenterSpy, times(1)).setPatientCardPhotoAsDefault();
        verify(memberDetailPresenterSpy, never()).setPatientCardPhotoBitmap(any(Bitmap.class));
    }

    @Test
    public void setPatientCardPhoto_withPhoto() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(mockMember.getPhotoBitmap(mockContentResolver)).thenReturn(mockPatientPhotoBitmap);

        doNothing().when(memberDetailPresenterSpy).setPatientCardPhotoBitmap(mockPatientPhotoBitmap);

        memberDetailPresenterSpy.setPatientCardPhoto();

        verify(memberDetailPresenterSpy, times(1)).setPatientCardPhotoBitmap(mockPatientPhotoBitmap);
        verify(memberDetailPresenterSpy, never()).setPatientCardPhotoAsDefault();
    }

    @Test
    public void setPatientCardTextFields() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        TextView mockTextView = mock(TextView.class);
        when(memberDetailPresenter.getMemberNameDetailTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberAgeAndGenderTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberCardIdDetailTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberPhoneNumberTextView()).thenReturn(mockTextView);

        when(mockMember.getFullName()).thenReturn("mockName");
        when(mockMember.getFormattedAgeAndGender()).thenReturn("25 F");
        when(mockMember.getFormattedCardId()).thenReturn("MOCK123123");
        when(mockMember.getFormattedPhoneNumber()).thenReturn("123456789");

        memberDetailPresenterSpy.setPatientCardTextFields();

        verify(mockTextView, times(1)).setText("mockName");
        verify(mockTextView, times(1)).setText("25 F");
        verify(mockTextView, times(1)).setText("MOCK123123");
        verify(mockTextView, times(1)).setText("123456789");

        verify(memberDetailPresenterSpy, times(1)).getMemberNameDetailTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberAgeAndGenderTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberCardIdDetailTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberPhoneNumberTextView();
    }
}
