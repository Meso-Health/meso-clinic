package org.watsi.uhp.helpers

import android.view.View
import org.watsi.device.managers.SessionManager

object PermissionsHelper {

    fun getVisibilityFromPermission(permission: SessionManager.Permissions, sessionManager: SessionManager): Int {
        return if (sessionManager.userHasPermission(permission)) { View.VISIBLE } else { View.GONE }
    }

}
