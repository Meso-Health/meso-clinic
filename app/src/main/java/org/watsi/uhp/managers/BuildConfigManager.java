package org.watsi.uhp.managers;

import org.watsi.uhp.BuildConfig;

public class BuildConfigManager {
    public static boolean isTrainingFlavour() {
        return BuildConfig.FLAVOR != null && BuildConfig.FLAVOR.equals("training");
    }
}
