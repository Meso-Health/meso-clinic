package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.basetests.ActivityTest;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EncounterFlowFeature extends ActivityTest {

    private final String username = "klinik";
    private final String password = "123456";
    private final String cardId = "RWI000000";
    private final String nameOfMember = "Lil Jon";
    private final int opdNumber = 30;
    private final String drugName = "phenobarbital";
    private final String defaultBillable1 = "Consultation";
    private final String defaultBillable2 = "Medical Form";

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);

    @Before
    public void start_encounterFlow() throws SQLException {
        IdentificationEvent idEvent = IdentificationEventFactory.createIdentificationEvent(getMember(cardId), opdNumber);
        IdentificationEventDao.create(idEvent);

        LoginFeature.logsUserIn(username, password);
    }

    @After
    public void end_encounterFlow() throws SQLException {
//        IdentificationEventDao.deleteById(new HashSet<>(Arrays.asList(getIdEvent(getMember("RWI000000")).getId())));
//
//        LoginFeature.logsUserOut();
    }

    @Test
    public void createEncounter_encounterFlow() {
        // asserts that member appears in current members
        onView(withText(nameOfMember)).check(matches(isDisplayed()));
        onView(withText(nameOfMember)).perform(click());

        // asserts that
        onView(withText("Is this the right person?")).check(matches(isDisplayed()));
        onView(withText("Enter treatment information")).perform(click());

        onView(withText(nameOfMember)).check(matches(isDisplayed()));
        onView(withText(defaultBillable1)).check(matches(isDisplayed()));
        onView(withText(defaultBillable2)).check(matches(isDisplayed()));
        onView(withText("Select a category...")).perform(click());
        onView(withText("DRUG")).perform(click());
        onView(withId(R.id.drug_search)).perform(typeText(drugName));
        onView(withText("30mg tablet")).perform(click());

        onView(withText("Continue")).perform(click());
        onView(withText("Take photo of form")).check(matches(isDisplayed()));
        onView(withText("Finish")).perform(click());

        onView(withText("Review receipt")).check(matches(isDisplayed()));
        onView(withText(defaultBillable1)).check(matches(isDisplayed()));
        onView(withText(defaultBillable2)).check(matches(isDisplayed()));
        onView(withText(drugName)).check(matches(isDisplayed()));
        onView(withText("Submit")).perform(click());

        onView(withText("There are no checked-in patients.")).check(matches(isDisplayed()));
    }
}
