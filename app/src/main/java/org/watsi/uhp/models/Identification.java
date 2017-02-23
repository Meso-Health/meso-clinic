package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = Identification.TABLE_NAME)
public class Identification extends AbstractModel{

    public static final String TABLE_NAME = "identifications";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_THROUGH_MEMBER_ID = "through_member_id";
    public static final String FIELD_NAME_SEARCH_METHOD = "search_method";
    public static final String FIELD_NAME_VALIDATED_BY_PHOTO = "validated_by_photo";
    public static final String FIELD_NAME_ACCEPTED = "accepted";

    public enum SearchMethodEnum { SCAN_BARCODE, SEARCH_ID, SEARCH_NAME, THROUGH_HOUSEHOLD }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @DatabaseField(columnName = FIELD_NAME_THROUGH_MEMBER_ID, foreign = true)
    private Member mThroughMember;

    @DatabaseField(columnName = FIELD_NAME_SEARCH_METHOD, canBeNull = false)
    private SearchMethodEnum mSearchMethod;

    @DatabaseField(columnName = FIELD_NAME_VALIDATED_BY_PHOTO, canBeNull = false,
            defaultValue = "true")
    private Boolean mValidatedByPhoto;

    @DatabaseField(columnName = FIELD_NAME_ACCEPTED, canBeNull = false)
    private boolean mAccepted;

    public Identification() {
        super();
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

    public Member getThroughMember() {
        return mThroughMember;
    }

    public void setThroughMember(Member throughMember) {
        this.mThroughMember = throughMember;
    }

    public SearchMethodEnum getSearchMethod() {
        return mSearchMethod;
    }

    public void setSearchMethod(SearchMethodEnum searchMethod) {
        this.mSearchMethod = searchMethod;
    }

    public Boolean getValidatedByPhoto() {
        return mValidatedByPhoto;
    }

    public void setValidatedByPhoto(Boolean validatedByPhoto) {
        this.mValidatedByPhoto = validatedByPhoto;
    }

    public boolean getAccepted() {
        return mAccepted;
    }

    public void setAccepted(boolean accepted) {
        this.mAccepted = accepted;
    }
}
