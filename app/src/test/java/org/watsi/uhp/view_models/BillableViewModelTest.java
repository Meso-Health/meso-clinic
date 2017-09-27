package org.watsi.uhp.view_models;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Billable.class })
public class BillableViewModelTest {
    private BillableViewModel billableViewModel;
    private final List<String> COMPOSITIONS = new ArrayList<>(Arrays.asList("vial", "tablet", "syrup", "fluid"));
    private final int TYPE_DRUG_INDEX = 1;
    private final int TYPE_SERVICE_INDEX = 2;
    private final int TYPE_LAB_INDEX = 3;
    private final int TYPE_SUPPLY_INDEX = 4;
    private final int TYPE_VACCINE_INDEX = 5;

    @Mock
    FormFragment mockFormFragment;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        mockStatic(Billable.class);
        when(Billable.getBillableCompositions()).thenReturn(COMPOSITIONS);
        when(Billable.getBillableTypes()).thenCallRealMethod();
        billableViewModel = new BillableViewModel(mockFormFragment);
    }

    @Test
    public void setTypeDrug() {
        billableViewModel.setSelectedTypeIndex(TYPE_DRUG_INDEX);

        assertEquals(billableViewModel.getShowComposition(), View.VISIBLE);
        assertEquals(billableViewModel.getShowUnit(), View.VISIBLE);

        billableViewModel.setName("Drug Name");
        billableViewModel.setPrice("1000");
        billableViewModel.setComposition("tablet");

        assertFalse(billableViewModel.getSaveEnabled());
        billableViewModel.setUnit("100mg");
        assertTrue(billableViewModel.getSaveEnabled());

        assertBillableHasAttributes(billableViewModel.getBillable(), Billable.TypeEnum.DRUG,
                "Drug Name", 1000, "100mg", "tablet");
    }

    @Test
    public void setTypeService() {
        billableViewModel.setSelectedTypeIndex(TYPE_SERVICE_INDEX);

        assertEquals(billableViewModel.getShowComposition(), View.GONE);
        assertEquals(billableViewModel.getShowUnit(), View.GONE);

        billableViewModel.setName("Service Name");
        billableViewModel.setPrice("1000");
        assertTrue(billableViewModel.getSaveEnabled());

        assertBillableHasAttributes(billableViewModel.getBillable(), Billable.TypeEnum.SERVICE,
                "Service Name", 1000, null, null);
    }

    @Test
    public void setTypeLab() {
        billableViewModel.setSelectedTypeIndex(TYPE_LAB_INDEX);

        assertEquals(billableViewModel.getShowComposition(), View.GONE);
        assertEquals(billableViewModel.getShowUnit(), View.GONE);

        billableViewModel.setName("Lab Name");
        billableViewModel.setPrice("1000");
        assertTrue(billableViewModel.getSaveEnabled());

        assertBillableHasAttributes(billableViewModel.getBillable(), Billable.TypeEnum.LAB,
                "Lab Name", 1000, null, null);
    }

    @Test
    public void setTypeSupply() {
        billableViewModel.setSelectedTypeIndex(TYPE_SUPPLY_INDEX);

        assertEquals(billableViewModel.getShowComposition(), View.GONE);
        assertEquals(billableViewModel.getShowUnit(), View.GONE);

        billableViewModel.setName("Supply Name");
        billableViewModel.setPrice("1000");
        assertTrue(billableViewModel.getSaveEnabled());

        assertBillableHasAttributes(billableViewModel.getBillable(), Billable.TypeEnum.SUPPLY,
                "Supply Name", 1000, null, null);
    }


    @Test
    public void setTypeVaccines() {
        billableViewModel.setSelectedTypeIndex(TYPE_VACCINE_INDEX);

        assertEquals(billableViewModel.getShowComposition(), View.GONE);
        assertEquals(billableViewModel.getShowUnit(), View.VISIBLE);

        billableViewModel.setName("Vaccine Name");
        billableViewModel.setPrice("1000");

        assertFalse(billableViewModel.getSaveEnabled());
        billableViewModel.setUnit("100mg");
        assertTrue(billableViewModel.getSaveEnabled());

        assertBillableHasAttributes(billableViewModel.getBillable(), Billable.TypeEnum.VACCINE,
                "Vaccine Name", 1000, "100mg", "vial");
    }

    private void assertBillableHasAttributes(Billable billable, Billable.TypeEnum billableType, String billableName,
                                             int price, String units, String composition) {
        assertEquals(billable.getType(), billableType);
        assertEquals(billable.getName(), billableName);
        assertEquals(billable.getUnit(), units);
        assertEquals(billable.getPrice(), Integer.valueOf(price));
        assertEquals(billable.getComposition(), composition);
    }
}
