package org.watsi.uhp.api;

import android.accounts.AccountManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.managers.Authenticator;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Request.class, Response.class})
public class UnauthorizedInterceptorTest {

    private UnauthorizedInterceptor unauthorizedInterceptor;

    @Mock
    AccountManager mockAccountManager;
    @Mock
    Interceptor.Chain mockChain;
    @Mock
    Request.Builder mockBuilder;
    @Mock
    Request mockRequest;
    @Mock
    Response mockResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        unauthorizedInterceptor = new UnauthorizedInterceptor(mockAccountManager);
    }

    @Test
    public void intercept_authorizedResponse_doesNotInvalidateAuthToken() throws Exception {
        when(mockChain.request()).thenReturn(mockRequest);
        when(mockChain.proceed(mockRequest)).thenReturn(mockResponse);
        when(mockResponse.code()).thenReturn(200);

        Response response = unauthorizedInterceptor.intercept(mockChain);

        assertEquals(response, mockResponse);
        verify(mockAccountManager, never()).invalidateAuthToken(anyString(), anyString());
    }

    @Test
    public void intercept_unauthorizedResponse_invalidatesAuthToken() throws Exception {
        String token = "token";

        when(mockChain.request()).thenReturn(mockRequest);
        when(mockChain.proceed(mockRequest)).thenReturn(mockResponse);
        when(mockResponse.code()).thenReturn(401);
        when(mockRequest.header(UhpApi.AUTHORIZATION_HEADER)).thenReturn(token);

        Response response = unauthorizedInterceptor.intercept(mockChain);

        assertEquals(response, mockResponse);
        verify(mockAccountManager, times(1)).invalidateAuthToken(Authenticator.ACCOUNT_TYPE, token);
    }
}
