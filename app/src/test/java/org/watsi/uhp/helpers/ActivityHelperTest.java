package org.watsi.uhp.helpers;

import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.AuthenticationActivity;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.BuildConfigManager;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

// All these decorators are from: https://github.com/robolectric/robolectric/wiki/Using-PowerMock
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*", "me.*"})
@PrepareForTest({ BuildConfigManager.class })
public class ActivityHelperTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void setUp() throws Exception {
        mockStatic(BuildConfigManager.class);
    }

    @Test
    public void setupBannerIfInTrainingMode_clinicActivity_notTraining() throws Exception {
        when(BuildConfigManager.isTrainingFlavour()).thenReturn(false);
        ClinicActivity activity  = Robolectric.buildActivity(ClinicActivity.class)
                .create().start().resume().get();
        TextView textView = (TextView) activity.findViewById(R.id.training_mode_banner);
        assertEquals(textView.getVisibility(), View.GONE);
    }

    @Test
    public void setupBannerIfInTrainingMode_clinicActivity_training() throws Exception {
        when(BuildConfigManager.isTrainingFlavour()).thenReturn(true);
        ClinicActivity activity = Robolectric.buildActivity(ClinicActivity.class)
                .create().start().resume().get();
        TextView textView = (TextView) activity.findViewById(R.id.training_mode_banner);
        assertEquals(textView.getVisibility(), View.VISIBLE);
    }

    @Test
    public void setupBannerIfInTrainingMode_authenticationActivity_notTraining() throws Exception {
        when(BuildConfigManager.isTrainingFlavour()).thenReturn(false);
        AuthenticationActivity activity = Robolectric.buildActivity(AuthenticationActivity.class)
                .create().start().resume().get();
        TextView textView = (TextView) activity.findViewById(R.id.training_mode_banner);
        assertEquals(textView.getVisibility(), View.GONE);
    }

    @Test
    public void setupBannerIfInTrainingMode_authenticationActivity_training() throws Exception {
        when(BuildConfigManager.isTrainingFlavour()).thenReturn(true);
        AuthenticationActivity activity = Robolectric.buildActivity(AuthenticationActivity.class)
                .create().start().resume().get();
        TextView textView = (TextView) activity.findViewById(R.id.training_mode_banner);
        assertEquals(textView.getVisibility(), View.VISIBLE);
    }
}
