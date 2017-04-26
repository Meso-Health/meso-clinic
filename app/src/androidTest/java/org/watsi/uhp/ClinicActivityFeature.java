package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ClinicActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ClinicActivityFeature {

    @Rule
    public ActivityTestRule<ClinicActivity> mActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);

    @Test
    public void promptsUserToLogin() throws Exception {
        onView(withText(R.string.authentication_activity_label)).check(matches(isDisplayed()));
    }
}
