package org.watsi.uhp;

import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class IdentificationEventFactory extends IdentificationEvent {

    public static IdentificationEvent createIdentificationEvent(Member member, int clinicNumber) {
        IdentificationEvent idEvent = new IdentificationEvent();

        // set by you (with parameters passed in):
        idEvent.setMember(member);
        idEvent.setClinicNumber(clinicNumber);

        // set automatically with method:
        idEvent.setMemberId(member.getId());
        idEvent.setOccurredAt(Calendar.getInstance().getTime());
        idEvent.setId(UUID.randomUUID());
        idEvent.setAccepted(true);
        idEvent.setPhotoVerified(true);
        idEvent.setSearchMethod(SearchMethodEnum.SCAN_BARCODE);
        idEvent.setClinicNumberType(ClinicNumberTypeEnum.OPD);
        idEvent.setDismissed(false);

        return idEvent;
    }
}
