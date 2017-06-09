package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class IdentificationFlowFeature extends BaseTest {

    private Member member;

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);

    @Before
    public void setUpTest() throws SQLException, AbstractModel.ValidationException {
        member = MemberDao.all().get(0);
    }

    @After
    public void cleanUpTest() throws SQLException {
        IdentificationEvent identification = IdentificationEventDao.openCheckIn(member.getId());
        if (identification != null) identification.delete();
    }

    public void performSearch(String query) {
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(query), pressImeActionButton());
        waitForUIToUpdate(1);
    }

    public void clearSearch() {
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());
    }

    public void checkingInPatient_idFlow(String name) {
        String opdNumber = "30";

        // when you click 'CHECK-IN', clinic number dialog comes up
        onView(withId(R.id.approve_identity)).perform(click());
        onView(withText(R.string.clinic_number_prompt)).check(matches(isDisplayed()));

        // when you enter OPD number and click 'SUBMIT', current patients fragment displays with
        // patient that you just checked in
        onView(withId(R.id.clinic_number_field)).perform(typeText(opdNumber));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));
        onView(withText(name)).check(matches(isDisplayed()));
    }

    @Test
    public void identificationByNameSearch_idFlow() {
        String notNameOfMember = "I am not a member";

        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // when you search a name not in the system, no results are found
        performSearch(notNameOfMember);
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        clearSearch();

        // when you search a name that belongs to a member, the member is found
        performSearch(member.getFullName());
        onView(withText(member.getFormattedCardId())).check(matches(isDisplayed()));

        // when you click on member found, their detail fragment displays with correct information
        onView(withText(member.getFormattedCardId())).perform(click());
        onView(withText(R.string.detail_fragment_label)).check(matches(isDisplayed()));
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        checkingInPatient_idFlow(member.getFullName());
    }

    @Test
    public void identificationByIdSearch_idFlow() {
        String notIdOfMember = "JWI000000";

        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // when you look up id not in system, no results found
        performSearch(notIdOfMember);
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        clearSearch();

        // when you look up id in system without spaces, member found
        performSearch(member.getCardId());
        onView(withText(member.getFullName())).check(matches(isDisplayed()));
        clearSearch();

        // when you look up id in system with spaces, member found
        performSearch(member.getFormattedCardId());
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        // when you click on member found, their detail fragment displays with correct information
        onView(withText(member.getFullName())).perform(click());
        onView(withText(R.string.detail_fragment_label)).check(matches(isDisplayed()));
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        checkingInPatient_idFlow(member.getFullName());
    }
}
