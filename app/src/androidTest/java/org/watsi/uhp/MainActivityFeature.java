package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.basetests.ActivityTest;
import org.watsi.uhp.managers.NavigationManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityFeature {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class, false, true);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setLoginFragment();
    }

    @Test
    public void showsMainScreen() throws Exception {
        onView(withText(R.string.login_username_label)).check(matches(isDisplayed()));
    }
}
