package org.watsi.uhp;

import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.Date;
import java.util.UUID;

public class IdentificationEventFactory extends IdentificationEvent {

    // Identification Event with setters
    public IdentificationEventFactory(UUID id, Date occurredAt, UUID memberId, Member member, SearchMethodEnum searchMethod, boolean photoVerified, int clinicNumber, ClinicNumberTypeEnum clinicNumberType, boolean accepted) {
        super();
        setId(id);
        setOccurredAt(occurredAt);
        setMemberId(memberId);
        setMember(member);
        setSearchMethod(searchMethod);
        setPhotoVerified(photoVerified);
        setClinicNumber(clinicNumber);
        setClinicNumberType(clinicNumberType);
        setAccepted(accepted);
    }
}
