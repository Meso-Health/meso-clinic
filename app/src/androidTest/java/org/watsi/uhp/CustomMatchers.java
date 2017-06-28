package org.watsi.uhp;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.Member;

import java.util.UUID;

public class CustomMatchers {

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
