package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.managers.NotificationManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@DatabaseTable(tableName = Member.TABLE_NAME)
public class Member extends SyncableModel {

    public static final String TABLE_NAME = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_CARD_ID = "card_id";
    public static final String FIELD_NAME_FULL_NAME = "full_name";
    public static final String FIELD_NAME_AGE = "age";
    public static final String FIELD_NAME_GENDER = "gender";
    public static final String FIELD_NAME_PHOTO = "photo";
    public static final String FIELD_NAME_PHOTO_URL = "photo_url";
    public static final String FIELD_NAME_NATIONAL_ID_PHOTO = "national_id_photo";
    public static final String FIELD_NAME_NATIONAL_ID_PHOTO_URL = "national_id_photo_url";
    public static final String FIELD_NAME_HOUSEHOLD_ID = "household_id";
    public static final String FIELD_NAME_ABSENTEE = "absentee";
    public static final String FIELD_NAME_FINGERPRINTS_GUID = "fingerprints_guid";
    public static final String FIELD_NAME_PHONE_NUMBER = "phone_number";

    public static final int MINIMUM_FINGERPRINT_AGE = 6;
    public static final int MINIMUM_NATIONAL_ID_AGE = 18;

    public enum GenderEnum { M, F }

    @Expose
    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, id = true)
    private UUID mId;

    @Expose
    @SerializedName(FIELD_NAME_CARD_ID)
    @DatabaseField(columnName = FIELD_NAME_CARD_ID)
    private String mCardId;

    @Expose
    @SerializedName(FIELD_NAME_FULL_NAME)
    @DatabaseField(columnName = FIELD_NAME_FULL_NAME, canBeNull = false)
    private String mFullName;

    @Expose
    @SerializedName(FIELD_NAME_AGE)
    @DatabaseField(columnName = FIELD_NAME_AGE)
    private int mAge;

    @Expose
    @SerializedName(FIELD_NAME_GENDER)
    @DatabaseField(columnName = FIELD_NAME_GENDER)
    private GenderEnum mGender;

    @DatabaseField(columnName = FIELD_NAME_PHOTO, dataType = DataType.BYTE_ARRAY)
    private byte[] mPhoto;

    @Expose
    @SerializedName(FIELD_NAME_PHOTO_URL)
    @DatabaseField(columnName = FIELD_NAME_PHOTO_URL)
    private String mPhotoUrl;

    @DatabaseField(columnName = FIELD_NAME_NATIONAL_ID_PHOTO, dataType = DataType.BYTE_ARRAY)
    private byte[] mNationalIdPhoto;

    @Expose
    @SerializedName(FIELD_NAME_NATIONAL_ID_PHOTO_URL)
    @DatabaseField(columnName = FIELD_NAME_NATIONAL_ID_PHOTO_URL)
    private String mNationalIdPhotoUrl;

    @Expose
    @SerializedName(FIELD_NAME_HOUSEHOLD_ID)
    @DatabaseField(columnName = FIELD_NAME_HOUSEHOLD_ID)
    private UUID mHouseholdId;

    @Expose
    @SerializedName(FIELD_NAME_ABSENTEE)
    @DatabaseField(columnName = FIELD_NAME_ABSENTEE)
    private Boolean mAbsentee;

    @Expose
    @SerializedName(FIELD_NAME_FINGERPRINTS_GUID)
    @DatabaseField(columnName = FIELD_NAME_FINGERPRINTS_GUID)
    private UUID mFingerprintsGuid;

    @Expose
    @SerializedName(FIELD_NAME_PHONE_NUMBER)
    @DatabaseField(columnName = FIELD_NAME_PHONE_NUMBER)
    private String mPhoneNumber;

    @ForeignCollectionField(orderColumnName = IdentificationEvent.FIELD_NAME_CREATED_AT)
    private final Collection<IdentificationEvent> mIdentificationEvents = new ArrayList<>();

    public Member() {
        super();
    }

    public void setFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.isEmpty()) {
            throw new ValidationException(FIELD_NAME_FULL_NAME, "Name cannot be blank");
        } else {
            addDirtyField(FIELD_NAME_FULL_NAME);
            this.mFullName = fullName;
        }
    }

    public String getFullName() {
        return this.mFullName;
    }

    public void setId(UUID id) { this.mId = id; }

    public UUID getId() {
        return mId;
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

    public void setCardId(String cardId) throws ValidationException {
        if (validCardId(cardId)) {
            addDirtyField(FIELD_NAME_CARD_ID);
            this.mCardId = cardId;
        } else {
            throw new ValidationException(FIELD_NAME_CARD_ID, "Card must be 3 letters followed by 6 numbers");
        }
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        addDirtyField(FIELD_NAME_AGE);
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
            return "Male";
        } else {
            return "Female";
        }
    }

    public byte[] getPhoto() {
        return mPhoto;
    }

    public void setPhoto(byte[] photoBytes) {
        addDirtyField(FIELD_NAME_PHOTO);
        this.mPhoto = photoBytes;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public byte[] getNationalIdPhoto() {
        return mNationalIdPhoto;
    }

    public void setNationalIdPhoto(byte[] nationalIdPhoto) {
        addDirtyField(FIELD_NAME_NATIONAL_ID_PHOTO);
        this.mNationalIdPhoto = nationalIdPhoto;
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

    public void setAbsentee(boolean absentee) {
        this.mAbsentee = absentee;
    }

    public Boolean getAbsentee() {
        return mAbsentee;
    }

    public Collection<IdentificationEvent> getIdentificationEvents() {
        return mIdentificationEvents;
    }

    public IdentificationEvent getLastIdentification() {
        ArrayList<IdentificationEvent> allIdentificationEvents = new ArrayList<>(getIdentificationEvents());
        return allIdentificationEvents.get(allIdentificationEvents.size() -1);
    }

    public void setIdentifications(Collection<IdentificationEvent> identificationEvents) {
        this.mIdentificationEvents.clear();
        this.mIdentificationEvents.addAll(identificationEvents);
    }

    public UUID getFingerprintsGuid() {
        return mFingerprintsGuid;
    }

    public void setFingerprintsGuid(UUID fingerprintsGuid) {
        addDirtyField(FIELD_NAME_FINGERPRINTS_GUID);
        this.mFingerprintsGuid = fingerprintsGuid;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) throws ValidationException {
        if (phoneNumber == null) {
            this.mPhoneNumber = null;
        } else {
            if (Member.validPhoneNumber(phoneNumber)) {
                addDirtyField(FIELD_NAME_PHONE_NUMBER);
                this.mPhoneNumber = phoneNumber;
            } else {
                throw new ValidationException(FIELD_NAME_PHONE_NUMBER, "Invalid phone number");
            }
        }
    }

    public void fetchAndSetPhotoFromUrl() throws IOException {
        Request request = new Request.Builder().url(getPhotoUrl()).build();
        Response response = new OkHttpClient().newCall(request).execute();

        if (response.isSuccessful()) {
            InputStream is = response.body().byteStream();
            setPhoto(ByteStreams.toByteArray(is));
            is.close();
            Log.d("UHP", "finished fetching photo at: " + getPhotoUrl());
        } else {
            Map<String,String> params = new HashMap<>();
            params.put("member.id", getId().toString());
            NotificationManager.requestFailure(
                    "Failed to fetch member photo",
                    request,
                    response,
                    params
            );
        }
    }

    public Bitmap getPhotoBitmap() {
        if (mPhoto != null) {
            return BitmapFactory.decodeByteArray(this.mPhoto, 0, this.mPhoto.length);
        } else {
            return null;
        }
    }

    public boolean shouldCaptureFingerprint() {
        return getAge() >= Member.MINIMUM_FINGERPRINT_AGE;
    }

    public boolean shouldCaptureNationalIdPhoto() {
        return getAge() >= Member.MINIMUM_NATIONAL_ID_AGE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;

        Member otherMember = (Member) o;

        return getId().equals(otherMember.getId());
    }

    public Map<String, RequestBody> formatPatchRequest(Context context) {
        Map<String, RequestBody> requestPartMap = new HashMap<>();

        if (dirty(FIELD_NAME_PHOTO)) {
            byte[] image = FileManager.readFromUri(Uri.parse(getPhotoUrl()), context);
            requestPartMap.put(FIELD_NAME_PHOTO, RequestBody.create(MediaType.parse("image/jpg"), image));
        }

        if (dirty(FIELD_NAME_NATIONAL_ID_PHOTO)) {
            byte[] image =  FileManager.readFromUri(Uri.parse(getNationalIdPhotoUrl()), context);
            requestPartMap.put(FIELD_NAME_NATIONAL_ID_PHOTO, RequestBody.create(MediaType.parse("image/jpg"), image));
        }

        if (dirty(FIELD_NAME_FINGERPRINTS_GUID)) {
            requestPartMap.put(
                    FIELD_NAME_FINGERPRINTS_GUID,
                    RequestBody.create(MultipartBody.FORM, getFingerprintsGuid().toString())
            );
        }

        if (dirty(FIELD_NAME_PHONE_NUMBER)) {
            requestPartMap.put(
                    FIELD_NAME_PHONE_NUMBER,
                    RequestBody.create(MultipartBody.FORM, getPhoneNumber())
            );
        }

        if (dirty(FIELD_NAME_FULL_NAME)) {
            requestPartMap.put(
                    FIELD_NAME_FULL_NAME,
                    RequestBody.create(MultipartBody.FORM, getFullName())
            );
        }

        if (dirty(FIELD_NAME_CARD_ID)) {
            requestPartMap.put(
                    FIELD_NAME_CARD_ID,
                    RequestBody.create(MultipartBody.FORM, getCardId())
            );
        }

        return requestPartMap;
    }

    public static boolean validCardId(String cardId) {
        if (cardId == null || cardId.isEmpty()) {
            return false;
        } else {
            return cardId.matches("RWI[0-9]{6}");
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

    public void deleteLocalImages() {
        if (getPhotoUrl() != null && FileManager.isLocal(getPhotoUrl())) {
            new File(getPhotoUrl()).delete();
            setPhotoUrl(null);
        }
        if (getNationalIdPhotoUrl() != null && FileManager.isLocal(getNationalIdPhotoUrl())) {
            new File(getNationalIdPhotoUrl()).delete();
            setNationalIdPhotoUrl(null);
        }
    }

    public UUID currentCheckInId() {
        try {
            return IdentificationEventDao.openCheckIn(getId());
        } catch (SQLException e) {
            Rollbar.reportException(e);
            return null;
        }
    }
}
