package org.watsi.uhp;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.Member;

import java.util.UUID;

public class CustomMatchers {
    /**
     * Matches a Diagnosis with a specific description
     */
    public static Matcher<Object> withDiagnosisDescription(final String description){
        return new BoundedMatcher<Object, Diagnosis>(Diagnosis.class){
            @Override
            public boolean matchesSafely(Diagnosis diagnosis) {
                return description.equals(diagnosis.getDescription());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with diagnosis name: " + description);
            }
        };
    }

    /**
     * Matches a Billable with a specific name
     */
    public static Matcher<Object> withBillableName(final String name){
        return new BoundedMatcher<Object, Billable>(Billable.class){
            @Override
            public boolean matchesSafely(Billable billable) {
                return name.equals(billable.getName());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with billable name: " + name);
            }
        };
    }

    /**
     * Matches an EncounterItem with a specific name
     */
    public static Matcher<Object> withEncounterItemName(final String name){
        return new BoundedMatcher<Object, EncounterItem>(EncounterItem.class){
            @Override
            public boolean matchesSafely(EncounterItem encounterItem) {
                return name.equals(encounterItem.getBillable().getName());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with encounter item name: " + name);
            }
        };
    }


    /**
     * Matches an EncounterItem with a specific name, price, and description (optional).
     */
    public static Matcher<Object> withEncounterItem(final String name, final Integer price, final String description){
        return new BoundedMatcher<Object, EncounterItem>(EncounterItem.class){
            @Override
            public boolean matchesSafely(EncounterItem encounterItem) {
                Billable billable = encounterItem.getBillable();
                boolean priceAndNameMatches = billable.getPrice().equals(price) && billable.getName().equals(name);
                if (description != null) {
                    if (billable.requiresLabResult()) {
                        return description.equals(encounterItem.getLabResult().toString()) && priceAndNameMatches;
                    } else {
                        return description.equals(billable.dosageDetails()) && priceAndNameMatches;
                    }
                } else {
                    return priceAndNameMatches;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with encounter item name: " + price);
            }
        };
    }

    /**
     * Matches a Member with a specific id
     */
    public static Matcher<Object> withMemberId(final UUID id){
        return new BoundedMatcher<Object, Member>(Member.class){
            @Override
            public boolean matchesSafely(Member member) {
                return id.equals(member.getId());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("with member id: " + id);
            }
        };
    }

    /**
     * Checks whether a data item is in an adapter for a given view.
     * Taken from: https://google.github.io/android-testing-support-library/docs/espresso/advanced/
     */
    public static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }
                @SuppressWarnings("rawtypes")
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }
        };
    }
}
