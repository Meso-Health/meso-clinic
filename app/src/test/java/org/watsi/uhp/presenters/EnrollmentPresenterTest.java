package org.watsi.uhp.presenters;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class EnrollmentPresenterTest {

    private EnrollmentPresenter enrollmentPresenter;
    @Mock
    private Member mockMember;

    @Mock
    Context context;

    @Before
    public void setup() {
        initMocks(this);
        enrollmentPresenter = new EnrollmentPresenter(mockMember, context);
    }

    @Test
    public void confirmationToast_memberStillAbsentee() throws Exception {
        when(mockMember.isAbsentee()).thenReturn(true);

        enrollmentPresenter.confirmationToast().show();
        assertEquals(ShadowToast.getTextOfLatestToast(), "Any updates successfully saved");
    }

    @Test
    public void confirmationToast_memberNoLongerAbsentee() throws Exception {
        when(mockMember.isAbsentee()).thenReturn(false);

        enrollmentPresenter.confirmationToast().show();
        assertEquals(ShadowToast.getTextOfLatestToast(), "Enrollment completed");
    }

}
