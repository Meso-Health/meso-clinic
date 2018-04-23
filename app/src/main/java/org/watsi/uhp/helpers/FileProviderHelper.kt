package org.watsi.uhp.helpers

import android.content.Context
import android.net.Uri
import android.support.v4.content.FileProvider
import org.watsi.uhp.BuildConfig
import java.io.File

object FileProviderHelper {

    fun getUriFromProvider(filename: String, context: Context): Uri {
        val fileProvider = BuildConfig.APPLICATION_ID + ".fileprovider"
        val dir = File(context.filesDir, "images/")
        if (!dir.exists()) dir.mkdirs()
        val image = File(dir, filename)
        return FileProvider.getUriForFile(context, fileProvider, image)
    }
}
