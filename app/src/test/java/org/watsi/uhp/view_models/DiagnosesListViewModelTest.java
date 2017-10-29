package org.watsi.uhp.view_models;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.Encounter;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DiagnosesListViewModelTest {
    private Encounter mEncounter;
    private Context mContext;

    @Before
    public void setup() throws Exception {
        ClinicActivity clinicActivity = Robolectric.buildActivity(ClinicActivity.class)
                .create().start().resume().get();
        mContext = clinicActivity.getApplicationContext();
        mEncounter = new Encounter();
    }

    @Test
    public void getSelectedDiagnoses_oneDiagnosis() throws Exception {
        Diagnosis diagnosis = new Diagnosis(1, "Malaria", null);
        mEncounter.addDiagnosis(diagnosis);
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        assertEquals(diagnosesListViewModel.getSelectedDiagnoses().size(), 1);
        DiagnosisItemViewModel diagnosisItemViewModel = diagnosesListViewModel.getSelectedDiagnoses().get(0);
        assertEquals(diagnosisItemViewModel.getDiagnosisDescription(), diagnosis.getDescription());
    }

    @Test
    public void getSelectedDiagnoses_noDiagnosis() throws Exception {
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        assertEquals(diagnosesListViewModel.getSelectedDiagnoses().size(), 0);
    }

    @Test
    public void getFormattedCount_multipleDiagnoses() throws Exception {
        Diagnosis d1 = new Diagnosis(1, "Malaria", null);
        Diagnosis d2 = new Diagnosis(2, "Byronium", null);
        mEncounter.addDiagnosis(d1);
        mEncounter.addDiagnosis(d2);
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        assertEquals(diagnosesListViewModel.getFormattedCount(), "2 Diagnoses");
    }

    @Test
    public void getFormattedCount_noDiagnoses() throws Exception {
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        assertEquals(diagnosesListViewModel.getFormattedCount(), "No Diagnoses");
    }

    @Test
    public void getFormattedCount_multipleDiagnosis() throws Exception {
        Diagnosis d1 = new Diagnosis(1, "Malaria", null);
        mEncounter.addDiagnosis(d1);
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        assertEquals(diagnosesListViewModel.getFormattedCount(), "1 Diagnosis");
    }

    @Test
    public void removeDiagnosis() throws Exception {
        Diagnosis d1 = new Diagnosis(1, "Malaria", null);
        mEncounter.addDiagnosis(d1);
        DiagnosesListViewModel diagnosesListViewModel = new DiagnosesListViewModel(mContext, mEncounter, true);
        diagnosesListViewModel.removeDiagnosis(d1);
        assertEquals(diagnosesListViewModel.getFormattedCount(), "No Diagnoses");
    }
}
