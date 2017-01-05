package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = Member.TABLE_NAME_MEMBERS)
public class Member {

    public static final String TABLE_NAME_MEMBERS = "members";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAME = "name";
    public static final String FIELD_NAME_BIRTHDATE = "birthdate";
    public static final String FIELD_NAME_GENDER = "gender";
    public static final String FIELD_NAME_PHONE_NUMBER = "phone_number";

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

    private enum GenderEnum { M, F }
}
