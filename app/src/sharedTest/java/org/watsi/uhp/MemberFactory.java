package org.watsi.uhp;

import org.watsi.uhp.models.Member;

import java.util.UUID;

public class MemberFactory extends Member {

    public MemberFactory(String fullName, String cardId, int age, GenderEnum genderEnum)
            throws ValidationException {
        setFullName(fullName);
        setCardId(cardId);
        setAge(age);
        setGender(genderEnum);

        setId(UUID.randomUUID());
        setFingerprintsGuid(UUID.randomUUID());
        setPhotoUrl("content://test_member_photo.jpg");
    }
}
