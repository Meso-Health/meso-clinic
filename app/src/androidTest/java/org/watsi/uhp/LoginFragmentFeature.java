package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
    public void logsUserIn_loginFragment() {
        // logging in clinic user that is loaded in test seed data on the uhp rails backend side, make sure you are running the server locally and have raked the seed data

        // type user's name in login username field
        onView(withId(R.id.login_username)).perform(typeText(USERNAME));

        // type user's password in password field
        onView(allOf(supportsInputMethods(), withParent(withId(R.id.login_password)))).perform(typeText(PASSWORD));

        // click Login button
        onView(withId(R.id.login_button)).perform(click());

        waitForUIToUpdate();

        // expect to log in
        onView(withText("Select a patient")).check(matches(isDisplayed()));
    }
}
