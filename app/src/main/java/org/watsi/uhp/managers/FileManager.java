package org.watsi.uhp.managers;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.google.common.io.ByteStreams;

import org.watsi.uhp.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class for dealing with files stored locally
 */
public class FileManager {

    private static String CAPTURE_IMAGE_FILE_PROVIDER = BuildConfig.APPLICATION_ID + ".fileprovider";

    public static Uri getUriFromProvider(String filename, String path, Context context) throws IOException {
        File dir = new File(context.getFilesDir(), "images/" + path);
        if (!dir.exists()) dir.mkdirs();
        File image = new File(dir, filename);
        return FileProvider.getUriForFile(context, CAPTURE_IMAGE_FILE_PROVIDER, image);
    }

    public static byte[] readFromUri(Uri uri, Context context) {
        InputStream iStream = null;
        ByteArrayOutputStream byteStream = null;
        try {
            iStream = context.getContentResolver().openInputStream(uri);
            byteStream = new ByteArrayOutputStream();
            ByteStreams.copy(iStream, byteStream);
            return byteStream.toByteArray();
        } catch (IOException e) {
            ExceptionManager.reportException(e);
        } finally {
            try {
                if (iStream != null) iStream.close();
                if (byteStream != null) byteStream.close();
            } catch (IOException e1) {
                ExceptionManager.reportException(e1);
            }
        }
        return null;
    }

    public static boolean isLocal(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        return scheme != null && scheme.equals("content");
    }

    public static void deleteLocalPhoto(String url) throws FileDeletionException {
        if (isLocal(url)) {
            File file = new File(url);
            if (file.exists() && file.delete()) return;
        }
        throw new FileDeletionException("Failed to delete photo with url: " + url);
    }

    public static class FileDeletionException extends Exception {
        public FileDeletionException(String reason) {
            super(reason);
        }
    }
}
