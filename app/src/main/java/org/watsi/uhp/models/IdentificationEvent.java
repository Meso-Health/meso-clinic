package org.watsi.uhp.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

@DatabaseTable(tableName = IdentificationEvent.TABLE_NAME)
public class IdentificationEvent extends SyncableModel {

    public static final String TABLE_NAME = "identifications";

    public static final String FIELD_NAME_OCCURRED_AT = "occurred_at";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";
    public static final String FIELD_NAME_THROUGH_MEMBER_ID = "through_member_id";
    public static final String FIELD_NAME_SEARCH_METHOD = "search_method";
    public static final String FIELD_NAME_PHOTO_VERIFIED = "photo_verified";
    public static final String FIELD_NAME_ACCEPTED = "accepted";
    public static final String FIELD_NAME_CLINIC_NUMBER = "clinic_number";
    public static final String FIELD_NAME_CLINIC_NUMBER_TYPE = "clinic_number_type";
    public static final String FIELD_NAME_DISMISSED = "dismissed";
    public static final String FIELD_NAME_DISMISSAL_REASON = "dismissal_reason";
    public static final String FIELD_NAME_SIMPRINTS_RESULTS_CODE = "simprints_results_code";
    public static final String FIELD_NAME_FINGERPRINTS_VERIFICATION_CONFIDENCE = "fingerprints_verification_confidence";
    public static final String FIELD_NAME_FINGERPRINTS_VERIFICATION_TIER = "fingerprints_verification_tier";

    public enum ClinicNumberTypeEnum {
        @SerializedName("opd") OPD,
        @SerializedName("delivery") DELIVERY
    }

    public enum SearchMethodEnum {
        @SerializedName("scan_barcode") SCAN_BARCODE,
        @SerializedName("search_id") SEARCH_ID,
        @SerializedName("search_name") SEARCH_NAME,
        @SerializedName("through_household") THROUGH_HOUSEHOLD
    }

    public enum DismissalReasonEnum {
        @SerializedName("accidental_identification") ACCIDENTAL_IDENTIFICATION,
        @SerializedName("patient_left_before_care") PATIENT_LEFT_BEFORE_CARE,
        @SerializedName("patient_left_after_care") PATIENT_LEFT_AFTER_CARE,
    }

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
    @SerializedName(FIELD_NAME_THROUGH_MEMBER_ID)
    private UUID mThroughMemberId;

    @DatabaseField(columnName = FIELD_NAME_THROUGH_MEMBER_ID, foreign = true)
    private Member mThroughMember;

    @Expose
    @SerializedName(FIELD_NAME_SEARCH_METHOD)
    @DatabaseField(columnName = FIELD_NAME_SEARCH_METHOD, canBeNull = false)
    private SearchMethodEnum mSearchMethod;

    @Expose
    @SerializedName(FIELD_NAME_PHOTO_VERIFIED)
    @DatabaseField(columnName = FIELD_NAME_PHOTO_VERIFIED, canBeNull = false, defaultValue = "true")
    private Boolean mPhotoVerified;

    @Expose
    @SerializedName(FIELD_NAME_ACCEPTED)
    @DatabaseField(columnName = FIELD_NAME_ACCEPTED, canBeNull = false, defaultValue = "true")
    private boolean mAccepted;

    @Expose
    @SerializedName(FIELD_NAME_CLINIC_NUMBER)
    @DatabaseField(columnName = FIELD_NAME_CLINIC_NUMBER)
    private Integer mClinicNumber;

    @Expose
    @SerializedName(FIELD_NAME_CLINIC_NUMBER_TYPE)
    @DatabaseField(columnName = FIELD_NAME_CLINIC_NUMBER_TYPE)
    private ClinicNumberTypeEnum mClinicNumberTypeEnum;

    @Expose
    @SerializedName(FIELD_NAME_DISMISSED)
    @DatabaseField(columnName = FIELD_NAME_DISMISSED, canBeNull = false, defaultValue = "false")
    private boolean mDismissed;

    @Expose
    @SerializedName(FIELD_NAME_DISMISSAL_REASON)
    @DatabaseField(columnName = FIELD_NAME_DISMISSAL_REASON)
    private DismissalReasonEnum mDismissalReason;

    @Expose
    @SerializedName(FIELD_NAME_SIMPRINTS_RESULTS_CODE)
    @DatabaseField(columnName = FIELD_NAME_SIMPRINTS_RESULTS_CODE)
    private int mSimprintsResultsCode;

    @Expose
    @SerializedName(FIELD_NAME_FINGERPRINTS_VERIFICATION_CONFIDENCE)
    @DatabaseField(columnName = FIELD_NAME_FINGERPRINTS_VERIFICATION_CONFIDENCE)
    private float mFingerprintsVerificationConfidence;

