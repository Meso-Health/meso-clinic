package org.watsi.uhp.managers;

import android.content.Context;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({File.class, Uri.class})
public class FileManagerTest {

    @Mock
    private Context mockContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isLocal() throws Exception {
        Uri mockUri = mock(Uri.class);
        mockStatic(Uri.class);

        String url = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";
        when(Uri.parse(url)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("https");

        assertFalse(FileManager.isLocal(url));

        String fileUri = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
        when(Uri.parse(fileUri)).thenReturn(mockUri);
        when(mockUri.getScheme()).thenReturn("content");
        assertTrue(FileManager.isLocal(fileUri));
    }}
