package org.watsi.uhp.fragments;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.MemberFactory;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;
import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarThreeMonthsAgo;
import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarToday;
import static org.watsi.uhp.helpers.DateTimeHelper.makeCalendarTomorrow;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class EnrollNewbornInfoFragmentTest {
    private EnrollNewbornInfoFragment enrollNewbornInfoFragment;
    private Member mNewborn;

    @Before
    public void setUp() throws Exception {
        enrollNewbornInfoFragment = new EnrollNewbornInfoFragment();
        Bundle bundle = new Bundle();
        Member parent = new MemberFactory("Parent", "RWI123123", 30, Member.GenderEnum.F);
        mNewborn = parent.createNewborn();

        IdentificationEvent idEvent = new IdentificationEvent(mNewborn, IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD, parent);
        bundle.putSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        bundle.putSerializable(NavigationManager.SYNCABLE_MODEL_BUNDLE_FIELD, mNewborn);
        enrollNewbornInfoFragment.setArguments(bundle);
    }


    @Test
    public void datePicker_minDateSet() throws Exception {
        startFragment(enrollNewbornInfoFragment, ClinicActivity.class);
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getMinDate(), makeCalendarThreeMonthsAgo().getTimeInMillis());
    }

    @Test
    public void datePicker_maxDateSet() throws Exception {
        startFragment(enrollNewbornInfoFragment, ClinicActivity.class);
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getMaxDate(), makeCalendarTomorrow().getTimeInMillis());
    }

    @Test
    public void datePicker_defaultToday() throws Exception {
        startFragment(enrollNewbornInfoFragment, ClinicActivity.class);
        Calendar today = makeCalendarToday();
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getDayOfMonth(), today.get(Calendar.DAY_OF_MONTH));
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getMonth(), today.get(Calendar.MONTH));
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getYear(), today.get(Calendar.YEAR));
    }

    @Test
    public void datePicker_newbornBirthdate() throws Exception {
        Calendar fiveDaysAgo = makeCalendarToday();
        fiveDaysAgo.add(Calendar.DAY_OF_MONTH, -5);
        mNewborn.setBirthdate(fiveDaysAgo.getTime());
        startFragment(enrollNewbornInfoFragment, ClinicActivity.class);

        assertEquals(enrollNewbornInfoFragment.getDatePicker().getDayOfMonth(), fiveDaysAgo.get(Calendar.DAY_OF_MONTH));
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getMonth(), fiveDaysAgo.get(Calendar.MONTH));
        assertEquals(enrollNewbornInfoFragment.getDatePicker().getYear(), fiveDaysAgo.get(Calendar.YEAR));
    }

    @Test
    public void datePicker_onDateChanged() throws Exception {
        startFragment(enrollNewbornInfoFragment, ClinicActivity.class);
        Calendar fiveDaysAgo = makeCalendarToday();
        fiveDaysAgo.add(Calendar.DAY_OF_MONTH, -5);

        enrollNewbornInfoFragment.getDatePicker().updateDate(
                fiveDaysAgo.get(Calendar.YEAR),
                fiveDaysAgo.get(Calendar.MONTH),
                fiveDaysAgo.get(Calendar.DAY_OF_MONTH)
        );

        assertEquals(mNewborn.getBirthdate().getTime(), fiveDaysAgo.getTimeInMillis());
    }
}