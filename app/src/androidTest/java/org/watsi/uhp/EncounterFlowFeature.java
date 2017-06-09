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
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class EncounterFlowFeature extends ActivityTest {

    private Member member;
    private Billable billableDrug;
    private Billable billableLab;
    private IdentificationEvent idEvent;

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, false);

    @Before
    public void setUpTest() throws SQLException, AbstractModel.ValidationException {
        member = MemberDao.all().get(0);
        billableDrug = BillableDao.getBillablesByCategory(Billable.TypeEnum.DRUG).get(0);
        billableLab = BillableDao.getBillablesByCategory(Billable.TypeEnum.LAB).get(0);

        idEvent = new IdentificationEventFactory(
                member,
                30
        );
        IdentificationEventDao.create(idEvent);

        clinicActivityRule.launchActivity(null);
    }

    @After
    public void cleanUpTest() throws SQLException {
        idEvent.delete();
    }

    @Test
    public void createEncounter_encounterFlow() {
        String defaultBillable1 = "Consultation";
        String defaultBillable2 = "Medical Form";

        // the user can see checked-in members
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        // when the user clicks on a checked-in member, their details appear
        onView(withText(member.getFullName())).perform(click());
        onView(withText(R.string.detail_fragment_label)).check(matches(isDisplayed()));

        // when the user proceeds to enter encounter information, a list of encounter items
        // appears with the default billables
        onView(withText(R.string.detail_create_encounter)).perform(click());
        onView(withText(R.string.encounter_fragment_label)).check(matches(isDisplayed()));
        onData(CustomMatchers.withEncounterItemName(defaultBillable1))
                .inAdapterView(withId(R.id.line_items_list))
                .check(matches(isDisplayed()));
        onData(CustomMatchers.withEncounterItemName(defaultBillable2))
                .inAdapterView(withId(R.id.line_items_list))
                .check(matches(isDisplayed()));

        // TODO: defaults do not appear for delivery

        // the user can add a drug to the list of encounter items by searching by drug name
        onView(withId(R.id.category_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(Billable.TypeEnum.DRUG.toString())))
                .perform(click());
        onView(withId(R.id.drug_search)).perform(typeText(billableDrug.getName()));
        onView(withText(billableDrug.getName()))
                .inRoot(withDecorView(not(is(clinicActivityRule.getActivity().getWindow()
                        .getDecorView()))))
                .perform(click());
        onData(CustomMatchers.withEncounterItemName(billableDrug.getName()))
                .inAdapterView(withId(R.id.line_items_list))
                .check(matches(isDisplayed()));

        // the user can add non-drugs to the list of encounters items by selecting them from a
        // drop-down
        onView(withId(R.id.category_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(Billable.TypeEnum.LAB.toString())))
                .perform(click());
        onData(allOf(is(instanceOf(Billable.class)),
                CustomMatchers.withBillableName(billableLab.getName())))
                .perform(click());
        onData(CustomMatchers.withEncounterItemName(billableLab.getName()))
                .inAdapterView(withId(R.id.line_items_list))
                .check(matches(isDisplayed()));

        // TODO: the user can change the quantity of drugs to a positive number

        // TODO: the user cannot change the quantity of non-drugs

        // TODO: if the user selects the same billable twice, an error message appears

        // TODO: the user can add a new billable

        // the user can continue to take a form of the encounter
        onView(withText(R.string.continue_encounter_button)).perform(click());
        onView(withText(R.string.encounter_form_fragment_label)).check(matches(isDisplayed()));
        onView(withText(R.string.encounter_form_finish_btn)).perform(click());

        // the user can review all entered encounter line items in the receipt fragment and submit
        onView(withText(R.string.receipt_fragment_label)).check(matches(isDisplayed()));
        onView(withText(defaultBillable1)).check(matches(isDisplayed()));
        onView(withText(defaultBillable2)).check(matches(isDisplayed()));
        onView(withText(billableDrug.getName())).check(matches(isDisplayed()));
        onView(withText(billableLab.getName())).check(matches(isDisplayed()));
        onView(withText(R.string.save_encounter_button)).perform(click());

        // no checked-in members
        onView(withText(R.string.current_patients_empty_text)).check(matches(isDisplayed()));
    }
}
