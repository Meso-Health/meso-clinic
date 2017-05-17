package org.watsi.uhp.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterPresenterTest {

    private EncounterPresenter encounterPresenter;

    @Mock
    Encounter encounter;

    @Mock
    EncounterItemAdapter encounterItemAdapter;

    @Before
    public void setup() {
        initMocks(this);
        encounterPresenter = new EncounterPresenter(encounter, encounterItemAdapter);
    }

    @Test
    public void addToEncounterItemList() throws Exception {
        Billable billable = mock(Billable.class);
        encounterPresenter.addToEncounterItemList(billable);

        assertNotNull(encounterPresenter.mEncounter.getEncounterItems());
        EncounterItem encounterItem = encounterPresenter.mEncounter.getEncounterItems().get(0);
        assertNotNull(encounterItem);

        assertEquals(encounterItem.getBillable(), billable);
        verify(encounterItemAdapter, times(1)).add(encounterItem);
    }
}
