package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.managers.NavigationManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;

public class AddNewBillableFragmentFeature {

    private static final String BILLABLE_NAME_TO_BE_TYPED = "New lab";
    private static final String BILLABLE_PRICE_TO_BE_TYPED = "10";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setAddNewBillableFragment();
    }

    @Test
    public void inputName_addNewBillableFragment() {
        // type name in name field
        onView(withId(R.id.name_field)).perform(typeText(BILLABLE_NAME_TO_BE_TYPED));

        // check that 'Add' button does not appear
        onView(withId(R.id.add_billable_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void inputPrice_addNewBillableFragment() {
        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // check that 'Add' button does not appear
        onView(withId(R.id.add_billable_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void inputNameAndPrice_addNewBillableFragment() {
        // type name in name field
        onView(withId(R.id.name_field)).perform(typeText(BILLABLE_NAME_TO_BE_TYPED));

        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // check that 'Add' button appears
        onView(withId(R.id.add_billable_button)).check(matches(isDisplayed()));
    }

    @Test
    public void addsBillableSetUp_addNewBillableFragment() {
        // type name in name field
        onView(withId(R.id.name_field)).perform(typeText(BILLABLE_NAME_TO_BE_TYPED));

        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // click 'Add' button
        onView(withId(R.id.add_billable_button)).perform(click());

        // check that it opens encounter fragment (checking for fragment title)
        onView(withText(R.string.encounter_fragment_label)).check(matches(isDisplayed()));
    }

    @Test
    public void addsBillableName_addNewBillableFragment() {
        addsBillableSetUp_addNewBillableFragment();

        // check that it adds billable to encounter fragment (checking for new billable name)
        onView(withText(BILLABLE_NAME_TO_BE_TYPED)).check(matches(isDisplayed()));
    }

    @Test
    public void addsBillablePrice_addNewBillableFragment() {
        addsBillableName_addNewBillableFragment();

        // click 'Continue' button to continue to receipt
        onView(withId(R.id.save_encounter)).perform(click());

        // check that receipt has correct price of billable added
        onView(withId(R.id.receipt_billable_price_and_quantity)).check(matches(withText(containsString(BILLABLE_PRICE_TO_BE_TYPED))));
    }
}
