package org.watsi.uhp.view_models;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Diagnosis;

import static junit.framework.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class })
public class DiagnosisItemViewModelTest {
    private Diagnosis mDiagnosis;

    @Mock
    DiagnosesListViewModel mockDiagnosesListViewModel;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        mockStatic(ExceptionManager.class);
        mDiagnosis = new Diagnosis(1, "Malaria", null);
    }

    @Test
    public void getShowClearButton_gone() {
        DiagnosisItemViewModel diagnosisItemViewModel = new DiagnosisItemViewModel(mDiagnosis, mockDiagnosesListViewModel, false);
        assertEquals(diagnosisItemViewModel.getShowClearButton(), View.GONE);
    }

    @Test
    public void getShowClearButton_visible() {
        DiagnosisItemViewModel diagnosisItemViewModel = new DiagnosisItemViewModel(mDiagnosis, mockDiagnosesListViewModel, true);
        assertEquals(diagnosisItemViewModel.getShowClearButton(), View.VISIBLE);
    }

    @Test
    public void getDiagnosisDescription() {
        DiagnosisItemViewModel diagnosisItemViewModel = new DiagnosisItemViewModel(mDiagnosis, mockDiagnosesListViewModel, true);
        assertEquals(diagnosisItemViewModel.getDiagnosisDescription(), mDiagnosis.getDescription());
    }
}
