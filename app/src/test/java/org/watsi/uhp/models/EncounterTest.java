package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.EncounterItemDao;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, EncounterItemDao.class })
public class EncounterTest {

    private Encounter encounter;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        mockStatic(EncounterItemDao.class);
        encounter = new Encounter();
    }

    @Test
    public void addEncounterForm_setsEncounterOnForm() throws Exception {
        EncounterForm form = new EncounterForm();

        encounter.addEncounterForm(form);

        assertEquals(form.getEncounter(), encounter);
    }

    @Test
    public void price_noItems() throws Exception {
        assertEquals(encounter.price(), 0);
    }

    @Test
    public void price_withItems() throws Exception {
        List<EncounterItem> encounterItems = new ArrayList<>();
        EncounterItem ei1 = new EncounterItem();
        Billable b1 = new Billable();
        b1.setPrice(500);
        ei1.setQuantity(2);
        ei1.setBillable(b1);
        encounterItems.add(ei1);

        EncounterItem ei2 = new EncounterItem();
        Billable b2 = new Billable();
        b2.setPrice(1400);
        ei2.setQuantity(4);
        ei2.setBillable(b2);
        encounterItems.add(ei2);

        encounter.setEncounterItems(encounterItems);
        assertEquals(encounter.price(), 6600);
    }

    @Test
    public void addEncounterItem_doesNotContainBillable_addsItem() throws Exception {
        EncounterItem encounterItem = new EncounterItem();

        encounter.addEncounterItem(encounterItem);

        assertTrue(encounter.getEncounterItems().contains(encounterItem));
    }

    @Test(expected=Encounter.DuplicateBillableException.class)
    public void addEncounterItem_alreadyContainsBillable_throwsException() throws Exception {
        Billable billable = new Billable();
        billable.generateId();
        EncounterItem item1 = new EncounterItem();
        item1.setBillable(billable);
        EncounterItem item2 = new EncounterItem();
        item2.setBillable(billable);

        encounter.addEncounterItem(item1);
        encounter.addEncounterItem(item2);
    }

    @Test
    public void addDiagnosis_doesNotContainDiagnosis_addsItem() throws Exception {
        Diagnosis diagnosis = new Diagnosis(1, "diagnosis description", null);
        Diagnosis diagnosis2 = new Diagnosis(2, "diagnosis description", null);

        encounter.addDiagnosis(diagnosis);
        encounter.addDiagnosis(diagnosis2);
        assertTrue(encounter.getDiagnoses().contains(diagnosis));
        assertTrue(encounter.getDiagnoses().contains(diagnosis2));
    }

    @Test(expected=Encounter.DuplicateDiagnosisException.class)
    public void addDiagnosis_alreadyContainsDiagnosis_throwsException() throws Exception {
        Diagnosis d1 = new Diagnosis(1, "description", null);
        encounter.addDiagnosis(d1);
        encounter.addDiagnosis(d1);
    }

    @Test
    public void removeEncounterItem() throws Exception {
        EncounterItem item = new EncounterItem();

        encounter.addEncounterItem(item);
        encounter.removeEncounterItem(item);

        assertFalse(encounter.getEncounterItems().contains(item));
    }
}
