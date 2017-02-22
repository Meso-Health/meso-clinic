package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.MemberAdapter;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withTitle;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MainActivityFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class, false, true);

    public void clickCheckInButton() {

    }

    @Test
    public void showsMainScreen() throws Exception {
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));
    }
//    @Test
//    public void clickCurrentPatient_currentPatientsFragment() {
//        // click on a current patient
//        onData(withTitle(R.id.detail_fragment))
//                .atPosition(0)
//                .perform(click());
//
//        // check that clinic number fragment opens (checking for fragment title)
//        onView(withText(R.string.clinic_number_fragment_label)).check(matches(isDisplayed()));
//    }

    @Test
    public void inputNumber_clinicNumberFragment() {
        // type number in edittext view
        onView(withId(R.id.clinic_number_field)).perform(typeText(CLINIC_NUMBER_TO_BE_TYPED));

        // check that 'Continue' button appears
        onView(withId(R.id.clinic_number_continue_button)).check(matches(isDisplayed()));
    }
}
