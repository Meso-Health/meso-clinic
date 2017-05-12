package org.watsi.uhp;

import android.database.SQLException;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.ExceptionManager;

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
public class IdentificationFlowFeature extends LoginFeature {

    private static final String OPD_NUMBER = "30";
    private static final String NOT_NAME_OF_MEMBER = "I am not a member";
    private static final String NAME_OF_MEMBER = "Lil Jon";
    private static final String NOT_ID_OF_MEMBER = "JWI000000";
    private static final String ID_OF_MEMBER = "RWI 000 000";

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);
    private ClinicActivity clinicActivity;

    @Before
    public void setup() {
        logsUserIn();
    }

    @After
    public void teardown() {
        logsUserOut();
    }

    @Test
    public void identificationByNameSearch_idFlow() {
        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // asserts that when you look up name not in system, no results found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(NOT_NAME_OF_MEMBER), pressImeActionButton());
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());

        // asserts that when you look up name that belongs to member, member found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(NAME_OF_MEMBER), pressImeActionButton());
        waitForUIToUpdate();
        onView(withText(ID_OF_MEMBER)).check(matches(isDisplayed()));

        // asserts that when you click on member found, their detail fragment displays with correct information
        onView(withText(ID_OF_MEMBER)).perform(click());
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText(NAME_OF_MEMBER)).check(matches(isDisplayed()));

        checkingInPatient_idFlow(NAME_OF_MEMBER);
    }

    @Test
    public void identificationByIdSearch_idFlow() {
        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // asserts that when you look up id not in system, no results found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(NOT_ID_OF_MEMBER), pressImeActionButton());
        onView(withText(R.string.member_no_search_results_text)).check(matches(isDisplayed()));
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());

        // asserts that when you look up id in system with spaces, member found
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText("RWI000000"), pressImeActionButton());
        waitForUIToUpdate();
        onView(withText(NAME_OF_MEMBER)).check(matches(isDisplayed()));

        // asserts that when you click on member found, their detail fragment displays with correct information
        onView(withText(NAME_OF_MEMBER)).perform(click());
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText(NAME_OF_MEMBER)).check(matches(isDisplayed()));

        checkingInPatient_idFlow(NAME_OF_MEMBER);
    }

    @Test
    public void identificationByBarcodeScan_idFlow() {
        onView(withId(R.id.identification_button)).perform(click());
        onView(withId(R.id.search_member)).perform(click());

        // asserts that when you click 'LOOK UP PATIENT', camera opens
        onView(withId(R.id.barcode_preview_surface)).check(matches(isDisplayed()));

        // asserts that when you scan invalid barcode, error message displays
        onView(withText(R.string.id_not_found_toast)).check(matches(isDisplayed()));

        // asserts that when you scan valid barcode, member's detail fragment appears
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText(NAME_OF_MEMBER)).check(matches(isDisplayed()));

        checkingInPatient_idFlow("placeholder");
    }

    public void checkingInPatient_idFlow(String name) {
        // asserts that when you click 'CHECK-IN', clinic number dialog comes up
        onView(withId(R.id.approve_identity)).perform(click());
        onView(withText("Enter the patient's clinic number")).check(matches(isDisplayed()));

        // asserts that when you enter OPD number and click 'SUBMIT', current patients fragment displays with patient that you just checked in
        onView(withId(R.id.clinic_number_field)).perform(typeText(OPD_NUMBER));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText("Select a patient")).check(matches(isDisplayed()));
        onView(withText(name)).check(matches(isDisplayed()));
    }
}

