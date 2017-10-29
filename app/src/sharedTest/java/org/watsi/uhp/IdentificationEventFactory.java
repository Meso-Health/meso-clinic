package org.watsi.uhp;

import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

public class IdentificationEventFactory {
    public static IdentificationEvent createIdentificationEvent(Member member, int clinicNumber) throws SQLException {
        IdentificationEvent idEvent = new IdentificationEvent();
        idEvent.setMember(member);
        idEvent.setClinicNumber(clinicNumber);
        idEvent.setMemberId(member.getId());
        idEvent.setOccurredAt(Calendar.getInstance().getTime());
        idEvent.setId(UUID.randomUUID());
        idEvent.setAccepted(true);
        idEvent.setPhotoVerified(true);
        idEvent.setSearchMethod(IdentificationEvent.SearchMethodEnum.SCAN_BARCODE);
        idEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.OPD);
        idEvent.setDismissed(false);
        idEvent.create();
        return idEvent;
    }
}
