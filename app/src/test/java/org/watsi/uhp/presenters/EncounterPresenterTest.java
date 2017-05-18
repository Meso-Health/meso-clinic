package org.watsi.uhp.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterPresenterTest {

    private EncounterPresenter encounterPresenter;
    private Encounter encounter;

    @Mock
    EncounterItemAdapter encounterItemAdapter;

    @Before
    public void setup() {
        initMocks(this);
        encounter = new Encounter();
        encounterPresenter = new EncounterPresenter(encounter, encounterItemAdapter);

        Date occurredAt = Calendar.getInstance().getTime();
        encounter.setOccurredAt(occurredAt);
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

    @Test
    public void newDateLinkText() throws Exception {
        assertThat(encounterPresenter.newDateLinkText(encounter), containsString(encounterPresenter.dateFormatter(encounter.getOccurredAt())));
    }
}
