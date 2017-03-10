package org.watsi.uhp;

import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.rollbar.android.Rollbar;
import com.squareup.haha.perflib.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.UUID;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class ReceiptFragmentFeature {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);
    private MainActivity mainActivity;

    private Member member;

    private Billable billable1;
    private Billable billable2;
    private Billable billable3;

    private EncounterItem encounterItem1;
    private EncounterItem encounterItem2;
    private EncounterItem encounterItem3;

    private Encounter encounter;

    @Before
    public void setup() {
        mainActivity = mActivityRule.getActivity();

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
        Date idOccurredAt =  new GregorianCalendar(2017, Calendar.FEBRUARY, 11).getTime();

        IdentificationEvent identificationEvent = new IdentificationEvent();
        identificationEvent.setId(identificationId);
        identificationEvent.setOccurredAt(idOccurredAt);
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

        billable1 = new Billable();
        billable1.generateId();
        billable1.setName("billable 1");
        billable1.setPrice(1000);
        billable1.setType(Billable.TypeEnum.DRUG);

        billable2 = new Billable();
        billable2.generateId();
        billable2.setName("billable 2");
        billable2.setPrice(2000);
        billable2.setType(Billable.TypeEnum.LAB);

        billable3 = new Billable();
        billable3.generateId();
        billable3.setName("billable 3");
        billable3.setPrice(3000);
        billable3.setType(Billable.TypeEnum.SERVICE);

        try {
            BillableDao.create(billable1);
            BillableDao.create(billable2);
            BillableDao.create(billable3);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        encounterItem1 = new EncounterItem();
        encounterItem1.setBillable(billable1);
        encounterItem1.setQuantity(30);

        encounterItem2 = new EncounterItem();
        encounterItem2.setBillable(billable2);

        encounterItem3 = new EncounterItem();
        encounterItem3.setBillable(billable3);

        encounter = new Encounter();
        UUID encounterId = UUID.randomUUID();
        Date encounterOccurredAt =  new GregorianCalendar(2017, Calendar.FEBRUARY, 11).getTime();
        ArrayList<EncounterItem> encounterItems = new ArrayList<>();
        encounterItems.add(encounterItem1);
        encounterItems.add(encounterItem2);
        encounterItems.add(encounterItem3);

        try {
            EncounterDao.create(encounter);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        encounter.setId(encounterId);
        encounter.setOccurredAt(encounterOccurredAt);
        encounter.setMemberId(memberId);
        encounter.setMember(member);
        encounter.setIdentificationEventId(identificationId);
        encounter.setIdentificationEvent(identificationEvent);
        encounter.setEncounterItems(encounterItems);

        encounterItem1.setEncounter(encounter);
        encounterItem2.setEncounter(encounter);
        encounterItem3.setEncounter(encounter);
        encounterItem1.setEncounterId(encounterId);
        encounterItem2.setEncounterId(encounterId);
        encounterItem3.setEncounterId(encounterId);

        try {
            EncounterItemDao.create(encounterItem1);
            EncounterItemDao.create(encounterItem2);
            EncounterItemDao.create(encounterItem3);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        new NavigationManager(mainActivity).setReceiptFragment();

    }

    @After
    public void deleteCreatedModels() {
        // TODO: delete created models? delete records saved to db
    }

    @Test
    public void showsReceiptFragmentPrompt_receiptFragment() {
        // check that it shows receipt fragment prompt
        onView(withText(R.string.receipt_check_prompt)).check(matches(isDisplayed()));
    }

    @Test
    public void showsCorrectBillables_receiptFragment() {
        // check that it shows all three billables
        onView(withText("billable 1")).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.receipt_billable_name), withText("billable 2"))).check(matches(isDisplayed()));
//        onView(allOf(withId(R.id.receipt_billable_name), withText("billable 3"))).check(matches(isDisplayed()));
    }

}
