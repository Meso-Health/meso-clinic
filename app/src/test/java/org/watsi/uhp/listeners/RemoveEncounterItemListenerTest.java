package org.watsi.uhp.listeners;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class RemoveEncounterItemListenerTest {

    @Mock
    View mockView;
    @Mock
    Encounter mockEncounter;
    @Mock
    EncounterItem mockEncounterItem;
    @Mock
    EncounterItemAdapter mockEncounterItemAdapter;

    private RemoveEncounterItemListener listener;

    @Before
    public void setup() {
        initMocks(this);
        listener = new RemoveEncounterItemListener(
                mockEncounter, mockEncounterItem, mockEncounterItemAdapter);
    }

    @Test
    public void onClick() throws Exception {
        listener.onClick(mockView);

        verify(mockEncounterItemAdapter, times(1)).remove(mockEncounterItem);
        verify(mockEncounter, times(1)).removeEncounterItem(mockEncounterItem);
    }
}
