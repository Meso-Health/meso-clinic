package org.watsi.uhp;

import org.watsi.uhp.models.Member;

import java.util.UUID;

class MemberFactory extends Member {

    MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum)
            throws ValidationException {
        setId(id);
        setFullName(fullName);
        setCardId(cardId);
        setAge(age);
        setGender(genderEnum);
    }

    MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum,
                         UUID householdId) throws ValidationException {
        setId(id);
        setFullName(fullName);
        setCardId(cardId);
        setAge(age);
        setGender(genderEnum);
        setHouseholdId(householdId);
    }
}