    @Expose
    @SerializedName(FIELD_NAME_FINGERPRINTS_VERIFICATION_TIER)
    @DatabaseField(columnName = FIELD_NAME_FINGERPRINTS_VERIFICATION_TIER)
    private String mFingerprintsVerificationAttemptDetail;

    public IdentificationEvent() {
        super();
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

    public UUID getThroughMemberId() {
        return mThroughMemberId;
    }

    public void setThroughMemberId(UUID throughMemberId) {
        this.mThroughMemberId = throughMemberId;
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

    public Boolean getPhotoVerified() {
        return mPhotoVerified;
    }

    public void setPhotoVerified(Boolean photoVerified) {
        this.mPhotoVerified = photoVerified;
    }

    public boolean getAccepted() {
        return mAccepted;
    }

    public void setAccepted(boolean accepted) {
        this.mAccepted = accepted;
    }

    public Integer getClinicNumber() {
        return mClinicNumber;
    }

    public void setClinicNumber(Integer n) {
        this.mClinicNumber = n;
    }

    public String getFormattedClinicNumber() {
        if (mClinicNumberTypeEnum == null) {
            return null;
        } else if (mClinicNumberTypeEnum == ClinicNumberTypeEnum.DELIVERY) {
            return "D" + mClinicNumber.toString();
        } else {
            return mClinicNumber.toString();
        }
    }

    public ClinicNumberTypeEnum getClinicNumberType() {
        return mClinicNumberTypeEnum;
    }

    public void setClinicNumberType(ClinicNumberTypeEnum numberType) {
        this.mClinicNumberTypeEnum = numberType;
    }

    public boolean getDismissed() {
        return mDismissed;
    }

    public void setDismissed(boolean dismissed) {
        this.mDismissed = dismissed;
    }

    public DismissalReasonEnum getDismissalReason() {
        return mDismissalReason;
    }

    public static String[] getFormattedDismissalReasons() {
        String[] names = new String[DismissalReasonEnum.values().length];

        for (int i = 0; i < names.length; i++) {
            String dismissalReason = DismissalReasonEnum.values()[i].name().replace("_", " ").toLowerCase();
            names[i] = dismissalReason.substring(0,1).toUpperCase() + dismissalReason.substring(1);
        }

        return names;
    }

    public void setDismissalReason(DismissalReasonEnum dismissalReason) {
        setDismissed(true);
        this.mDismissalReason = dismissalReason;
    }

    public void setFingerprintsVerificationConfidence(float confidence) {
        this.mFingerprintsVerificationConfidence = confidence;
    }

    public float getFingerprintsVerificationConfidence() {
        return this.mFingerprintsVerificationConfidence;
    }

    public void setFingerprintsVerificationTier(String detail) {
        this.mFingerprintsVerificationAttemptDetail = detail;
    }

    public String getFingerprintsVerificationTier() {
        return mFingerprintsVerificationAttemptDetail;
    }

    public void setSimprintsResultsCode(int simprintsResultsCode) {
        this.mSimprintsResultsCode = simprintsResultsCode;
    }

    public int getSimprintsResultsCode() {
        return mSimprintsResultsCode;
    }

    public Map<String, RequestBody> formatPatchRequest() {
        Map<String, RequestBody> requestBodyMap = new HashMap<>();
        requestBodyMap.put(IdentificationEvent.FIELD_NAME_DISMISSED,
                RequestBody.create(
                        MultipartBody.FORM, String.valueOf(getDismissed())));
        requestBodyMap.put(IdentificationEvent.FIELD_NAME_DISMISSAL_REASON,
                RequestBody.create(
                        MultipartBody.FORM,
                        new Gson().toJsonTree(getDismissalReason()).getAsString()));
        return requestBodyMap;
    }

    @Override
    public void handleUpdateFromSync(SyncableModel responseModel) {
        // no-op
    }

    @Override
    protected Call postApiCall(Context context) throws SQLException {
        if (getThroughMember() != null) {
            setThroughMemberId(getThroughMember().getId());
        }
        Log.i("UHP", "IdentificationEvent post: " + getMemberId());
        return ApiService.requestBuilder(context).postIdentificationEvent(
                getTokenAuthHeaderString(), BuildConfig.PROVIDER_ID, this);
    }

    @Override
    protected Call patchApiCall(Context context) throws SQLException {
        return ApiService.requestBuilder(context).patchIdentificationEvent(
                getTokenAuthHeaderString(), getId(), formatPatchRequest());
    }

    @Override
    protected void persistAssociations() {
        // no-op
    }
}
