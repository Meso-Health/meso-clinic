package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.j256.ormlite.table.TableUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

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
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity mActivity = null;

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityRule.getActivity();
        createAndPersistUser();
    }

    @Test
    public void searchByMemberName_showsMemberSuggestions() throws Exception {
        // click on search icon
        onView(withId(R.id.search)).perform(click());

        // fill in search text
        onView(isAssignableFrom(EditText.class)).perform(typeText("Foo"));

        // click on member in suggested options dropdown
        onView(withText("Foo Bar"))
                .inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView()))))
                .perform(click());

        // check that detail view is shown (can see member age)
        onView(withText("22"))
                .check(matches(isDisplayed()));

    }

    private Member createAndPersistUser() throws SQLException {
        DatabaseHelper.init(mActivity.getBaseContext());
        TableUtils.clearTable(DatabaseHelper.getHelper().getConnectionSource(), Member.class);
        Member member = new Member();
        member.setId("3293-3249-9348");
        member.setCardId("RWI8581734");
        member.setName("Foo Bar");
        member.setAge(22);
        MemberDao.create(member);
        return member;
    }
}
