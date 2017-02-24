package org.watsi.uhp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@DatabaseTable(tableName = Member.TABLE_NAME)
public class Member extends AbstractModel {

    public static final String TABLE_NAME = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_CARD_ID = "card_id";
    public static final String FIELD_NAME_FULL_NAME = "full_name";
    public static final String FIELD_NAME_AGE = "age";
    public static final String FIELD_NAME_GENDER = "gender";
    public static final String FIELD_NAME_PHOTO = "photo";
    public static final String FIELD_NAME_PHOTO_URL = "photo_url";
    public static final String FIELD_NAME_HOUSEHOLD_ID = "household_id";
    public static final String FIELD_NAME_ABSENTEE = "absentee";

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

    @Expose
    @SerializedName(FIELD_NAME_HOUSEHOLD_ID)
    @DatabaseField(columnName = FIELD_NAME_HOUSEHOLD_ID)
    private UUID mHouseholdId;

    @Expose
    @SerializedName(FIELD_NAME_ABSENTEE)
    @DatabaseField(columnName = FIELD_NAME_ABSENTEE)
    private Boolean mAbsentee;

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

    public UUID getId() {
        return mId;
    }

    public String getCardId() {
        return mCardId;
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

    public GenderEnum getGender() {
        return mGender;
    }

    public void setGender(GenderEnum gender) {
        this.mGender = gender;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;

        Member otherMember = (Member) o;

        return getId().equals(otherMember.getId());
    }
}
