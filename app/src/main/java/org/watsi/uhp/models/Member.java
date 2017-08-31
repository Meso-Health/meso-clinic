package org.watsi.uhp.models;

import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;

import com.google.common.io.ByteStreams;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;

@DatabaseTable(tableName = Member.TABLE_NAME)
public class Member extends SyncableModel {

    public static final String TABLE_NAME = "members";

    public static final String FIELD_NAME_CARD_ID = "card_id";
    public static final String FIELD_NAME_FULL_NAME = "full_name";
    public static final String FIELD_NAME_AGE = "age";
    public static final String FIELD_NAME_GENDER = "gender";
    public static final String FIELD_NAME_PHOTO = "photo";
    public static final String FIELD_NAME_PHOTO_URL = "photo_url";
    public static final String FIELD_NAME_NATIONAL_ID_PHOTO = "national_id_photo";
    public static final String FIELD_NAME_NATIONAL_ID_PHOTO_URL = "national_id_photo_url";
    public static final String FIELD_NAME_HOUSEHOLD_ID = "household_id";
    public static final String FIELD_NAME_FINGERPRINTS_GUID = "fingerprints_guid";
    public static final String FIELD_NAME_PHONE_NUMBER = "phone_number";
    public static final String FIELD_NAME_BIRTHDATE = "birthdate";
    public static final String FIELD_NAME_BIRTHDATE_ACCURACY = "birthdate_accuracy";
    public static final String FIELD_NAME_ENROLLED_AT = "enrolled_at";

    public static final int MINIMUM_FINGERPRINT_AGE = 6;
    public static final int MINIMUM_NATIONAL_ID_AGE = 18;

    public enum GenderEnum { M, F }

    public enum BirthdateAccuracyEnum { D, M, Y }

    @Expose
    @SerializedName(FIELD_NAME_CARD_ID)
    @DatabaseField(columnName = FIELD_NAME_CARD_ID)
    protected String mCardId;

    @Expose
    @SerializedName(FIELD_NAME_FULL_NAME)
    @DatabaseField(columnName = FIELD_NAME_FULL_NAME, canBeNull = false)
    protected String mFullName;

    @Expose
    @SerializedName(FIELD_NAME_AGE)
    @DatabaseField(columnName = FIELD_NAME_AGE)
    protected int mAge;

    @Expose
    @SerializedName(FIELD_NAME_GENDER)
    @DatabaseField(columnName = FIELD_NAME_GENDER)
    protected GenderEnum mGender;

    @DatabaseField(columnName = FIELD_NAME_PHOTO, dataType = DataType.BYTE_ARRAY)
    protected byte[] mPhoto;

    @Expose
    @SerializedName(FIELD_NAME_PHOTO_URL)
    @DatabaseField(columnName = FIELD_NAME_PHOTO_URL)
    protected String mPhotoUrl;

    @DatabaseField(columnName = FIELD_NAME_NATIONAL_ID_PHOTO, dataType = DataType.BYTE_ARRAY)
    protected byte[] mNationalIdPhoto;

    @Expose
    @SerializedName(FIELD_NAME_NATIONAL_ID_PHOTO_URL)
    @DatabaseField(columnName = FIELD_NAME_NATIONAL_ID_PHOTO_URL)
    protected String mNationalIdPhotoUrl;

    @Expose
    @SerializedName(FIELD_NAME_HOUSEHOLD_ID)
    @DatabaseField(columnName = FIELD_NAME_HOUSEHOLD_ID)
    protected UUID mHouseholdId;

    @Expose
    @SerializedName(FIELD_NAME_FINGERPRINTS_GUID)
    @DatabaseField(columnName = FIELD_NAME_FINGERPRINTS_GUID)
    protected UUID mFingerprintsGuid;

    @Expose
    @SerializedName(FIELD_NAME_BIRTHDATE)
    @DatabaseField(columnName = FIELD_NAME_BIRTHDATE)
    private Date mBirthdate;

