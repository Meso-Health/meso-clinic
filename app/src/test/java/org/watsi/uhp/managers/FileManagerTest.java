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
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ExceptionManager.class, File.class, FileManager.class, Uri.class})
public class FileManagerTest {
    private final String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";

    @Mock
    private Context mockContext;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ExceptionManager.class);
    }

    @Test
    public void isLocal_localUrl_returnsTrue() throws Exception {
        Uri mockUri = mock(Uri.class);
        mockStatic(Uri.class);

        String url = localPhotoUrl;
        when(Uri.parse(url)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("content");

        assertTrue(FileManager.isLocal(url));
    }

    @Test
    public void isLocal_remoteUrl_returnsFalse() throws Exception {
        Uri mockUri = mock(Uri.class);
        mockStatic(Uri.class);

        String url = remotePhotoUrl;
        when(Uri.parse(url)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("https");

        assertFalse(FileManager.isLocal(url));
    }

    @Test
    public void deleteLocalPhoto_localFileUrlDeleteReturnsTrue_succeeds() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.delete()).thenReturn(true);
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);

        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);

        FileManager.deleteLocalPhoto(localPhotoUrl);

        verifyStatic(never());
        ExceptionManager.reportMessage(anyString(), anyString(), anyMap());
    }

    @Test
    public void deleteLocalPhoto_localFileDeleteReturnsFalse_fails() throws Exception {
        File mockFile = mock(File.class);
        when(mockFile.delete()).thenReturn(false);
        whenNew(File.class).withArguments(localPhotoUrl).thenReturn(mockFile);

        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);

        FileManager.deleteLocalPhoto(localPhotoUrl);

        verifyStatic();
        ExceptionManager.reportMessage(anyString(), anyString(), anyMap());
    }

    @Test(expected=FileManager.FileDeletionException.class)
    public void deleteLocalPhoto_remoteFileUrl_fails() throws Exception {
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(false);

        FileManager.deleteLocalPhoto(localPhotoUrl);
    }
}
