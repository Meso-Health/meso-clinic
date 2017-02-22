package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ClinicNumberFragmentFeature {

    private static final String CLINIC_NUMBER_TO_BE_TYPED = "10";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mActivityRule.getActivity();
        mainActivity.setClinicNumberFragment();
    }

    @Test
    public void inputNumber_clinicNumberFragment() {
        // type number in edittext view
        onView(withId(R.id.clinic_number_field)).perform(typeText(CLINIC_NUMBER_TO_BE_TYPED));

        // check that 'Continue' button appears
        onView(withId(R.id.clinic_number_continue_button)).check(matches(isDisplayed()));
    }

    @Test
    public void clickContinueButton_clinicNumberFragment() {
        // type number in edittext view
        onView(withId(R.id.clinic_number_field)).perform(typeText(CLINIC_NUMBER_TO_BE_TYPED));

        // click 'Continue' button
        onView(withId(R.id.clinic_number_continue_button)).perform(click());

        // check that encounter fragment opens (checking for fragment title)
        onView(withText(R.string.encounter_fragment_label)).check(matches(isDisplayed()));
    }
}
