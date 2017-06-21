package org.watsi.uhp.presenters;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;
import org.watsi.uhp.models.Member;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class EnrollmentPresenterTest {

    private EnrollmentPresenter enrollmentPresenter;
    private Member member;

    @Mock
    Context context;

    @Before
    public void setup() {
        member = new Member();
        member.setAge(40);

        enrollmentPresenter = new EnrollmentPresenter(member, context);
    }

    @Test
    public void confirmationToast_memberStillAbsentee() throws Exception {
        member.setPhotoUrl("fake photo url");

        enrollmentPresenter.confirmationToast().show();
        assertEquals(ShadowToast.getTextOfLatestToast(), "Any updates successfully saved");
    }

    @Test
    public void confirmationToast_memberNoLongerAbsentee() throws Exception {
        member.setPhotoUrl("fake photo url");
        member.setFingerprintsGuid(UUID.randomUUID());

        enrollmentPresenter.confirmationToast().show();
        assertEquals( ShadowToast.getTextOfLatestToast(), "Enrollment completed");
    }

}
