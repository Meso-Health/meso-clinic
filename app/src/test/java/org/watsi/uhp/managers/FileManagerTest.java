package org.watsi.uhp.managers;

import android.content.Context;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExceptionManager.class, File.class, FileManager.class, Uri.class})
public class FileManagerTest {
    private String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";

    @Mock
    Context mockContext;
    @Mock
    File mockFile;
    @Mock
    Uri mockUri;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ExceptionManager.class);
        mockStatic(Uri.class);
    }

    @Test
    public void isLocal_localUrl_returnsTrue() throws Exception {
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("content");

        boolean result = FileManager.isLocal(localPhotoUrl);

        assertTrue(result);
    }

    @Test
    public void isLocal_remoteUrl_returnsFalse() throws Exception {
        String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";

        when(Uri.parse(remotePhotoUrl)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("https");

        boolean result = FileManager.isLocal(remotePhotoUrl);

        assertFalse(result);
    }

    @Test
    public void deleteLocalPhoto_localFileExistsDeleteReturnsTrue_doesNotThrowException() throws Exception {
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.delete()).thenReturn(true);

        FileManager.deleteLocalPhoto(localPhotoUrl);

        verify(mockFile, times(1)).delete();
    }

    @Test(expected=FileManager.FileDeletionException.class)
    public void deleteLocalPhoto_localFileDoesNotExist_throwExceptions() throws Exception {
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);
        when(mockFile.exists()).thenReturn(false);

        FileManager.deleteLocalPhoto(localPhotoUrl);

        verify(mockFile, never()).delete();
    }

    @Test(expected=FileManager.FileDeletionException.class)
    public void deleteLocalPhoto_localFileDeleteReturnsFalse_throwsException() throws Exception {
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.delete()).thenReturn(false);

        FileManager.deleteLocalPhoto(localPhotoUrl);
    }

    @Test(expected=FileManager.FileDeletionException.class)
    public void deleteLocalPhoto_remoteFileUrl_throwsException() throws Exception {
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(false);

        FileManager.deleteLocalPhoto(localPhotoUrl);
    }
}
