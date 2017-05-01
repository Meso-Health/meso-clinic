package org.watsi.uhp.models;

import android.content.Context;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.managers.FileManager;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileManager.class, Uri.class })
public class EncounterFormTest {

    @Mock
    Context mockContext;

    private EncounterForm encounterForm;

    @Before
    public void setup() {
        encounterForm = new EncounterForm();
    }

    @Test
    public void getImage() throws Exception {
        byte[] image = new byte[]{(byte)0xe0};
        String url = "foo";
        encounterForm.setUrl(url);
        mockStatic(Uri.class);
        mockStatic(FileManager.class);
        Uri mockUri = mock(Uri.class);

        when(Uri.parse(url)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(image);

        byte[] result = encounterForm.getImage(mockContext);

        assertEquals(result, image);
    }
}
