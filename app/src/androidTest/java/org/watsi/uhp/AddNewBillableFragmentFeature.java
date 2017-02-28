package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.managers.NavigationManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

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
        onView(withId(R.id.add_billable_button)).check(doesNotExist());
    }

    @Test
    public void inputPrice_addNewBillableFragment() {
        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // check that 'Add' button does not appear
        onView(withId(R.id.add_billable_button)).check(doesNotExist());
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
}
