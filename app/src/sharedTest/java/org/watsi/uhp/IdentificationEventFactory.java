package org.watsi.uhp;

import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.Calendar;
import java.util.UUID;

public class IdentificationEventFactory extends IdentificationEvent {

    public IdentificationEventFactory(Member member, int clinicNumber) {
        setMember(member);
        setClinicNumber(clinicNumber);

        setMemberId(member.getId());
        setOccurredAt(Calendar.getInstance().getTime());
        setId(UUID.randomUUID());
        setAccepted(true);
        setPhotoVerified(true);
        setSearchMethod(SearchMethodEnum.SCAN_BARCODE);
        setClinicNumberType(ClinicNumberTypeEnum.OPD);
        setDismissed(false);
    }
}
