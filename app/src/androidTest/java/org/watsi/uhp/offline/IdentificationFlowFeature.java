package org.watsi.uhp.offline;

import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Tier;
import com.simprints.libsimprints.Verification;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.watsi.uhp.CustomMatchers.withMemberId;


@RunWith(AndroidJUnit4.class)
public class IdentificationFlowFeature extends BaseTest {

    private Member member;
    private Matcher<Intent> verifyIntentMatcher;

    @Rule
    public IntentsTestRule<ClinicActivity> clinicActivityRule =
            new IntentsTestRule<>(ClinicActivity.class, false, true);

    @Before
    public void setUpTest() throws SQLException, AbstractModel.ValidationException {
        member = MemberDao.all().get(0);
        verifyIntentMatcher =
                allOf(hasAction(Constants.SIMPRINTS_VERIFY_INTENT),
                        hasExtras(allOf(
                                hasEntry(equalTo(Constants.SIMPRINTS_API_KEY), equalTo
                                        (BuildConfig.SIMPRINTS_API_KEY)),
                                hasEntry(equalTo(Constants.SIMPRINTS_USER_ID), equalTo(TEST_USER_NAME)),
                                hasEntry(equalTo(Constants.SIMPRINTS_MODULE_ID), equalTo
                                        (BuildConfig.PROVIDER_ID.toString())),
                                hasEntry(equalTo(Constants.SIMPRINTS_VERIFY_GUID), equalTo(member.getFingerprintsGuid().toString())))
                        ));
    }

    @After
    public void cleanUpTest() throws SQLException {
        IdentificationEvent identification = IdentificationEventDao.openCheckIn(member.getId());
        if (identification != null) identification.delete();
    }

    public void performSearch(String query) {
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(query), pressImeActionButton());
    }

    public void clearSearch() {
        onView(withId(R.id.member_search)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(clearText());
    }

    public void checkInPatient(Member member) {
        String opdNumber = "30";

        // when the user decides to check-in a patient, a prompt to enter clinic number appears
        onView(withText(R.string.check_in)).perform(click());
        onView(withText(R.string.clinic_number_prompt)).check(matches(isDisplayed()));

        // after checking in a patient, the user can see a confirmation toast and the patient in
        // the current patients list
        onView(withId(R.id.clinic_number_field)).perform(typeText(opdNumber));
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));

        // TODO: not working currently because feature tests run so quickly that a previous toast
        // is still on the screen when this one is supposed to be launched.
//        assertDisplaysToast(clinicActivityRule, member.getFullName() + " " + getInstrumentation()
//                        .getTargetContext().getString(R.string.identification_approved));
        assertItemInList(withMemberId(member.getId()), R.id.current_patients);
    }

    public void reportPatient() {
        // when the user decides to report the patient, a confirmation screen appears
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.menu_report_member)).perform(click());
        onView(withText(R.string.reject_identity_alert)).check(matches(isDisplayed()));

        // after reporting a patient, the user can see a confirmation toast and that the patient
        // is not in the current patients list
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));
        onView(withText(R.string.current_patients_empty_text)).check(matches(isDisplayed()));
    }

    public void scanFingerprintWithResult(int resultCode, boolean scanSuccess) {
        Intent resultIntent = new Intent();
        Verification results;

        if (scanSuccess) {
            results = new Verification(120, Tier.TIER_1, member.getFingerprintsGuid().toString());
        } else {
            results = new Verification(15, Tier.TIER_5, member.getFingerprintsGuid().toString());
        }

        resultIntent.putExtra(Constants.SIMPRINTS_VERIFICATION, results);
        ActivityResult activityResult = new ActivityResult(resultCode, resultIntent);

        intending(verifyIntentMatcher).respondWith(activityResult);

        // click scan fingerprint button
        onView(withId(R.id.scan_fingerprints_btn)).perform(click());

        // checks that correct fingerprint intent was sent
        intended(verifyIntentMatcher);

        // checks that correct fingerprint result appears
        if (scanSuccess) {
            onView(withText(R.string.good_scan_indicator)).check(matches(isDisplayed()));
        } else {
            onView(withText(R.string.bad_scan_indicator)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void identificationByNameSearch() {
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

        scanFingerprintWithResult(Activity.RESULT_OK, true);
        checkInPatient(member);
    }

    @Test
    public void identificationByIdSearch() {
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

        scanFingerprintWithResult(Activity.RESULT_OK, false);
        reportPatient();
    }

    //TODO: test absentee flow

    //TODO: test no match flow
}
