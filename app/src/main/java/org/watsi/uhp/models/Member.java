package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.watsi.uhp.api.ApiService;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;

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

    @ForeignCollectionField
    private final Collection<Encounter> mEncounters = new ArrayList<>();
    
    public Member() {
        super();
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
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

    public void setCardId(String cardId) {
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
            return "Male";
        } else {
            return "Female";
        }
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

    public byte[] getNationalIdPhoto() {
        return mNationalIdPhoto;
    }

    public void setNationalIdPhoto(byte[] nationalIdPhoto) {
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
    
    public Collection<Encounter> getEncounters() {
        return mEncounters;
    }

    public void setEncounters(Collection<Encounter> encounters) {
        this.mEncounters.clear();
        this.mEncounters.addAll(encounters);
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

    public void setPhoneNumber(String phoneNumber) throws ValidationException {
        if (phoneNumber == null) {
            this.mPhoneNumber = null;
        } else {
            if (Member.validPhoneNumber(phoneNumber)) {
                this.mPhoneNumber = phoneNumber;
            } else {
                throw new ValidationException(FIELD_NAME_PHONE_NUMBER, "Invalid phone number");
            }
        }
    }

    public void fetchAndSetPhotoFromUrl() throws IOException {
        Request request = new Request.Builder().url(getPhotoUrl()).build();
        Response response = new OkHttpClient().newCall(request).execute();
        InputStream is = response.body().byteStream();
        DataInputStream dis = new DataInputStream(is);
        byte[] imgData = new byte[(int) response.body().contentLength()];
        dis.readFully(imgData);
        setPhoto(imgData);

        is.close();
        dis.close();
        Log.d("UHP", "finished fetching photo at: " + getPhotoUrl());
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

    public Call<Member> formatPatchRequest(Context context) {
        String tokenAuthorizationString = "Token " + getToken();

        MultipartBody.Part memberPhoto = null;
        if (getPhoto() != null) {
            RequestBody memberPhotoRequestBody = RequestBody.create(MediaType.parse("image/jpg"), getPhoto());
            memberPhoto = MultipartBody.Part.createFormData(Member.FIELD_NAME_PHOTO, null, memberPhotoRequestBody);
        }

        MultipartBody.Part idPhoto = null;
        if (getNationalIdPhoto() != null) {
            RequestBody idPhotoRequestBody = RequestBody.create(MediaType.parse("image/jpg"), getNationalIdPhoto());
            idPhoto = MultipartBody.Part.createFormData(Member.FIELD_NAME_NATIONAL_ID_PHOTO, null, idPhotoRequestBody);
        }

        RequestBody fingerprintGuid;
        RequestBody phoneNumber;

        if (getFingerprintsGuid() == null && getPhoneNumber() == null) {
            return ApiService.requestBuilder(context).syncMember(
                    tokenAuthorizationString,
                    getId().toString(),
                    memberPhoto,
                    idPhoto
            );
        } else if (getFingerprintsGuid() != null && getPhoneNumber() == null) {
            fingerprintGuid = RequestBody.create(MultipartBody.FORM, getFingerprintsGuid().toString());

            return ApiService.requestBuilder(context).syncMember(
                    tokenAuthorizationString,
                    getId().toString(),
                    memberPhoto,
                    idPhoto,
                    fingerprintGuid
            );
        } else if (getPhoneNumber() != null && getFingerprintsGuid() == null) {
            phoneNumber = RequestBody.create(MultipartBody.FORM, getPhoneNumber());

            return ApiService.requestBuilder(context).syncMember(
                    tokenAuthorizationString,
                    getId().toString(),
                    phoneNumber,
                    memberPhoto,
                    idPhoto
            );
        } else {
            phoneNumber = RequestBody.create(MultipartBody.FORM, getPhoneNumber());
            fingerprintGuid = RequestBody.create(MultipartBody.FORM, getFingerprintsGuid().toString());

            return ApiService.requestBuilder(context).syncMember(
                    tokenAuthorizationString,
                    getId().toString(),
                    phoneNumber,
                    fingerprintGuid,
                    memberPhoto,
                    idPhoto
            );
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
            return getPhoneNumber().substring(0,3) + " " + getPhoneNumber().substring(3,6) + " " +
                    getPhoneNumber().substring(6,9);
        } else {
            return null;
        }
    }
}
