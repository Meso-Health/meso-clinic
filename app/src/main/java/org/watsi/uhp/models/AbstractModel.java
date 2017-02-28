package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

/**
 * Abstract class for specifying any fields and behavior that all models should share
 */
public abstract class AbstractModel {

    public static final String FIELD_NAME_CREATED_AT = "created_at";

    @DatabaseField(columnName = FIELD_NAME_CREATED_AT)
    private Date mCreatedAt;

    public Date getCreatedAt() {
        return this.mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.mCreatedAt = createdAt;
    }
}
