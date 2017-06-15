package org.watsi.uhp.presenters;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ BillableDao.class, EncounterPresenter.class })
public class EncounterPresenterTest {

    private EncounterPresenter encounterPresenter;
    private Encounter encounter;

    @Mock
    View view;

    @Mock
    Spinner spinner;

    @Mock
    SearchView searchView;

    @Mock
    ListView listView;

    @Mock
    TextView textView;

    @Mock
    ArrayAdapter mockArrayAdapter;

    @Mock
    EncounterItemAdapter encounterItemAdapter;

    @Mock
    Context context;

    @Mock
    NavigationManager navigationManager;

    @Mock
    EncounterFragment encounterFragment;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(BillableDao.class);

        encounter = new Encounter();

        Date occurredAt = Calendar.getInstance().getTime();
        encounter.setOccurredAt(occurredAt);

        encounterPresenter = new EncounterPresenter(encounter, view, context, encounterItemAdapter, navigationManager, encounterFragment);
    }

    @Test
    public void getCategorySpinner() throws Exception {
        when(view.findViewById(R.id.category_spinner)).thenReturn(spinner);

        assertEquals(encounterPresenter.getCategorySpinner(), spinner);
    }

    @Test
    public void getBillableSpinner() throws Exception {
        when(view.findViewById(R.id.billable_spinner)).thenReturn(spinner);

        assertEquals(encounterPresenter.getBillableSpinner(), spinner);
    }

    @Test
    public void getDrugSearchView() throws Exception {
        when(view.findViewById(R.id.drug_search)).thenReturn(searchView);

        assertEquals(encounterPresenter.getDrugSearchView(), searchView);
    }

    @Test
    public void getLineItemsList() throws Exception {
        when(view.findViewById(R.id.line_items_list)).thenReturn(listView);

        assertEquals(encounterPresenter.getLineItemsListView(), listView);
    }

    @Test
    public void getBackdateEncounterLink() throws Exception {
        when(view.findViewById(R.id.backdate_encounter)).thenReturn(textView);

        assertEquals(encounterPresenter.getBackdateEncounterLink(), textView);
    }

    @Test
    public void setup_backdateLinkText() throws Exception {
        encounter.setBackdatedOccurredAt(true);
        encounterPresenter.mFormattedBackDate = encounter.getOccurredAt().toString();

        when(encounterPresenter.getLineItemsListView()).thenReturn(listView);
        when(encounterPresenter.getCategorySpinner()).thenReturn(spinner);
        when(encounterPresenter.getDrugSearchView()).thenReturn(searchView);
        when(encounterPresenter.getAddBillablePrompt()).thenReturn(textView);
        when(encounterPresenter.getBackdateEncounterLink()).thenReturn(textView);

        encounterPresenter.setUp();
        Mockito.verify(encounterFragment, Mockito.times(1)).updateBackdateLinkText();
    }

    @Test
    public void getAddBillablePrompt() throws Exception {
        when(view.findViewById(R.id.add_billable_prompt)).thenReturn(textView);

        assertEquals(encounterPresenter.getAddBillablePrompt(), textView);
    }

    @Test
    public void clearDrugSearch() throws Exception {
        when(encounterPresenter.getDrugSearchView()).thenReturn(searchView);

        encounterPresenter.clearDrugSearch();
        assertEquals(encounterPresenter.getDrugSearchView().findFocus(), null);
        assertEquals(encounterPresenter.getDrugSearchView().getQuery(), null);
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
    public void setFormattedBackDate() throws Exception {
        encounterPresenter.setFormattedBackDate();

        assertNotNull(encounterPresenter.mFormattedBackDate);

        Date date = new SimpleDateFormat("MMM d, H:mma").parse(encounterPresenter.mFormattedBackDate);
        assertEquals(date.getMonth(), encounter.getOccurredAt().getMonth());
        assertEquals(date.getDate(), encounter.getOccurredAt().getDate());
    }

    @Test
    public void promptBillable() throws Exception {
        assertEquals(encounterPresenter.promptBillable("FOO").getClass(), Billable.class);
        assertEquals(encounterPresenter.promptBillable("FOO").toString(), "Select a foo...");
    }

    @Test
    public void getBillablesList() throws Exception {
        List<Billable> billables = new ArrayList<>();
        Billable fakeLab1 = new Billable();
        Billable fakeLab2 = new Billable();
        Billable fakeLab3 = new Billable();
        fakeLab1.setName("fake lab 1");
        fakeLab2.setName("fake lab 2");
        fakeLab3.setName("fake lab 3");
        billables.add(fakeLab1);
        billables.add(fakeLab2);
        billables.add(fakeLab3);

        when(BillableDao.getBillablesByCategory(Billable.TypeEnum.LAB)).thenReturn(billables);

        List<Billable> billablesList = encounterPresenter.getBillablesList(Billable.TypeEnum.LAB);

        assertEquals(billablesList.toString(), "[Select a lab..., fake lab 1, fake lab 2, fake lab 3]");
    }

    @Test
    public void getEncounterItemAdapter() throws Exception {
        whenNew(ArrayAdapter.class)
                .withParameterTypes(Context.class, int.class, List.class)
                .withArguments(Matchers.eq(context), Matchers.eq(android.R.layout.simple_spinner_dropdown_item), Matchers.anyList())
                .thenReturn(mockArrayAdapter);

        ArrayAdapter<Billable> result = encounterPresenter.getEncounterItemAdapter(Billable.TypeEnum.LAB);

        assertEquals(result, mockArrayAdapter);
    }

    @Test
    public void getCategoriesList() throws Exception {
        List<String> categoriesList = encounterPresenter.getCategoriesList("foo");

        assertEquals(categoriesList.toString(), "[foo, DRUG, SERVICE, LAB, SUPPLY, VACCINE]");
    }

    @Test
    public void getCategoriesAdapter() throws Exception {
        whenNew(ArrayAdapter.class)
                .withParameterTypes(android.content.Context.class, int.class, java.util.List.class)
                .withArguments(Matchers.eq(context), Matchers.eq(android.R.layout.simple_spinner_dropdown_item), Matchers.anyList())
                .thenReturn(mockArrayAdapter);

        ArrayAdapter<String> result = encounterPresenter.getCategoriesAdapter("foo");
        assertEquals(result, mockArrayAdapter);
    }
    
}
