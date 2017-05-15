package org.watsi.uhp.models;

import android.content.Context;
import android.net.Uri;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.managers.FileManager;

import java.io.File;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, EncounterForm.class, File.class, FileManager.class,
        MediaType.class, RequestBody.class, Uri.class })
public class EncounterFormTest {

    @Mock
    Context mockContext;
    @Mock
    UhpApi mockApi;
    @Mock
    MediaType mockMediaType;
    @Mock
    RequestBody mockRequestBody;
    @Mock
    Dao mockDao;

    private EncounterForm encounterForm;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        mockStatic(MediaType.class);
        mockStatic(RequestBody.class);
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

    @Test
    public void handleUpdateFromSync_deletesLocalFile() throws Exception {
        String url = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
        encounterForm.setUrl(url);
        File mockFile = mock(File.class);

        whenNew(File.class).withArguments(url).thenReturn(mockFile);

        encounterForm.handleUpdateFromSync(null);

        verify(mockFile, times(1)).delete();
    }

    @Test
    public void postApiCall() throws Exception {
        UUID id = UUID.randomUUID();
        encounterForm.setId(id);
        encounterForm.setToken("foo");
        EncounterForm encounterFormSpy = spy(encounterForm);
        byte[] photoBytes = new byte[]{(byte)0xe0};

        doReturn(photoBytes).when(encounterFormSpy).getImage(mockContext);
        when(MediaType.parse("image/jpg")).thenReturn(mockMediaType);
        when(RequestBody.create(mockMediaType, photoBytes)).thenReturn(mockRequestBody);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);

        encounterFormSpy.postApiCall(mockContext);

        verify(mockApi, times(1)).syncEncounterForm("Token foo", id, mockRequestBody);
    }

    @Test
    public void destroy() throws Exception {
        UUID id = UUID.randomUUID();
        encounterForm.setId(id);
        EncounterForm encounterFormSpy = spy(encounterForm);

        doReturn(mockDao).when(encounterFormSpy).getDao();

        encounterFormSpy.destroy();

        verify(mockDao, times(1)).deleteById(id);
    }
}
