package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = Encounter.TABLE_NAME)
public class Encounter extends AbstractModel {

    public static final String TABLE_NAME = "encounters";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_IDENTIFICATION_ID = "identification_id";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_IDENTIFICATION_ID, foreign = true, canBeNull = false)
    private IdentificationEvent mIdentificationEvent;

    @ForeignCollectionField
    private final Collection<EncounterItem> mEncounterItems = new ArrayList<>();

    public Encounter() {
        super();
    }

    public Encounter(List<EncounterItem> encounterItems) {
        setLineItems(encounterItems);
    }

    public UUID getId() {
        return mId;
    }

    public Member getMember() {
        return mMember;
    }

    public void setMember(Member member) {
        this.mMember = member;
    }

    public IdentificationEvent getIdentification() {
        return mIdentificationEvent;
    }

    public void setIdentification(IdentificationEvent identificationEvent) {
        this.mIdentificationEvent = identificationEvent;
    }

    public Collection<EncounterItem> getLineItems() {
        return mEncounterItems;
    }

    public void setLineItems(Collection<EncounterItem> encounterItems) {
        this.mEncounterItems.clear();
        this.mEncounterItems.addAll(encounterItems);
    }
}
