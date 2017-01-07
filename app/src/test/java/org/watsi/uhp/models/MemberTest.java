package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MemberTest {

    Member member;

    @Before
    public void setup() {
        member = new Member();
    }

    @Test
    public void getName_getsTheName() throws Exception {
        member.setName("Foo");
        assertEquals(member.getName(), "Foo");
    }
}
