package org.watsi.uhp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PreferenceManager.class, Request.class})
public class TokenInterceptorTest {

    private TokenInterceptor tokenInterceptor;

    @Mock
    Context mockContext;

    @Mock
    SharedPreferences mockSharedPreferences;

    @Mock
    Interceptor.Chain mockChain;

    @Mock
    Request.Builder mockBuilder;

    @Mock
    Request mockRequest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(PreferenceManager.class);
        tokenInterceptor = new TokenInterceptor(mockContext);
        when(PreferenceManager.getDefaultSharedPreferences(mockContext))
                .thenReturn(mockSharedPreferences);
    }

    @Test
    public void getRequest_storedTokenIsNull_addsEmptyTokenAuthHeader() throws Exception {
        when(mockSharedPreferences.getString(TokenInterceptor.TOKEN_PREFERENCES_KEY, null))
                .thenReturn(null);
        when(mockChain.request()).thenReturn(mockRequest);
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.newBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRequest);
        when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);

        tokenInterceptor.intercept(mockChain);

        verify(mockBuilder).header("Authorization", "Token null");
        verify(mockChain).proceed(mockRequest);
    }



    @Test
    public void getRequest_storedTokenIsNotNull() throws Exception {
        String token = "32do8j3dkndsl8i3fin238fwhefaewf8e";

        when(mockSharedPreferences.getString(TokenInterceptor.TOKEN_PREFERENCES_KEY, null))
                .thenReturn(token);
        when(mockChain.request()).thenReturn(mockRequest);
        when(mockRequest.method()).thenReturn("GET");
        when(mockRequest.newBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockRequest);
        when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);

        tokenInterceptor.intercept(mockChain);

        verify(mockBuilder).header("Authorization", "Token " + token);
        verify(mockChain).proceed(mockRequest);
    }

    @Test
    public void nonGetRequest_passesOriginalRequest() throws Exception {
        RequestBody body = mock(RequestBody.class);
        Request request = new Request.Builder().method("POST", body).url("http://test.org").build();
        when(mockChain.request()).thenReturn(request);
        tokenInterceptor.intercept(mockChain);
        verify(mockChain).proceed(request);
    }
}
