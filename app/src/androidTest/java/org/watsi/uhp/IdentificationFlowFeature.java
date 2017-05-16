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
import org.watsi.uhp.basetests.ActivityTest;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.managers.ExceptionManager;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

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
public class IdentificationFlowFeature extends ActivityTest {

    private final String username = "klinik";
    private final String password = "123456";
    private final String opdNumber = "30";
    private final String notNameOfMember = "I am not a member";
    private final String nameOfMember = "Lil Jon";
    private final String notCardIdOfMember = "JWI000000";
    private final String cardIdOfMember = "RWI 000 000";

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);

    @Before
    public void start_idFlow() {
        LoginFeature.logsUserIn(username, password);
    }

    @After
    public void end_idFlow() throws SQLException {
        // if when trying to delete, it says Identification Event is null, this may mean it is not saving it to the phone properly
        IdentificationEventDao.deleteById(new HashSet<>(Arrays.asList(getIdEvent(getMember("RWI000000")).getId())));

        LoginFeature.logsUserOut();
    }

    @Test
    public void identificationByNameSearch_idFlow() {
        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // asserts that when you look up name not in system, no results found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(notNameOfMember), pressImeActionButton());
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());

        // asserts that when you look up name that belongs to member, member found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(nameOfMember), pressImeActionButton());
        waitForUIToUpdate();
        onView(withText(cardIdOfMember)).check(matches(isDisplayed()));

        // asserts that when you click on member found, their detail fragment displays with correct information
        onView(withText(cardIdOfMember)).perform(click());
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText(nameOfMember)).check(matches(isDisplayed()));

        checkingInPatient_idFlow(nameOfMember);
    }

    @Test
    public void identificationByIdSearch_idFlow() {
        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // asserts that when you look up id not in system, no results found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(notCardIdOfMember), pressImeActionButton());
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());

        // asserts that when you look up id in system with spaces, member found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(cardIdOfMember), pressImeActionButton());
        waitForUIToUpdate();
        onView(withText(nameOfMember)).check(matches(isDisplayed()));

        // asserts that when you click on member found, their detail fragment displays with correct information
        onView(withText(nameOfMember)).perform(click());
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText(nameOfMember)).check(matches(isDisplayed()));

        checkingInPatient_idFlow(nameOfMember);
    }

    public void checkingInPatient_idFlow(String name) {
        // asserts that when you click 'CHECK-IN', clinic number dialog comes up
        onView(withId(R.id.approve_identity)).perform(click());
        onView(withText("Enter the patient's clinic number")).check(matches(isDisplayed()));

        // asserts that when you enter OPD number and click 'SUBMIT', current patients fragment displays with patient that you just checked in
        onView(withId(R.id.clinic_number_field)).perform(typeText(opdNumber));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText("Select a patient")).check(matches(isDisplayed()));
        onView(withText(name)).check(matches(isDisplayed()));
    }
}

