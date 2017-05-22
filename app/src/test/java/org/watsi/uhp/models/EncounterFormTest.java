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
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, EncounterForm.class, FileManager.class, MediaType.class,
        RequestBody.class, Uri.class })
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
    @Mock
    Encounter mockEncounter;

    private EncounterForm encounterForm;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        mockStatic(FileManager.class);
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
        EncounterForm encounterFormResponse = new EncounterForm();
        String url = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
        encounterForm.setUrl(url);

        encounterForm.handleUpdateFromSync(encounterFormResponse);

        verifyStatic();
        FileManager.deleteLocalPhoto(encounterForm.getUrl());
    }

    @Test
    public void handleUpdateFromSync_setsEncounterFormDetailsOnResponse() throws Exception {
        EncounterForm encounterFormResponse = new EncounterForm();
        String url = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
        UUID formId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        encounterForm.setId(formId);
        encounterForm.setEncounterId(encounterId);
        encounterForm.setUrl(url);

        encounterForm.handleUpdateFromSync(encounterFormResponse);

        assertEquals(encounterFormResponse.getId(), formId);
        assertEquals(encounterFormResponse.getEncounterId(), encounterId);
        assertEquals(encounterFormResponse.getUrl(), url);
    }

    @Test
    public void postApiCall() throws Exception {
        UUID encounterId = UUID.randomUUID();
        encounterForm.setToken("foo");
        encounterForm.setEncounter(mockEncounter);
        EncounterForm encounterFormSpy = spy(encounterForm);
        byte[] photoBytes = new byte[]{(byte)0xe0};

        doReturn(photoBytes).when(encounterFormSpy).getImage(mockContext);
        when(MediaType.parse("image/jpg")).thenReturn(mockMediaType);
        when(mockEncounter.getId()).thenReturn(encounterId);
        when(RequestBody.create(mockMediaType, photoBytes)).thenReturn(mockRequestBody);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);

        encounterFormSpy.postApiCall(mockContext);

        verify(mockApi, times(1)).syncEncounterForm("Token foo", encounterId, mockRequestBody);
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
