package org.watsi.uhp;

import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.Photo;

import java.sql.SQLException;
import java.util.UUID;

public class MemberFactory {
    public static Member createMember(String fullName, String cardId, int age, Member.GenderEnum genderEnum)
            throws AbstractModel.ValidationException, SQLException {
        Member member = new Member();
        member.setFullName(fullName);
        member.setCardId(cardId);
        member.setAge(age);
        member.setGender(genderEnum);

        member.setId(UUID.randomUUID());
        member.setFingerprintsGuid(UUID.randomUUID());
        member.setLocalMemberPhoto(new Photo());
        member.getLocalMemberPhoto().setUrl("content://test_member_photo.jpg");
        member.create();
        return member;
    }
}