    @Expose
    @SerializedName(FIELD_NAME_BIRTHDATE_ACCURACY)
    @DatabaseField(columnName = FIELD_NAME_BIRTHDATE_ACCURACY)
    private BirthdateAccuracyEnum mBirthdateAccuracy;

    @Expose
    @SerializedName(FIELD_NAME_PHONE_NUMBER)
    @DatabaseField(columnName = FIELD_NAME_PHONE_NUMBER)
    protected String mPhoneNumber;

    @Expose
    @SerializedName(FIELD_NAME_ENROLLED_AT)
    @DatabaseField(columnName = FIELD_NAME_ENROLLED_AT)
    private Date mEnrolledAt;

    @ForeignCollectionField(orderColumnName = IdentificationEvent.FIELD_NAME_CREATED_AT)
    private final Collection<IdentificationEvent> mIdentificationEvents = new ArrayList<>();

    public Member() {
        super();
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    public String getFullName() {
        return this.mFullName;
    }

    @Override
    public void validate() throws ValidationException {
        validateFullName();
        validateCardId();
        validatePhoneNumber();
        validateBirthdate();
        validateGender();
    }

    public boolean validFullName() {
        return mFullName != null && !mFullName.isEmpty();
    }

    public boolean validCardId() {
        return Member.validCardId(mCardId);
    }

    public boolean validPhoneNumber() {
        return mPhoneNumber == null || mPhoneNumber.matches("0?[1-9]\\d{8}");
    }

    public boolean validGender() {
        return mGender != null;
    }

    public boolean validBirthdate() {
        return mBirthdate != null && mBirthdateAccuracy != null;
    }

    public void validateFullName() throws ValidationException {
        if (!validFullName()) {
            throw new ValidationException(FIELD_NAME_FULL_NAME, "Name cannot be blank");
        }
    }

    public void validateCardId() throws ValidationException {
        if (!validCardId()) {
            throw new ValidationException(FIELD_NAME_CARD_ID, "Card must be 3 letters followed by 6 numbers");
        }
    }

    public void validatePhoneNumber() throws ValidationException {
        if (!validPhoneNumber()) {
            throw new ValidationException(FIELD_NAME_PHONE_NUMBER, "Phone number is invalid.");
        }
    }

    public void validateGender() throws ValidationException {
        if (!validGender()) {
            throw new ValidationException(FIELD_NAME_GENDER, "Gender cannot be blank.");
        }
    }

    public void validateBirthdate() throws ValidationException {
        if (!validBirthdate()) {
            throw new ValidationException(FIELD_NAME_BIRTHDATE, "Birthdate or birthdate accuracy is invalid.");
        }
    }

    @Override
    public void handleUpdateFromSync(SyncableModel response) {
        Member memberResponse = (Member) response;
        String photoUrlFromResponse = memberResponse.getPhotoUrl();
        String nationalIdPhotoUrlFromResponse = memberResponse.getNationalIdPhotoUrl();

        try {
            if (photoUrlFromResponse != null) {
                try {
                    if (FileManager.isLocal(getPhotoUrl())) FileManager.deleteLocalPhoto(getPhotoUrl());
                } catch (FileManager.FileDeletionException e) {
                    ExceptionManager.reportException(e);
                }
                setPhotoUrl(photoUrlFromResponse);
                fetchAndSetPhotoFromUrl(new OkHttpClient());
                // set the photo field on the response so the field does not get marked as
                //  dirty when the models are diffed in the sync logic
                memberResponse.setPhoto(getPhoto());
            }

            if (nationalIdPhotoUrlFromResponse != null && FileManager.isLocal((getNationalIdPhotoUrl()))) {
                try {
                    FileManager.deleteLocalPhoto(getNationalIdPhotoUrl());
                } catch (FileManager.FileDeletionException e) {
                    ExceptionManager.reportException(e);
                }
                setNationalIdPhotoUrl(nationalIdPhotoUrlFromResponse);
            }
        } catch (IOException | ValidationException e) {
            ExceptionManager.reportException(e);
        }
    }

    public void updateFromFetch() throws SQLException {
        Member persistedMember = MemberDao.findById(getId());
        if (persistedMember != null) {
            // if the persisted member has not been synced to the back-end, assume it is
            // the most up-to-date and do not update it with the fetched member attributes
            if (!persistedMember.isSynced()) {
                return;
            }

            // if the existing member record has a photo and the fetched member record has
            // the same photo url as the existing record, copy the photo to the new record
            // so we do not have to re-download it
            if (persistedMember.getPhoto() != null && persistedMember.getPhotoUrl() != null &&
                    persistedMember.getPhotoUrl().equals(getPhotoUrl())) {
                setPhoto(persistedMember.getPhoto());
            }
        }
        getDao().createOrUpdate(this);
    }

    @Override
    protected Call postApiCall(Context context) throws SQLException {
        return ApiService.requestBuilder(context).enrollMember(
                getTokenAuthHeaderString(), formatPostRequest(context));
    }

    @Override
    protected void persistAssociations() {
        // no-op
    }

    @Override
    protected Call patchApiCall(Context context) throws SQLException, SyncException {
        return ApiService.requestBuilder(context).syncMember(
                getTokenAuthHeaderString(), getId(), formatPatchRequest(context));
    }

    public String getCardId() {
        return mCardId;
    }

    public String getFormattedCardId() {
        if (getCardId() == null) {
            return null;
        } else {
            return getCardId().substring(0,3) + " " + getCardId().substring(3,6) + " " + getCardId().substring(6);
        }
    }

    public void setCardId(String cardId) {
        cardId = cardId.replaceAll(" ","");
        this.mCardId = cardId;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        this.mAge = age;
    }

    public String getFormattedAge() {
        if (getAge() == 1) {
            return "1 year";
        } else {
            return getAge() + " years";
        }
    }

    public GenderEnum getGender() {
        return mGender;
    }

    public void setGender(GenderEnum gender) {
        this.mGender = gender;
    }

    public String getFormattedGender() {
        if (getGender() == GenderEnum.M) {
            return "M";
        } else {
            return "F";
        }
    }

    public String getFormattedAgeAndGender() {
        return getFormattedAge() + " / " + getFormattedGender();
    }

    public byte[] getPhoto() {
        return mPhoto;
    }

    public void setPhoto(byte[] photoBytes) {
        this.mPhoto = photoBytes;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    // TODO Leaving this to validate on set because the photos PR will take care of this.
    public void setPhotoUrl(String photoUrl) throws ValidationException {
        if (photoUrl == null) {
            this.mPhotoUrl = null;
        } else {
            if (URLUtil.isValidUrl(photoUrl)) {
                this.mPhotoUrl = photoUrl;
            } else {
                throw new ValidationException(FIELD_NAME_PHOTO_URL, "Invalid photo url");
            }
        }
    }

    public String getNationalIdPhotoUrl() {
        return mNationalIdPhotoUrl;
    }

    public void setNationalIdPhotoUrl(String nationalIdPhotoUrl) {
        this.mNationalIdPhotoUrl = nationalIdPhotoUrl;
    }

    public void setHouseholdId(UUID householdId) {
        this.mHouseholdId = householdId;
    }

    public UUID getHouseholdId() {
        return mHouseholdId;
    }

    public Collection<IdentificationEvent> getIdentificationEvents() {
        return mIdentificationEvents;
    }

    public UUID getFingerprintsGuid() {
        return mFingerprintsGuid;
    }

    public void setFingerprintsGuid(UUID fingerprintsGuid) {
        this.mFingerprintsGuid = fingerprintsGuid;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            this.mPhoneNumber = null;
        } else {
            this.mPhoneNumber = phoneNumber;
        }
    }

    public BirthdateAccuracyEnum getBirthdateAccuracy() {
        return mBirthdateAccuracy;
    }

    public void setBirthdateAccuracy(BirthdateAccuracyEnum birthdateAccuracy) {
        this.mBirthdateAccuracy = birthdateAccuracy;
    }

    public Date getBirthdate() {
        return mBirthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.mBirthdate = birthdate;
    }

    public Date getEnrolledAt() {
        return mEnrolledAt;
    }

    public void setEnrolledAt(Date enrolledAt) {
        this.mEnrolledAt = enrolledAt;
    }

    public boolean isAbsentee() {
        return getPhotoUrl() == null || (getAge() >= 6 && getFingerprintsGuid() == null);
    }

    public void fetchAndSetPhotoFromUrl(OkHttpClient okHttpClient) throws IOException {
        if (FileManager.isLocal(getPhotoUrl())) return;
        Request request = new Request.Builder().url(getPhotoUrl()).build();
        okhttp3.Response response = okHttpClient.newCall(request).execute();

        try {
            if (response.isSuccessful()) {
                InputStream is = response.body().byteStream();
                setPhoto(ByteStreams.toByteArray(is));
            } else {
                Map<String,String> params = new HashMap<>();
                params.put("member.id", getId().toString());
                ExceptionManager.requestFailure(
                        "Failed to fetch member photo",
                        request,
                        response,
                        params
                );
            }
        } finally {
            response.close();
        }
    }

    public boolean shouldCaptureFingerprint() {
        return getAge() >= Member.MINIMUM_FINGERPRINT_AGE;
    }

    public boolean shouldCaptureNationalIdPhoto() {
        return getAge() >= Member.MINIMUM_NATIONAL_ID_AGE;
    }

    public Map<String, RequestBody> formatPatchRequest(Context context) throws SyncException {
        Map<String, RequestBody> requestPartMap = new HashMap<>();

        if (dirty(FIELD_NAME_PHOTO_URL)) {
            byte[] image = FileManager.readFromUri(Uri.parse(getPhotoUrl()), context);
            if (image != null) {
                requestPartMap.put(
                        FIELD_NAME_PHOTO,
                        RequestBody.create(MediaType.parse("image/jpg"), image)
                );
            }
        }

        // only include national ID field in request if member photo is not
        //  being sent in order to limit the size of the request
        if (requestPartMap.get(FIELD_NAME_PHOTO) == null) {
            if (dirty(FIELD_NAME_NATIONAL_ID_PHOTO_URL)) {
                byte[] image =  FileManager.readFromUri(Uri.parse(getNationalIdPhotoUrl()), context);
                if (image != null) {
                    requestPartMap.put(
                            FIELD_NAME_NATIONAL_ID_PHOTO,
                            RequestBody.create(MediaType.parse("image/jpg"), image)
                    );
                }
            }
        }

        if (dirty(FIELD_NAME_FINGERPRINTS_GUID)) {
            if (getFingerprintsGuid() != null) {
                requestPartMap.put(
                        FIELD_NAME_FINGERPRINTS_GUID,
                        RequestBody.create(MultipartBody.FORM, getFingerprintsGuid().toString())
                );
            }
        }

        if (dirty(FIELD_NAME_PHONE_NUMBER)) {
            if (getPhoneNumber() != null) {
                requestPartMap.put(
                        FIELD_NAME_PHONE_NUMBER,
                        RequestBody.create(MultipartBody.FORM, getPhoneNumber())
                );
            }
        }

        if (dirty(FIELD_NAME_FULL_NAME)) {
            if (getFullName() != null) {
                requestPartMap.put(
                        FIELD_NAME_FULL_NAME,
                        RequestBody.create(MultipartBody.FORM, getFullName())
                );
            }
        }

        if (dirty(FIELD_NAME_CARD_ID)) {
            if (getCardId() != null) {
                requestPartMap.put(
                        FIELD_NAME_CARD_ID,
                        RequestBody.create(MultipartBody.FORM, getCardId())
                );
            }
        }

        if (requestPartMap.isEmpty()) {
            throw new SyncException("Empty request body map for member " + getId().toString() +
                    ". Dirty fields are: " + getDirtyFields().toString());
        }
        return requestPartMap;
    }

    public Map<String, RequestBody> formatPostRequest(Context context) {
        Map<String, RequestBody> requestBodyMap = new HashMap<>();

        requestBodyMap.put(FIELD_NAME_ID, RequestBody.create(MultipartBody.FORM, getId().toString()));

        requestBodyMap.put(
                "provider_assignment[provider_id]",
                RequestBody.create(MultipartBody.FORM, String.valueOf(BuildConfig.PROVIDER_ID))
        );

        requestBodyMap.put(
                "provider_assignment[start_reason]",
                RequestBody.create(MultipartBody.FORM, "birth")
        );

        if (getEnrolledAt() != null) {
            requestBodyMap.put(
                    FIELD_NAME_ENROLLED_AT,
                    RequestBody.create(MultipartBody.FORM, Clock.asIso(getEnrolledAt()))
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member without an enrolled date.");
        }

        if (getBirthdate() != null){
            requestBodyMap.put(
                    Member.FIELD_NAME_BIRTHDATE,
                    RequestBody.create(MultipartBody.FORM, Clock.asIso(getBirthdate()))
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member with a null birthdate.");
        }

        if (getBirthdateAccuracy() != null) {
            requestBodyMap.put(
                    Member.FIELD_NAME_BIRTHDATE_ACCURACY,
                    RequestBody.create(MultipartBody.FORM, getBirthdateAccuracy().toString())
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member with a null birthdateAccuracy.");
        }

        if (getHouseholdId() != null) {
            requestBodyMap.put(
                    Member.FIELD_NAME_HOUSEHOLD_ID,
                    RequestBody.create(MultipartBody.FORM, getHouseholdId().toString())
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member with a null household ID.");
        }

        if (getPhotoUrl() != null && FileManager.isLocal(getPhotoUrl())) {
            byte[] image = FileManager.readFromUri(Uri.parse(getPhotoUrl()), context);
            if (image != null) {
                requestBodyMap.put(FIELD_NAME_PHOTO, RequestBody.create(MediaType.parse("image/jpg"), image));
            }
        }

        if (getGender() != null) {
            requestBodyMap.put(
                    FIELD_NAME_GENDER,
                    RequestBody.create(MultipartBody.FORM, getGender().toString())
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member with a null gender.");
        }

        if (getFullName() != null) {
            requestBodyMap.put(
                    FIELD_NAME_FULL_NAME,
                    RequestBody.create(MultipartBody.FORM, getFullName())
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member without a full name.");
        }

        if (getCardId() != null) {
            requestBodyMap.put(
                    FIELD_NAME_CARD_ID,
                    RequestBody.create(MultipartBody.FORM, getCardId())
            );
        } else {
            ExceptionManager.reportErrorMessage("Member.sync called on member without a valid card ID.");
        }

        clearDirtyFields();
        return requestBodyMap;
    }

    public static boolean validCardId(String cardId) {
        if (cardId == null || cardId.isEmpty()) {
            return false;
        } else {
            return cardId.matches("[A-Z]{3}[0-9]{6}");
        }
    }

    public static boolean validPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        } else {
            return phoneNumber.matches("0?[1-9]\\d{8}");
        }
    }

    public String getFormattedPhoneNumber() {
        if (getPhoneNumber() == null) {
            return null;
        } else if (getPhoneNumber().length() == 10) {
            return "(0) " + getPhoneNumber().substring(1,4) + " " +
                    getPhoneNumber().substring(4,7) + " " + getPhoneNumber().substring(7);
        } else if (getPhoneNumber().length() == 9) {
            return "(0) " + getPhoneNumber().substring(0,3) + " " + getPhoneNumber().substring(3,6) + " " +
                    getPhoneNumber().substring(6,9);
        } else {
            return null;
        }
    }

    public IdentificationEvent currentCheckIn() {
        try {
            return IdentificationEventDao.openCheckIn(getId());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            return null;
        }
    }

    public Member createNewborn() {
        Member newborn = new Member();
        newborn.setHouseholdId(getHouseholdId());
        newborn.setBirthdateAccuracy(BirthdateAccuracyEnum.D);
        newborn.setEnrolledAt(Clock.getCurrentTime());
        return newborn;
    }
}
