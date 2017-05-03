package org.watsi.uhp.managers;

import android.util.Log;

import com.rollbar.android.Rollbar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpUrl.class, Log.class, MediaType.class, Request.class, Response.class,
        Rollbar.class })
public class ExceptionManagerTest {

    @Mock
    Request mockRequest;
    @Mock
    Response mockResponse;
    @Mock
    Map<String, String> mockParamsMap;
    @Mock
    HttpUrl mockUrl;
    @Mock
    RequestBody mockRequestBody;
    @Mock
    MediaType mockMediaType;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Log.class);
        mockStatic(Rollbar.class);
    }

    @Test
    public void requestFailure() throws Exception {
        String description = "foo";
        String url = "http://uhp.org";
        String method = "GET";
        String contentType = "text/plain";
        long contentLength = 42L;
        String requestId = "bar";
        int responseCode = 500;
        String responseMessage = "oops";

        when(mockRequest.url()).thenReturn(mockUrl);
        when(mockUrl.toString()).thenReturn(url);
        when(mockRequest.method()).thenReturn(method);
        when(mockRequest.body()).thenReturn(mockRequestBody);
        when(mockRequestBody.contentType()).thenReturn(mockMediaType);
        when(mockMediaType.toString()).thenReturn(contentType);
        when(mockRequestBody.contentLength()).thenReturn(contentLength);
        when(mockResponse.code()).thenReturn(responseCode);
        when(mockResponse.message()).thenReturn(responseMessage);
        when(mockResponse.header("X-Request-Id")).thenReturn(requestId);
        when(Rollbar.isInit()).thenReturn(true);

        ExceptionManager.requestFailure(description, mockRequest, mockResponse, mockParamsMap);

        verifyStatic();
        Rollbar.reportMessage(description, "warning", mockParamsMap);
        verify(mockParamsMap, times(1)).put("Url", url);
        verify(mockParamsMap, times(1)).put("Method", method);
        verify(mockParamsMap, times(1)).put("Content-Type", contentType);
        verify(mockParamsMap, times(1)).put("Content-Length", String.valueOf(contentLength));
        verify(mockParamsMap, times(1)).put("X-Request-Id", requestId);
        verify(mockParamsMap, times(1)).put("response.code", String.valueOf(responseCode));
        verify(mockParamsMap, times(1)).put("response.message", responseMessage);
    }
}
