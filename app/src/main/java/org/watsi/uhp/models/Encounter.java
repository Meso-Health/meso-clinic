package org.watsi.uhp.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@DatabaseTable(tableName = Encounter.TABLE_NAME)
public class Encounter extends AbstractModel {

    public static final String TABLE_NAME = "encounters";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_MEMBER_ID = "member_id";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private UUID mId;

    @DatabaseField(columnName = FIELD_NAME_MEMBER_ID, foreign = true, canBeNull = false)
    private Member mMember;

    @ForeignCollectionField
    Collection<LineItem> mLineItems;

    public Encounter() {
        super();
    }

    public Encounter(List<LineItem> lineItems) {
        this.mLineItems = lineItems;
    }

    public UUID getId() {
        return mId;
    }

    public Member getMember() {
        return mMember;
    }

    public void setMember(Member member) {
        this.mMember = member;
    }

    public Collection<LineItem> getLineItems() {
        return mLineItems;
    }

    public void setLineItems(Collection<LineItem> lineItems) {
        this.mLineItems = lineItems;
    }
}
