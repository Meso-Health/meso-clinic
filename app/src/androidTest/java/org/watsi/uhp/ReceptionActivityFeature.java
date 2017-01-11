package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;
import android.widget.EditText;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.ReceptionActivity;
import org.watsi.uhp.database.DatabaseHelper;
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
public class ReceptionActivityFeature {

    @Rule
    public ActivityTestRule<ReceptionActivity> mActivityRule =
            new ActivityTestRule<ReceptionActivity>(ReceptionActivity.class);

    private ReceptionActivity mActivity = null;
    private Member mMember = null;

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        mMember = createAndPersistUser();
    }

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
        // set member detail view to created member
        mActivity.runOnUiThread(new Runnable() {
           public void run() {
               try {
                   mActivity.setMember(mMember.getId());
               } catch (SQLException e) {
                   e.printStackTrace();
               }
           }
       });

        onView(withText("Check-in")).perform(click());
        onView(withText("Admitted as inpatient")).perform(click());

        String dateString = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());
        onView(withId(R.id.member_last_check_in)).check(matches(withText(dateString)));
    }

    private Member createAndPersistUser() throws SQLException {
        DatabaseHelper dbHelper = new DatabaseHelper(mActivity);
        Dao<Member, Integer> memberDao = dbHelper.getMemberDao();
        Member member = new Member();
        member.setName("Foo");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        member.setBirthdate(cal.getTime());
        memberDao.create(member);
        return member;
    }
}
