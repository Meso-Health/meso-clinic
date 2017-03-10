package org.watsi.uhp;

import android.support.test.rule.ActivityTestRule;

import com.rollbar.android.Rollbar;
import com.squareup.haha.perflib.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsNot.not;

public class AddNewBillableFragmentFeature {

    private static final String BILLABLE_NAME_TO_BE_TYPED = "New lab";
    private static final String BILLABLE_PRICE_TO_BE_TYPED = "10";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;
    private Member member;

    @Before
    public void setup() {
        mainActivity = mainActivityRule.getActivity();
        new NavigationManager(mainActivity).setAddNewBillableFragment();

        String fullName = "Lil Jon";
        String cardId = "RWI000000";
        int age = 69;
        UUID memberId = UUID.randomUUID();

        member = new Member(fullName, cardId, age, Member.GenderEnum.M);
        member.setId(memberId);
        member.setAbsentee(false);

        try {
            MemberDao.createOrUpdate(member);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        UUID identificationId = UUID.randomUUID();
        Date occurredAt =  new GregorianCalendar(2017, Calendar.FEBRUARY, 11).getTime();

        IdentificationEvent identificationEvent = new IdentificationEvent();
        identificationEvent.setId(identificationId);
        identificationEvent.setOccurredAt(occurredAt);
        identificationEvent.setMemberId(memberId);
        identificationEvent.setSearchMethod(IdentificationEvent.SearchMethodEnum.SCAN_BARCODE);
        identificationEvent.setPhotoVerified(true);
        identificationEvent.setClinicNumber(30);
        identificationEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.OPD);
        identificationEvent.setAccepted(true);
        identificationEvent.setMember(member);

        try {
            IdentificationEventDao.create(identificationEvent);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        mainActivity.setNewEncounter(member);
    }

    @After
    public void deleteCreatedModels() {
        // TODO: delete created models?
    }

    @Test
    public void inputNameAndPrice_addNewBillableFragment() {
        // type name in name field
        onView(withId(R.id.name_field)).perform(typeText(BILLABLE_NAME_TO_BE_TYPED));

        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // check that 'Add' button appears
        onView(withId(R.id.add_billable_button)).check(matches(isDisplayed()));
    }

    @Test
    public void addsBillableSetUp_addNewBillableFragment() {
        // type name in name field
        onView(withId(R.id.name_field)).perform(typeText(BILLABLE_NAME_TO_BE_TYPED));

        // type price in price field
        onView(withId(R.id.price_field)).perform(typeText(BILLABLE_PRICE_TO_BE_TYPED));

        // click 'Add' button
        onView(withId(R.id.add_billable_button)).perform(click());

        // check that it opens encounter fragment (checking for encounter fragment title)
        onView(withText(member.getFullName())).check(matches(isDisplayed()));
    }

    @Test
    public void addsBillableName_addNewBillableFragment() {
        addsBillableSetUp_addNewBillableFragment();

        // check that it adds billable to encounter fragment (checking for new billable name)
        onView(withText(BILLABLE_NAME_TO_BE_TYPED)).check(matches(isDisplayed()));
    }
}
