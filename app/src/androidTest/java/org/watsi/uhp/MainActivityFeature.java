package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    private MainActivity mActivity = null;
    private Member mMember = null;

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mMember = createAndPersistUser();
    }

    @Ignore // ignoring due to issues with root views not working in CI environment
    @Test
    public void searchByMemberName_showsMemberSuggestions() throws Exception {
        // click on search icon
        onView(withId(R.id.search)).perform(click());

        // fill in search text
        onView(isAssignableFrom(EditText.class)).perform(typeText("Mem"));

        // check that member suggestions are displayed
        onView(withText("Member 1"))
                .inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkInMember_updatesLastCheckedInDate() throws Exception {
        mActivity.setDetailFragment(String.valueOf(mMember.getId()));

        onView(withText("Check-in")).perform(click());
        onView(withText("Admitted as inpatient")).perform(click());

        String dateString = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        onView(withId(R.id.member_last_check_in)).check(matches(withText(dateString)));
    }

    private Member createAndPersistUser() throws SQLException {
        DatabaseHelper.init(mActivity.getBaseContext());
        Member member = new Member();
        member.setName("Foo");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        member.setBirthdate(cal.getTime());
        MemberDao.create(member);
        return member;
    }
}
