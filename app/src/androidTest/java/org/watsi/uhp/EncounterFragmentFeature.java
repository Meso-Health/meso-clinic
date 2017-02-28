package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.watsi.uhp.models.Billable.TypeEnum.SERVICE;

@RunWith(AndroidJUnit4.class)
public class EncounterFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mActivityRule.getActivity();
        new NavigationManager(mainActivity).setEncounterFragment();
    }

    @Test
    public void selectService_encounterFragment() {
        // click on spinner to open
        onView(withId(R.id.category_spinner)).perform(click());

        // click on Service category in dropdown
        onData(anything())
                .inAdapterView(withId(16908308))
                .atPosition(1)
                .perform(click());

        List<Billable> billables = new ArrayList<>();
        try {
            billables.addAll(BillableDao.getBillablesByCategory(SERVICE));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        // click on billable at position 2 in dropdown
        onData(anything())
                .inAdapterView(withId(R.id.billable_spinner))
                .atPosition(2)
                .perform(click());

        // check that the billable at that
        onView(withText(billables.get(1).toString())).check(matches(isDisplayed()));

    }

}
