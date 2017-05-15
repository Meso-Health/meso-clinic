package org.watsi.uhp.models;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;

import java.util.Map;
import java.util.UUID;

import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, IdentificationEvent.class })
public class IdentificationEventTest {

    @Mock
    Context mockContext;
    @Mock
    UhpApi mockApi;
    @Mock
    Call<IdentificationEvent> mockCall;
    @Mock
    Map<String, RequestBody> mockPatchRequestBody;

    private IdentificationEvent identificationEvent;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);
        identificationEvent = new IdentificationEvent();
    }

    @Test
    public void getFormattedClinicNumber() throws Exception {
        identificationEvent.setClinicNumber(50);
        identificationEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.OPD);
        assertEquals(identificationEvent.getFormattedClinicNumber(), "50");

        identificationEvent.setClinicNumber(50);
        identificationEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.DELIVERY);
        assertEquals(identificationEvent.getFormattedClinicNumber(), "D50");
    }

    @Test
    public void getFormattedDismissalReasons() throws Exception {
        String[] formattedDismissalReasons = IdentificationEvent.getFormattedDismissalReasons();

        assertEquals(formattedDismissalReasons.length, 3);
        assertEquals(formattedDismissalReasons[0], "Accidental identification");
        assertEquals(formattedDismissalReasons[1], "Patient left before care");
        assertEquals(formattedDismissalReasons[2], "Patient left after care");
    }

    @Test
    public void setDismissalReason() throws Exception {
        IdentificationEvent.DismissalReasonEnum reason =
                IdentificationEvent.DismissalReasonEnum.ACCIDENTAL_IDENTIFICATION;

        identificationEvent.setDismissalReason(reason);

        assertEquals(identificationEvent.getDismissalReason(), reason);
        assertTrue(identificationEvent.getDismissed());
    }

    @Test
    public void formatPatchRequest() throws Exception {
        IdentificationEvent idEvent = new IdentificationEvent();
        idEvent.setDismissed(true);
        idEvent.setDismissalReason(
                IdentificationEvent.DismissalReasonEnum.ACCIDENTAL_IDENTIFICATION);

        Map<String, RequestBody> requestBodyMap = idEvent.formatPatchRequest();

        assertTrue(requestBodyMap.containsKey(IdentificationEvent.FIELD_NAME_DISMISSED));
        assertTrue(requestBodyMap.containsKey(IdentificationEvent.FIELD_NAME_DISMISSAL_REASON));
        Buffer buffer = new Buffer();
        RequestBody dismissedRequestBody =
                requestBodyMap.get(IdentificationEvent.FIELD_NAME_DISMISSED);
        dismissedRequestBody.writeTo(buffer);
        assertEquals(buffer.readUtf8(), "true");
        buffer.clear();
        RequestBody dismissalReasonRequestBody =
                requestBodyMap.get(IdentificationEvent.FIELD_NAME_DISMISSAL_REASON);
        dismissalReasonRequestBody.writeTo(buffer);
        assertEquals(buffer.readUtf8(), "accidental_identification");
    }

    @Test
    public void postApiCall_hasThroughMember_setsThroughMemberId() throws Exception {
        Member throughMember = new Member();
        throughMember.setId(UUID.randomUUID());
        identificationEvent.setThroughMember(throughMember);

        when(mockApi.postIdentificationEvent(anyString(), anyInt(), any(IdentificationEvent.class)))
                .thenReturn(mockCall);

        identificationEvent.postApiCall(mockContext);

        assertEquals(identificationEvent.getThroughMemberId(), throughMember.getId());
    }

    @Test
    public void postApiCall_createsRequestWithProperArguments() throws Exception {
        identificationEvent.setToken("foo");

        when(mockApi.postIdentificationEvent(anyString(), anyInt(), any(IdentificationEvent.class)))
                .thenReturn(mockCall);

        Call call = identificationEvent.postApiCall(mockContext);

        assertEquals(call, mockCall);
        verify(mockApi, times(1)).postIdentificationEvent(
                "Token foo", BuildConfig.PROVIDER_ID, identificationEvent);
    }

    @Test
    public void patchApiCall() throws Exception {
        identificationEvent.setId(UUID.randomUUID());
        identificationEvent.setToken("foo");
        IdentificationEvent identificationEventSpy = spy(identificationEvent);

        when(mockApi.patchIdentificationEvent(
                anyString(), any(UUID.class), anyMapOf(String.class, RequestBody.class)))
                .thenReturn(mockCall);
        doReturn(mockPatchRequestBody).when(identificationEventSpy).formatPatchRequest();

        Call call = identificationEventSpy.patchApiCall(mockContext);

        assertEquals(call, mockCall);
        verify(mockApi, times(1)).patchIdentificationEvent(
                "Token foo", identificationEventSpy.getId(), mockPatchRequestBody);
    }
}
