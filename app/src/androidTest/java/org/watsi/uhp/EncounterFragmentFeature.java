package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rollbar.android.Rollbar;
import com.squareup.haha.perflib.Main;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.watsi.uhp.models.Billable.CategoryEnum.SERVICES;

@RunWith(AndroidJUnit4.class)
public class EncounterFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        mainActivity = mActivityRule.getActivity();
        mainActivity.setEncounterFragment();
    }

    @Test
    public void selectService_encounterFragment() {
        // click on spinner to open
        onView(withId(R.id.category_spinner)).perform(click());

        // click on Service category in dropdown
        onData(anything())
                .inAdapterView(withId(R.id.category_spinner))
                .atPosition(1)
                .perform(click());

        List<Billable> billables = new ArrayList<>();
        try {
            billables.addAll(BillableDao.getBillablesByCategory(SERVICES));
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
