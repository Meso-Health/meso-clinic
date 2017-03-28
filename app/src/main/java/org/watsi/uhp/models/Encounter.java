package org.watsi.uhp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = Encounter.TABLE_NAME)
public class Encounter extends SyncableModel {

    public static final String TABLE_NAME = "encounters";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_OCCURRED_AT = "occurred_at";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_IDENTIFICATION_EVENT_ID = "identification_event_id";
    public static final String FIELD_NAME_ENCOUNTER_ITEMS = "encounter_items";
    public static final String FIELD_NAME_ENCOUNTER_FORMS = "encounter_forms";
    public static final String FIELD_NAME_BACKDATED_OCCURRED_AT = "backdated_occurred_at";

    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#,###,###");

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @Expose
    @SerializedName(FIELD_NAME_OCCURRED_AT)
    @DatabaseField(columnName = FIELD_NAME_OCCURRED_AT, canBeNull = false)
    private Date mOccurredAt;

    @Expose
    @SerializedName(FIELD_NAME_MEMBER_ID)
    private UUID mMemberId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @Expose
    @SerializedName(FIELD_NAME_IDENTIFICATION_EVENT_ID)
    private UUID mIdentificationEventId;

    @DatabaseField(columnName = FIELD_NAME_IDENTIFICATION_EVENT_ID, foreign = true, canBeNull = false)
    private IdentificationEvent mIdentificationEvent;

    @Expose
    @SerializedName(FIELD_NAME_ENCOUNTER_ITEMS)
    private final List<EncounterItem> mEncounterItems = new ArrayList<>();

    private final List<EncounterForm> mEncounterForms = new ArrayList<>();

    @Expose
    @SerializedName(FIELD_NAME_BACKDATED_OCCURRED_AT)
    @DatabaseField(columnName = FIELD_NAME_BACKDATED_OCCURRED_AT, canBeNull = false, defaultValue = "false")
    private boolean mBackdatedOccurredAt;

    public Encounter() {
        super();
    }

    public Encounter(List<EncounterItem> encounterItems) {
        setEncounterItems(encounterItems);
    }

    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        this.mId = id;
    }

    public Date getOccurredAt() {
        return mOccurredAt;
    }

    public void setOccurredAt(Date occurredAt) {
        this.mOccurredAt = occurredAt;
    }

    public UUID getMemberId() {
        return mMemberId;
    }

    public void setMemberId(UUID memberId) {
        this.mMemberId = memberId;
    }

    public Member getMember() {
        return mMember;
    }

    public void setMember(Member member) {
        this.mMember = member;
    }

    public UUID getIdentificationEventId() {
        return mIdentificationEventId;
    }

    public void setIdentificationEventId(UUID identificationEventId) {
        this.mIdentificationEventId = identificationEventId;
    }

    public IdentificationEvent getIdentificationEvent() {
        return mIdentificationEvent;
    }

    public void setIdentificationEvent(IdentificationEvent identificationEvent) {
        this.mIdentificationEvent = identificationEvent;
    }

    public Collection<EncounterItem> getEncounterItems() {
        return mEncounterItems;
    }

    public void setEncounterItems(Collection<EncounterItem> encounterItems) {
        this.mEncounterItems.clear();
        this.mEncounterItems.addAll(encounterItems);
    }

    public Boolean getBackdatedOccurredAt() {
        return mBackdatedOccurredAt;
    }

    public void setBackdatedOccurredAt(Boolean backdatedOccurredAt) {
        this.mBackdatedOccurredAt = backdatedOccurredAt;
    }

    public List<EncounterForm> getEncounterForms() {
        return mEncounterForms;
    }

    public void addEncounterForm(EncounterForm encounterForm) {
        encounterForm.setEncounter(this);
        getEncounterForms().add(encounterForm);
    }

    public int price() {
        int sum = 0;
        for (EncounterItem item : getEncounterItems()) {
            sum += item.getBillable().getPrice() * item.getQuantity();
        }
        return sum;
    }
}
