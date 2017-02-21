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
    public static final String FIELD_NAME_CLINIC_NUMBER = "clinic_number";
    public static final String FIELD_NAME_CLINIC_NUMBER_TYPE = "clinic_number_type";

    public enum ClinicNumberTypeEnum {
        OPD,
        IPD,
        DELIVERY
    }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_CLINIC_NUMBER, canBeNull = false)
    private Integer mClinicNumber;

    @DatabaseField(columnName = FIELD_NAME_CLINIC_NUMBER_TYPE, canBeNull = false)
    private ClinicNumberTypeEnum mClinicNumberTypeEnum;

    @ForeignCollectionField
    private final Collection<LineItem> mLineItems = new ArrayList<>();

    public Encounter() {
        super();
    }

    public Encounter(List<LineItem> lineItems) {
        setLineItems(lineItems);
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

    public Collection<LineItem> getLineItems() {
        return mLineItems;
    }

    public void setLineItems(Collection<LineItem> lineItems) {
        this.mLineItems.clear();
        this.mLineItems.addAll(lineItems);
    }

    public Integer getClinicNumber() {
        return mClinicNumber;
    }

    public void setClinicNumber(Integer n) {
        this.mClinicNumber = n;
    }

    public ClinicNumberTypeEnum getClinicNumberType() {
        return mClinicNumberTypeEnum;
    }

    public void setClinicNumberType(ClinicNumberTypeEnum numberType) {
        this.mClinicNumberTypeEnum = numberType;
    }
}
