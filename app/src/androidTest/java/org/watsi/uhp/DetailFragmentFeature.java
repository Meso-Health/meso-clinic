package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class DetailFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        Member member = new Member();
        UUID memberId = UUID.randomUUID();
        String memberIdString = memberId.toString();
        member.setId(memberId);
        member.setAge(69);
        member.setFullName("Lil Jon");
        member.setGender(Member.GenderEnum.M);
        member.setCardId("RWI000000");
        member.setAbsentee(false);
        UUID membersHouseholdId = UUID.randomUUID();
        member.setHouseholdId(membersHouseholdId);

        try {
            MemberDao.createOrUpdate(member);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        Member member2 = new Member();
        UUID member2Id = UUID.randomUUID();
        member2.setId(member2Id);
        member2.setAge(10);
        member2.setFullName("Big Jon");
        member.setGender(Member.GenderEnum.F);
        member2.setCardId("RWI999999");
        member2.setAbsentee(false);
        member2.setHouseholdId(membersHouseholdId);

        try {
            MemberDao.createOrUpdate(member);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        IdentificationEvent.SearchMethodEnum searchMethodEnum = IdentificationEvent.SearchMethodEnum.SEARCH_NAME;
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setDetailFragment(memberIdString, searchMethodEnum, null);
    }

    @Test
    public void showsCorrectMember_detailFragment() {
        // shows correct member name
        onView(withId(R.id.member_name)).check(matches(withText(containsString("Lil Jon"))));

        // shows correct member age
        onView(withId(R.id.member_age)).check(matches(withText(containsString("69"))));

        // shows correct member gender
        onView(withId(R.id.member_gender)).check(matches(withText(containsString("M"))));

        // shows correct card Id
        onView(withId(R.id.member_card_id)).check(matches(withText(containsString("RWI000000"))));
    }

    @Test
    public void showsCorrectHouseholdMember_detailFragment() {
        // shows correct number of household members
        onView(withId(R.id.household_members)).check(matches(withText(containsString("2"))));

        // shows correct household member info
        onView(withId(R.id.member_name)).check(matches(withText(containsString("Big Jon"))));
        onView(withId(R.id.member_age_and_gender)).check(matches(withText(containsString("Big Jon"))));

    }
}
