package org.watsi.uhp.custom_components;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.DiagnosisFactory;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.database.DaoTest;
import org.watsi.uhp.fragments.DiagnosisFragment;
import org.watsi.uhp.models.Encounter;

import static junit.framework.Assert.assertNotNull;
import static org.watsi.uhp.managers.NavigationManager.SYNCABLE_MODEL_BUNDLE_FIELD;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DiagnosisFuzzySearchInputTest extends DaoTest {
    Encounter mEncounter = new Encounter();
    DiagnosisFragment mDiagnosisFragment;
    DiagnosisFuzzySearchInput mDiagnosisFuzzySearchInput;

    @Before
    public void setUp() throws Exception {
        DiagnosisFactory.createDiagnosis(1, "Malaria", null);
        DiagnosisFactory.createDiagnosis(2, "Salmoneria", null);
        mDiagnosisFragment = new DiagnosisFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, mEncounter);
        mDiagnosisFragment.setArguments(bundle);
        SupportFragmentTestUtil.startFragment(mDiagnosisFragment, ClinicActivity.class);
        mDiagnosisFuzzySearchInput = mDiagnosisFragment.getDiagnosisFuzzySearchInput();
    }

    @Test
    public void setUpSuccess() throws Exception {
        assertNotNull(mDiagnosisFragment.getView());
    }
}