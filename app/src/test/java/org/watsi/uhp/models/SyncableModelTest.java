package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class SyncableModelTest {

    // SyncableModel is an abstract class so testing with an implementation
    private Member member;

    @Before
    public void setup() {
        member = new Member();
    }

    @Test
    public void setUnsynced_setsToken() throws Exception {
        String token = "foo";
        member.setToken(token);

        member.setUnsynced(token);

        assertEquals(member.getToken(), token);
    }

    @Test
    public void dirty() throws Exception {
        String dirtyFieldName = Member.FIELD_NAME_FULL_NAME;
        member.addDirtyField(dirtyFieldName);

        assertTrue(member.dirty(dirtyFieldName));
        assertFalse(member.dirty(Member.FIELD_NAME_PHONE_NUMBER));
    }

    @Test
    public void isDirty() throws Exception {
        assertFalse(member.isDirty());

        member.addDirtyField(Member.FIELD_NAME_FULL_NAME);

        assertTrue(member.isDirty());
    }

    @Test
    public void getTokenAuthHeaderString() throws Exception {
        member.setToken("foo");

        assertEquals(member.getTokenAuthHeaderString(), "Token foo");
    }
}
