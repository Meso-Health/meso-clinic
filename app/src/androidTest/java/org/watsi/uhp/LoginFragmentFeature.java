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
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class LoginFragmentFeature extends ActivityTest {

    private static final String USERNAME = "klinik";
    private static final String PASSWORD = "123456";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setLoginFragment();
    }

    @Test
    /**
     * logging in clinic user that is loaded in test seed data on the uhp rails backend side, make sure you are running the rails server locally and have raked the seed data
     */
    public void logsUserIn_loginFragment() {
        onView(withId(R.id.login_username)).perform(typeText(USERNAME));

        onView(allOf(supportsInputMethods(), withParent(withId(R.id.login_password)))).perform(typeText(PASSWORD));

        onView(withId(R.id.login_button)).perform(click());

        waitForUIToUpdate();

        onView(withText("Select a patient")).check(matches(isDisplayed()));
    }
}
