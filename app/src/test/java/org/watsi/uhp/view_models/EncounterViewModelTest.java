package org.watsi.uhp.view_models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.EncounterItem;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class EncounterViewModelTest {

    @Mock Encounter mockEncounter;

    private EncounterViewModel viewModel;

    @Before
    public void setup() {
        initMocks(this);
        viewModel = new EncounterViewModel(mockEncounter, RuntimeEnvironment.application);
    }

    @Test
    public void getPriceTotal_returnsFormattedPriceOfEncounter() throws Exception {
        when(mockEncounter.price()).thenReturn(1500);

        assertEquals("1,500 UGX", viewModel.getPriceTotal());
    }

    @Test
    public void getItemsCountLabel_returnsLabelDescribingItemCount() throws Exception {
        List<EncounterItem> list = new ArrayList<>();
        list.add(mock(EncounterItem.class));
        list.add(mock(EncounterItem.class));
        list.add(mock(EncounterItem.class));
        when(mockEncounter.getEncounterItems()).thenReturn(list);

        assertEquals("3 Items", viewModel.getItemsCountLabel());
    }

    @Test
    public void getFormsAttachedLabel_returnsLabelDescribingFormsCount() throws Exception {
        List<EncounterForm> list = new ArrayList<>();
        list.add(mock(EncounterForm.class));
        list.add(mock(EncounterForm.class));
        when(mockEncounter.getEncounterForms()).thenReturn(list);

        assertEquals("2 forms attached", viewModel.getFormsAttachedLabel());
    }
}
