package org.watsi.uhp.models;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;

public class DiagnosisTest {
    @Test
    public void constructor_noArgs() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        assertNull(diagnosis.getId());
    }

    @Test
    public void toString_noDescription() throws Exception {
        Diagnosis diagnosis = new Diagnosis();
        assertNull(diagnosis.toString());
    }

    @Test
    public void toString_withDescription() throws Exception {
        Diagnosis diagnosis = new Diagnosis(1, "description", null);
        assertEquals(diagnosis.toString(), "description");
    }

    @Test
    public void equals_sameDescription() throws Exception {
        Diagnosis diagnosis1 = new Diagnosis(1, "description", null);
        Diagnosis diagnosis2 = new Diagnosis(2, "description", null);
        assertFalse(diagnosis1.equals(diagnosis2));
    }
}
