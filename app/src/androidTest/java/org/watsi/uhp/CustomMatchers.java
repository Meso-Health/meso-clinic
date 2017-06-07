package org.watsi.uhp;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.EncounterItem;

public class CustomMatchers {

    public static Matcher<Billable> withBillableName(final String name){
        return new TypeSafeMatcher<Billable>(){
            @Override
            public boolean matchesSafely(Billable billable) {
                return name.equals(billable.getName());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("error text: ");
            }
        };
    }

    public static Matcher<EncounterItem> withEncounterItemName(final String name){
        return new TypeSafeMatcher<EncounterItem>(){
            @Override
            public boolean matchesSafely(EncounterItem encounterItem) {
                return name.equals(encounterItem.getBillable().getName());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("error text: ");
            }
        };
    }
}
