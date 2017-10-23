package org.watsi.uhp.helpers;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.BuildConfigManager;

/**
 * Helper class for shared code across activities.
 */

public class ActivityHelper {
    public static void setupBannerIfInTrainingMode(Activity activity) {
        TextView textView = (TextView) activity.findViewById(R.id.training_mode_banner);
        if (BuildConfigManager.isTrainingFlavour()) {
            textView.setVisibility(View.VISIBLE);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}
