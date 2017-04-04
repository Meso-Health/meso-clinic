package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.Date;

/**
 * Abstract class for specifying any fields and behavior that all models should share
 */
public abstract class AbstractModel implements Serializable {

    public static final String FIELD_NAME_CREATED_AT = "created_at";

    @DatabaseField(columnName = FIELD_NAME_CREATED_AT)
    private Date mCreatedAt;

    public Date getCreatedAt() {
        return this.mCreatedAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.mCreatedAt = createdAt;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String fieldName, String reason) {
            super(fieldName + ": " + reason);
        }
    }
}
