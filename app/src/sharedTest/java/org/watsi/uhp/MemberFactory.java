package org.watsi.uhp;

import org.watsi.uhp.models.Member;

import java.util.UUID;

public class MemberFactory extends Member {

    // Member without household that does not use any setters:
    public MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum, boolean absentee) {
        super();
        setId(id);
        mFullName = fullName;
        mCardId = cardId;
        mAge = age;
        mGender = genderEnum;
        mAbsentee = absentee;
    }

    // Member with household that does not use any setters:
    public MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum, boolean absentee, UUID householdId) {
        super();
        setId(id);
        mFullName = fullName;
        mCardId = cardId;
        mAge = age;
        mGender = genderEnum;
        mAbsentee = absentee;
        mHouseholdId = householdId;
    }
}
