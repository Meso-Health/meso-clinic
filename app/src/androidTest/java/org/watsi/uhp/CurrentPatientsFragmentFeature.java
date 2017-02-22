package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class CurrentPatientsFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mActivityRule.getActivity();
    }

    @Test
    public void showsMainScreen() throws Exception {
        // checks that fragment title appears
        onView(withText(R.string.current_patients_fragment_label)).check(matches(isDisplayed()));
    }

    @Test
    public void clickCheckInButton_currentPatientsFragment() {
        // click 'Check in New Patient' button
        onView(withId(R.id.identification_button)).perform(click());

        // check that barcode fragment opens (checking for fragment title)
        onView(withText(R.string.barcode_fragment_label)).check(matches(isDisplayed()));
    }

    @Test
    public void clickCurrentPatient_currentPatientsFragment() {
        // click on a current patient
        onData(anything())
                .inAdapterView(withId(R.id.current_patients))
                .atPosition(0)
                .perform(click());

        // check that clinic number fragment opens (checking for fragment title)
        onView(withText(R.string.clinic_number_fragment_label)).check(matches(isDisplayed()));
    }
}
