package org.watsi.uhp.offline;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.IdentificationEventFactory;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.LabResult;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.watsi.uhp.CustomMatchers.withBillableName;
import static org.watsi.uhp.CustomMatchers.withEncounterItem;
import static org.watsi.uhp.CustomMatchers.withEncounterItemName;

@RunWith(AndroidJUnit4.class)
public class EncounterFlowFeature extends BaseTest {

    private Member member;
    private Billable billableDrug;
    private Billable billableLab;
    private Billable billableLabThatRequiresLabResult;
    private Billable billableSupply;

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, false);

    @Before
    public void setUpTest() throws SQLException, AbstractModel.ValidationException {
        member = Member.all(Member.class).get(0);
        billableDrug = BillableDao.findBillableByName("Quinine");
        billableLab = BillableDao.findBillableByName("CD4");
        billableLabThatRequiresLabResult = BillableDao.findBillableByName("Malaria (BS)");
        billableSupply = BillableDao.findBillableByName("Sutures");

        IdentificationEventFactory.createIdentificationEvent(member, 30);

        clinicActivityRule.launchActivity(null);
    }

    public void addBillable(Billable billable) throws InterruptedException {
        onView(withId(R.id.category_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(billable.getType().toString())))
                .perform(click());
        if (billable.getType() == Billable.TypeEnum.DRUG) {
            onView(withId(R.id.drug_search)).perform(typeText(billable.getName()));
            Thread.sleep(1000);
            onView(withText(billable.getName()))
                    .inRoot(withDecorView(not(is(clinicActivityRule.getActivity().getWindow().getDecorView()))))
                    .perform(click());
        } else {
            onData(allOf(is(instanceOf(Billable.class)), withBillableName(billable.getName()))).perform(click());
        }
    }

    public void addBillableLabWithLabResult(Billable billable, LabResult.LabResultEnum labResult) {
        onView(withId(R.id.category_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(billable.getType().toString())))
                .perform(click());
        onData(allOf(is(instanceOf(Billable.class)),
                withBillableName(billable.getName())))
                .perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(labResult.toString())))
                .perform(click());
    }

    public void removeBillable(Billable billable) {
        onData(withEncounterItemName(billable.getName()))
                .inAdapterView(withId(R.id.line_items_list))
                .onChildView(withId(R.id.remove_line_item_btn))
                .perform(click());
    }

    public void dismissMember() {
        // when the user decides to dismiss a patient, a confirmation screen appears
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.menu_dismiss_member)).perform(click());
        onView(withText(R.string.dismiss_member_alert)).check(matches(isDisplayed()));

        // after dismissing a patient, the user can see a confirmation toast and that the patient
        // is not in the current patients list
        onView(withText("Member on other phone")).inRoot(isDialog()).perform(click());
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));
        onView(withText(R.string.current_patients_empty_text)).check(matches(isDisplayed()));
    }

    private void searchForAndChooseDiagnosis(String query, String fullName) throws InterruptedException {
        onView(withId(R.id.diagnosis_search)).perform(typeText(query));
        Thread.sleep(1000);
        onView(withText(fullName)).inRoot(isPlatformPopup()).perform(click());
    }

    private void addNewBillable(String name, Billable.TypeEnum type, int price, String units, String composition) {
        onView(withId(R.id.add_billable_prompt)).perform(click());
        onView(withId(R.id.type_field)).perform(click());
        onData(allOf(is(instanceOf(String.class)),
                is(type.toString())))
                .perform(click());
        onView(withId(R.id.name_field)).perform(typeText(name));
        onView(withId(R.id.price_field)).perform(typeText(String.valueOf(price)));
        if (units != null) {
            onView(withId(R.id.unit_field)).perform(typeText(units));
        }

        if (composition != null) {
            onView(withId(R.id.list_of_compositions)).perform(click());
            onView(withText(composition)).inRoot(isPlatformPopup()).perform(click());
        }
        onView(withId(R.id.save_button)).perform(click());
    }

    @Test
    public void dismissMemberFlow() {
        // the user can see checked-in members
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        // when the user clicks on a checked-in member, their details appear
        onView(withText(member.getFullName())).perform(click());
        onView(withText(R.string.detail_fragment_label)).check(matches(isDisplayed()));

        // the user can dismiss the checked-in member without entering treatment info
        dismissMember();
    }

    @Test
    public void createEncounter_outpatientEncounterFlow() throws Exception {
        String defaultBillable1 = "Consultation";
        String defaultBillable2 = "Medical Form";

        // the user can see checked-in members
        onView(withText(member.getFullName())).check(matches(isDisplayed()));

        // when the user clicks on a checked-in member, their details appear
        onView(withText(member.getFullName())).perform(click());
        onView(withText(R.string.detail_fragment_label)).check(matches(isDisplayed()));

        // when the user proceeds to enter encounter information,
        // the presenting conditions show up with the fever question
        onView(withText(R.string.detail_create_encounter)).perform(click());
        onView(withText(R.string.fragment_presenting_conditions)).check(matches(isDisplayed()));

        // User can check and uncheck the fever check box.
        onView(withId(R.id.fever_checkbox)).check(matches(isNotChecked()));
        onView(withId(R.id.fever_checkbox)).perform(click());
        onView(withId(R.id.fever_checkbox)).check(matches(isChecked()));
        onView(withId(R.id.fever_checkbox)).perform(click());
        onView(withId(R.id.fever_checkbox)).check(matches(isNotChecked()));

        // when the user clicks next on the presenting conditions fragment
        // a list of encounter items appears with the default billables
        onView(withText(R.string.continue_encounter_button)).perform(click());
        onView(withText(R.string.encounter_fragment_label)).check(matches(isDisplayed()));
        assertItemInList(withEncounterItemName(defaultBillable1), R.id.line_items_list);
        assertItemInList(withEncounterItemName(defaultBillable2), R.id.line_items_list);

        // TODO: defaults do not appear for delivery

        // the user can billables to the list of encounters items
        addBillable(billableLab);
        assertItemInList(withEncounterItemName(billableLab.getName()), R.id.line_items_list);

        addBillable(billableSupply);
        assertItemInList(withEncounterItemName(billableSupply.getName()), R.id.line_items_list);


        // if the user selects the same billable twice, an error message appears
        addBillable(billableLab);
        assertDisplaysToast(clinicActivityRule, R.string.already_in_list_items);

        addBillable(billableDrug);
        assertItemInList(withEncounterItemName(billableDrug.getName()), R.id.line_items_list);

        addBillableLabWithLabResult(billableLabThatRequiresLabResult, LabResult.LabResultEnum.POSITIVE);
        assertItemInList(withEncounterItem(billableLabThatRequiresLabResult.getName(), 2000, "Positive"), R.id.line_items_list);

        // the user can remove billables
        removeBillable(billableSupply);
        assertItemNotInList(withEncounterItemName(billableSupply.getName()), R.id.line_items_list);

        removeBillable(billableDrug);
        assertItemNotInList(withEncounterItemName(billableDrug.getName()), R.id.line_items_list);

        // the user can add a billable again after removing it
        addBillable(billableSupply);
        assertItemInList(withEncounterItemName(billableSupply.getName()), R.id.line_items_list);

        // the user can add a new service billable with a custom name and amount
        addNewBillable("New Service Billable", Billable.TypeEnum.SERVICE, 1255, null, null);
        addNewBillable("New Drug Billable", Billable.TypeEnum.DRUG, 1599, "100mg", "syrup");
        addNewBillable("New Vaccine Billable", Billable.TypeEnum.VACCINE, 15000, "128mg", null);

        // scroll to the bottom.
        onData(anything()).inAdapterView(withId(R.id.line_items_list)).atPosition(5).perform(click());

        // make sure billables are displayed correctly in the encounter fragment.
        onView(withText("New Service Billable")).check(matches(isDisplayed()));
        onView(withText("New Drug Billable")).check(matches(isDisplayed()));
        onView(withText("100mg syrup")).check(matches(isDisplayed()));
        onView(withText("New Vaccine Billable")).check(matches(isDisplayed()));
        onView(withText("128mg vial")).check(matches(isDisplayed()));

        // TODO: the user cannot change the quantity of non-drugs

        // TODO: the user can change the date of the encounter for backfilling

        // the user can continue to choose diagnoses
        onView(withText(R.string.continue_encounter_button)).perform(click());
        onView(withText(R.string.diagnosis_fragment_label)).check(matches(isDisplayed()));
        onView(withId(R.id.diagnosis_search)).perform(click());
        // TODO: Submit three diagnosis.
        onView(withText("0 Diagnoses")).check(matches(isDisplayed()));
        searchForAndChooseDiagnosis("Malaria", "Severe Malaria");
        onView(withText("Severe Malaria")).check(matches(isDisplayed()));
        onView(withText("1 Diagnosis")).check(matches(isDisplayed()));
        searchForAndChooseDiagnosis("Cough", "Cough");
        onView(withText("Cough")).check(matches(isDisplayed()));
        onView(withText("2 Diagnoses")).check(matches(isDisplayed()));
        searchForAndChooseDiagnosis("uti", "Urinary Tract Infection");
        onView(withText("Urinary Tract Infection")).check(matches(isDisplayed()));
        onView(withText("3 Diagnoses")).check(matches(isDisplayed()));

        // Remove a diagnosis
        onData(anything()).inAdapterView(withId(R.id.selected_diagnosis_list)).onChildView(withId(R.id.remove_diagnosis_btn)).atPosition(1).perform(click());
        onView(withText("2 Diagnoses")).check(matches(isDisplayed()));
        onView(withText("Severe Malaria")).check(matches(isDisplayed()));
        onView(withText("Urinary Tract Infection")).check(matches(isDisplayed()));
        onView(withText("Cough")).check(doesNotExist());

        // the user can continue to take a form of the encounter
        onView(withText(R.string.continue_encounter_button)).perform(click());
        onView(withText(R.string.encounter_form_fragment_label)).check(matches(isDisplayed()));
        onView(withText(R.string.encounter_form_finish_btn)).perform(click());

        // the user can review all entered encounter line items in the receipt fragment and submit
        onView(withText(R.string.receipt_fragment_label)).check(matches(isDisplayed()));

        // all the billables, descriptions, and prices are displayed correctly in the list of receipt items.
        assertItemInList(withEncounterItem(defaultBillable1, 2000, null), R.id.receipt_items);
        assertItemInList(withEncounterItem(defaultBillable2, 1000, null), R.id.receipt_items);
        assertItemInList(withEncounterItem(billableLab.getName(), billableLab.getPrice(), null), R.id.receipt_items);
        assertItemInList(withEncounterItem(billableLabThatRequiresLabResult.getName(), billableLabThatRequiresLabResult.getPrice(), "Positive"), R.id.receipt_items);
        assertItemInList(withEncounterItem(billableSupply.getName(), billableSupply.getPrice(), null), R.id.receipt_items);
        assertItemInList(withEncounterItem("New Service Billable", 1255, null), R.id.receipt_items);
        assertItemInList(withEncounterItem("New Drug Billable", 1599, null), R.id.receipt_items);
        assertItemInList(withEncounterItem("New Vaccine Billable", 15000, null), R.id.receipt_items);

        // TODO: Check diagnosis in this fragment.
        onView(withText("2 Diagnoses")).check(matches(isDisplayed()));
        onView(withText("Severe Malaria")).check(matches(isDisplayed()));
        onView(withText("Urinary Tract Infection")).check(matches(isDisplayed()));
        onView(withText("Cough")).check(doesNotExist());
        onView(withText(R.string.save_encounter_button)).perform(click());

        // TODO: add quick toast check
        // no checked-in members
        onView(withText(R.string.current_patients_empty_text)).check(matches(isDisplayed()));
    }
}
