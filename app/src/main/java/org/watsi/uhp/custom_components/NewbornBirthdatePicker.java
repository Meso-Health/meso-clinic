package org.watsi.uhp.custom_components;

import android.widget.DatePicker;

import org.threeten.bp.LocalDate;
import org.watsi.domain.entities.Member;

import java.util.Calendar;

public class NewbornBirthdatePicker {
    Member mMember;
    DatePicker mDatePicker;

    public NewbornBirthdatePicker(DatePicker datePicker, Member member) {
        mDatePicker = datePicker;
        mMember = member;

        setDatePickerBounds();
        setDatePickerInitializerAndListener();
    }

    private void setDatePickerInitializerAndListener() {
        // TODO: re-implement
//        mDatePicker.init(selectedBirthdate.get(Calendar.YEAR), selectedBirthdate.get(Calendar.MONTH), selectedBirthdate.get(Calendar.DAY_OF_MONTH),
//                new DatePicker.OnDateChangedListener() {
//                    @Override
//                    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
//
//                        Calendar cal = makeCalendarToday();
//                        cal.set(year, month, day);
//                        mMember.setBirthdate(cal.getTime());
//                    }
//                }
//        );
    }

    private void setDatePickerBounds() {
        mDatePicker.setMinDate(makeCalendarThreeMonthsAgo().getTimeInMillis());
        mDatePicker.setMaxDate(makeCalendarToday().getTimeInMillis());
    }

    private Calendar makeCalendarToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal;
    }

    private Calendar makeCalendarThreeMonthsAgo() {
        Calendar threeMonthsAgo = makeCalendarToday();
        threeMonthsAgo.add(Calendar.MONTH, -3);
        return threeMonthsAgo;
    }
}
