package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

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
    public void showsMembersByRecentEncounter() throws Exception {
        // check to see that member is displayed in recent members list
        onData(anything()).
                inAdapterView(withId(R.id.recent_members)).
                atPosition(0).
                onChildView(withId(android.R.id.text1)).
                check(matches(withText("Foo Bar")));
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
