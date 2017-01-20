package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.rollbar.android.Rollbar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.watsi.uhp.database.CheckInDao;
import org.watsi.uhp.database.MemberDao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DatabaseTable(tableName = Member.TABLE_NAME)
public class Member {

    public static final String TABLE_NAME = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAME = "name";
    public static final String FIELD_NAME_BIRTHDATE = "birthdate";
    public static final String FIELD_NAME_GENDER = "gender";
    public static final String FIELD_NAME_PHONE_NUMBER = "phone_number";
    public static final String FIELD_NAME_PHOTO = "photo";
    public static final String FIELD_NAME_PHOTO_URL = "photo_url";

    private enum GenderEnum { M, F }

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_NAME)
    private String mName;

    @DatabaseField(columnName = FIELD_NAME_BIRTHDATE)
    private Date mBirthdate;

    @DatabaseField(columnName = FIELD_NAME_GENDER)
    private GenderEnum mGender;

    @DatabaseField(columnName = FIELD_NAME_PHONE_NUMBER)
    private String mPhoneNumber;

    @DatabaseField(columnName = FIELD_NAME_PHOTO, dataType = DataType.BYTE_ARRAY)
    private byte[] mPhoto;

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

    public int getId() {
        return this.mId;
    }

    public Date getBirthdate() {
        return mBirthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.mBirthdate = birthdate;
    }

    public GenderEnum getGender() {
        return mGender;
    }

    public void setGender(GenderEnum gender) {
        this.mGender = gender;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
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

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
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

    public CheckIn getLastCheckIn() throws SQLException {
        Map<String,Object> queryMap = new HashMap<String,Object>();
        queryMap.put(CheckIn.FIELD_NAME_MEMBER_ID, getId());
        List<CheckIn> checkIns = CheckInDao.find(queryMap);
        if (checkIns.size() > 0) {
            return checkIns.get(0);
        } else {
            return null;
        }
    }
}
