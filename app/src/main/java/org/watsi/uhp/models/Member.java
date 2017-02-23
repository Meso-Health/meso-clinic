package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.watsi.uhp.database.MemberDao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

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

    public Collection<IdentificationEvent> getIdentifications() {
        return mIdentificationEvents;
    }

    public IdentificationEvent getLastIdentification() {
        ArrayList<IdentificationEvent> allIdentificationEvents = new ArrayList<>(getIdentifications());
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

    public Target createTarget() {
        final Member self = this;
        return new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                setPhoto(stream.toByteArray());
                 try {
                     MemberDao.update(self);
                     stream.close();
                } catch (SQLException | IOException e) {
                    Rollbar.reportException(e);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("UHP", "bitmap failed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // no-op
            }
        };
    }

    public void fetchAndSetPhotoFromUrl(Target target, Context context) throws IOException, SQLException {
        final Context finalContext = context;
        final Target finalTarget = target;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Picasso.with(finalContext).load(getPhotoUrl()).into(finalTarget);
            }
        });
    }

    public Bitmap getPhotoBitmap() {
        if (mPhoto != null) {
            return BitmapFactory.decodeByteArray(this.mPhoto, 0, this.mPhoto.length);
        } else {
            return null;
        }
    }
}
