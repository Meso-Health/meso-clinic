package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.MemberDao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DatabaseTable(tableName = Member.TABLE_NAME)
public class Member {

    public static final String TABLE_NAME = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_CARD_ID = "card_id";
    public static final String FIELD_NAME_NAME = "name";
    public static final String FIELD_NAME_AGE = "age";
    public static final String FIELD_NAME_PHOTO = "photo";
    public static final String FIELD_NAME_PHOTO_URL = "photo_url";

    @SerializedName(FIELD_NAME_ID)
    @DatabaseField(columnName = FIELD_NAME_ID, id = true, canBeNull = false)
    private String mId;

    @SerializedName(FIELD_NAME_CARD_ID)
    @DatabaseField(columnName = FIELD_NAME_CARD_ID, canBeNull = false)
    private String mCardId;

    @SerializedName(FIELD_NAME_NAME)
    @DatabaseField(columnName = FIELD_NAME_NAME, canBeNull = false)
    private String mName;

    @SerializedName(FIELD_NAME_AGE)
    @DatabaseField(columnName = FIELD_NAME_AGE)
    private int mAge;

    @DatabaseField(columnName = FIELD_NAME_PHOTO, dataType = DataType.BYTE_ARRAY)
    private byte[] mPhoto;

    @SerializedName(FIELD_NAME_PHOTO_URL)
    @DatabaseField(columnName = FIELD_NAME_PHOTO_URL)
    private String mPhotoUrl;

    public Member() {
        // empty constructor necessary for ORM
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public String getId() {
        return this.mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public long getIdAsLong() {
        return Long.valueOf(getId().hashCode());
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

    public byte[] getPhoto() {
        return mPhoto;
    }

    public void setPhoto(byte[] photo_bytes) {
        this.mPhoto = photo_bytes;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void fetchAndSetPhotoFromUrl(Context context) throws IOException, SQLException {
        final Member self = this;

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                setPhoto(stream.toByteArray());
                try {
                    MemberDao.update(self);
                } catch (SQLException e) {
                    Rollbar.reportException(e);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("UHP", "bitmap failed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d("UHP", "on prepare load");
            }
        };
        Picasso.with(context).load(mPhotoUrl).into(target);
    }

    public Bitmap getPhotoBitmap() {
        if (mPhoto != null) {
            return BitmapFactory.decodeByteArray(this.mPhoto, 0, this.mPhoto.length);
        } else {
            return null;
        }
    }

    public Encounter getLastEncounter() throws SQLException {
        Map<String,Object> queryMap = new HashMap<>();
        queryMap.put(Encounter.FIELD_NAME_MEMBER_ID, getId());
        List<Encounter> encounters = EncounterDao.find(queryMap);
        if (encounters.size() > 0) {
            return encounters.get(0);
        } else {
            return null;
        }
    }
}
