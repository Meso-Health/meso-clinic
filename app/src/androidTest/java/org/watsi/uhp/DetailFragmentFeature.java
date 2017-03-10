package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import com.rollbar.android.Rollbar;

import org.junit.After;
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
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class DetailFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    @Before
    public void setup() {
        String fullName = "Lil Jon";
        String cardId = "RWI000000";
        int age = 69;
        UUID memberId = UUID.randomUUID();
        UUID membersHouseholdId = UUID.randomUUID();

        Member member = new Member(fullName, cardId, age, Member.GenderEnum.M);
        member.setId(memberId);
        member.setAbsentee(false);
        member.setHouseholdId(membersHouseholdId);

        try {
            MemberDao.createOrUpdate(member);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        String fullName2 = "Big Jon";
        String cardId2 = "RWI999999";
        int age2 = 10;
        UUID memberId2 = UUID.randomUUID();

        Member member2 = new Member(fullName2, cardId2, age2, Member.GenderEnum.F);
        member2.setId(memberId2);
        member2.setAbsentee(false);
        member2.setHouseholdId(membersHouseholdId);

        try {
            MemberDao.createOrUpdate(member2);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        IdentificationEvent.SearchMethodEnum searchMethodEnum = IdentificationEvent.SearchMethodEnum.SEARCH_NAME;
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setDetailFragment(memberId, searchMethodEnum, null);
    }

    @After
    public void deleteCreatedModels() {
        // TODO: delete created models?
    }

    @Test
    public void showsCorrectMember_detailFragment() {
        // shows correct member name
        onView(withId(R.id.member_name_detail_fragment)).check(matches(withText(containsString("Lil Jon"))));

        // shows correct member age
        onView(withId(R.id.member_gender_and_age)).check(matches(withText(containsString("69"))));

        // shows correct member gender
        onView(withId(R.id.member_gender_and_age)).check(matches(withText(containsString("Male"))));

        // shows correct card Id
        onView(withId(R.id.member_card_id)).check(matches(withText(containsString("RWI 000 000"))));
    }

    @Test
    public void showsCorrectHouseholdMember_detailFragment() {
        // shows correct number of household members
        onView(withId(R.id.household_members_label)).check(matches(withText(containsString("2"))));

        // shows correct household member
        onView(withId(R.id.member_name)).check(matches(withText(containsString("Big Jon"))));
    }
}
