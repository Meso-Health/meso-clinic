package org.watsi.uhp;

import org.watsi.uhp.models.Member;

import java.util.UUID;

public class MemberFactory extends Member {

    public MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum)
            throws ValidationException {
        super();
        setId(id);
        setFullName(fullName);
        setCardId(cardId);
        setAge(age);
        setGender(genderEnum);
    }

    public MemberFactory(UUID id, String fullName, String cardId, int age, GenderEnum genderEnum,
                         UUID householdId) throws ValidationException {
        super();
        setId(id);
        setFullName(fullName);
        setCardId(cardId);
        setAge(age);
        setGender(genderEnum);
        setHouseholdId(householdId);
    }
}
