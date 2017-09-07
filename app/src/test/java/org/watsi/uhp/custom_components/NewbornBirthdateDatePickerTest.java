package org.watsi.uhp.custom_components;

import android.content.Context;
import android.widget.DatePicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.MemberFactory;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.models.Member;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class NewbornBirthdateDatePickerTest {
    private Member mMember;
    private DatePicker mDatePicker;

    @Before
    public void setUp() throws Exception {
        mMember = new MemberFactory("Salmonella Malaria NewBorn", "RWI123123", 30, Member.GenderEnum.F);
        Context context = Robolectric.buildActivity(ClinicActivity.class).create().get();
        mDatePicker = new DatePicker(context);
    }

    @Test
    public void datePicker_maxDateSet() throws Exception {
        NewbornBirthdateDatePicker newbornBirthdateDatePicker = new NewbornBirthdateDatePicker(mDatePicker, mMember);
        Calendar maxDate = Calendar.getInstance();
        maxDate.setTimeInMillis(mDatePicker.getMaxDate());

        Calendar today = Calendar.getInstance();

        assertEquals(maxDate.get(Calendar.YEAR), today.get(Calendar.YEAR));
        assertEquals(maxDate.get(Calendar.MONTH), today.get(Calendar.MONTH));
        assertEquals(maxDate.get(Calendar.DAY_OF_MONTH), today.get(Calendar.DAY_OF_MONTH));
        assertDateHasNoTime(mDatePicker.getMaxDate());
    }

    @Test
    public void datePicker_minDateSet() throws Exception {
        NewbornBirthdateDatePicker newbornBirthdateDatePicker = new NewbornBirthdateDatePicker(mDatePicker, mMember);
        Calendar threeMonthsAgo = Calendar.getInstance();
        threeMonthsAgo.add(Calendar.MONTH, -3);

        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(mDatePicker.getMinDate());

        assertEquals(minDate.get(Calendar.YEAR), threeMonthsAgo.get(Calendar.YEAR));
        assertEquals(minDate.get(Calendar.MONTH), threeMonthsAgo.get(Calendar.MONTH));
        assertEquals(minDate.get(Calendar.DAY_OF_MONTH), threeMonthsAgo.get(Calendar.DAY_OF_MONTH));
        assertDateHasNoTime(mDatePicker.getMinDate());
    }

    @Test
    public void datePicker_memberNoBirthdateDefaultsToToday() throws Exception {
        mMember.setBirthdate(null);
        NewbornBirthdateDatePicker newbornBirthdateDatePicker = new NewbornBirthdateDatePicker(mDatePicker, mMember);
        Calendar today = Calendar.getInstance();
        assertEquals(mDatePicker.getYear(), today.get(Calendar.YEAR));
        assertEquals(mDatePicker.getMonth(), today.get(Calendar.MONTH));
        assertEquals(mDatePicker.getDayOfMonth(), today.get(Calendar.DAY_OF_MONTH));

        assertDateHasNoTime(mMember.getBirthdate().getTime());
    }

    @Test
    public void datePicker_memberHasBirthdate() throws Exception {
        NewbornBirthdateDatePicker newbornBirthdateDatePicker = new NewbornBirthdateDatePicker(mDatePicker, mMember);
        Calendar birthdate = Calendar.getInstance();
        birthdate.setTime(mMember.getBirthdate());
        assertEquals(mDatePicker.getYear(), birthdate.get(Calendar.YEAR));
        assertEquals(mDatePicker.getMonth(), birthdate.get(Calendar.MONTH));
        assertEquals(mDatePicker.getDayOfMonth(), birthdate.get(Calendar.DAY_OF_MONTH));

        assertDateHasNoTime(mMember.getBirthdate().getTime());
    }


    @Test
    public void datePicker_onDateChanged() throws Exception {
        NewbornBirthdateDatePicker newbornBirthdateDatePicker = new NewbornBirthdateDatePicker(mDatePicker, mMember);
        Calendar fiveDaysAgo = Calendar.getInstance();
        fiveDaysAgo.add(Calendar.DAY_OF_MONTH, -5);

        mDatePicker.updateDate(
                fiveDaysAgo.get(Calendar.YEAR),
                fiveDaysAgo.get(Calendar.MONTH),
                fiveDaysAgo.get(Calendar.DAY_OF_MONTH)
        );

        assertEquals(mDatePicker.getYear(), fiveDaysAgo.get(Calendar.YEAR));
        assertEquals(mDatePicker.getMonth(), fiveDaysAgo.get(Calendar.MONTH));
        assertEquals(mDatePicker.getDayOfMonth(), fiveDaysAgo.get(Calendar.DAY_OF_MONTH));

        assertDateHasNoTime(mMember.getBirthdate().getTime());
    }

    private void assertDateHasNoTime(long timeInMilliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliseconds);
        assertEquals(calendar.get(Calendar.HOUR), 0);
        assertEquals(calendar.get(Calendar.MINUTE), 0);
        assertEquals(calendar.get(Calendar.SECOND), 0);
        assertEquals(calendar.get(Calendar.MILLISECOND), 0);
    }
}