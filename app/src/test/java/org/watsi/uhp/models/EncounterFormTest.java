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

import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, EncounterForm.class, MediaType.class, RequestBody.class, Uri.class })
public class EncounterFormTest {

    @Mock
    Context mockContext;
    @Mock
    Photo mockPhoto;
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
        mockStatic(MediaType.class);
        mockStatic(RequestBody.class);
        encounterForm = new EncounterForm();
        encounterForm.setPhoto(mockPhoto);
    }

    @Test
    public void handleUpdateFromSync_marksPhotoAsSynced() throws Exception {
        doNothing().when(mockPhoto).markAsSynced();
        EncounterForm encounterFormResponse = new EncounterForm();

        encounterForm.handleUpdateFromSync(encounterFormResponse);

        verify(mockPhoto, times(1)).getSynced();
    }

    @Test
    public void handleUpdateFromSync_setsEncounterFormDetailsOnResponse() throws Exception {
        doNothing().when(mockPhoto).markAsSynced();
        EncounterForm encounterFormResponse = new EncounterForm();
        UUID formId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        encounterForm.setId(formId);
        encounterForm.setEncounterId(encounterId);

        encounterForm.handleUpdateFromSync(encounterFormResponse);

        assertEquals(encounterFormResponse.getId(), formId);
        assertEquals(encounterFormResponse.getEncounterId(), encounterId);
    }

    @Test
    public void postApiCall() throws Exception {
        UUID encounterId = UUID.randomUUID();
        encounterForm.setToken("foo");
        encounterForm.setEncounter(mockEncounter);
        EncounterForm encounterFormSpy = spy(encounterForm);
        byte[] photoBytes = new byte[]{(byte)0xe0};

        when(mockPhoto.bytes(mockContext)).thenReturn(photoBytes);
        when(MediaType.parse("image/jpg")).thenReturn(mockMediaType);
        when(mockEncounter.getId()).thenReturn(encounterId);
        when(RequestBody.create(mockMediaType, photoBytes)).thenReturn(mockRequestBody);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);

        encounterFormSpy.postApiCall(mockContext);

        verify(mockApi, times(1)).syncEncounterForm("Token foo", encounterId, mockRequestBody);
    }
}
