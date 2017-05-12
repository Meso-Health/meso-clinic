package org.watsi.uhp;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.basetests.ActivityTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class LoginFeature extends ActivityTest {

    private static final String USERNAME = "klinik";
    private static final String PASSWORD = "123456";

    @Rule
    public ActivityTestRule<ClinicActivity> clinicActivityRule =
            new ActivityTestRule<>(ClinicActivity.class, false, true);

    @Test
    public void showsLoginScreen() throws Exception {
        onView(withText(R.string.login_username_label)).check(matches(isDisplayed()));
    }

    @Test
    public void logInAndLogOut_loginFlow() {
        logsUserIn();
        logsUserOut();
    }

    /**
     * logging in clinic user that is loaded in test seed data on the uhp rails backend side,
     * make sure you are running the rails server locally and have raked the seed data
     */
    public void logsUserIn() {
        onView(withId(R.id.login_username)).perform(typeText(USERNAME));
        onView(allOf(supportsInputMethods(), withParent(withId(R.id.login_password))))
                .perform(typeText(PASSWORD));
        onView(withId(R.id.login_button)).perform(click());
        waitForUIToUpdate();
        onView(withText("Select a patient")).check(matches(isDisplayed()));
    }

    public void logsUserOut() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Logout")).perform(click());
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
        onView(withText("User Login")).check(matches(isDisplayed()));
    }
}
