package org.watsi.uhp.api;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.models.AuthenticationToken;

import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okreplay.OkReplayInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccountManager.class, ApiService.class, Credentials.class,
        EncounterTypeAdapterFactory.class, DiagnosisTypeAdapterFactory.class, GsonBuilder.class,
        GsonConverterFactory.class, OkHttpClient.Builder.class, OkReplayInterceptor.class,
        Response.class, Retrofit.class, Retrofit.Builder.class })
public class ApiServiceTest {

    @Mock Context mockContext;
    @Mock AccountManager mockAccountManager;
    @Mock OkHttpClient.Builder mockHttpClientBuilder;
    @Mock OkHttpClient mockHttpClient;
    @Mock UnauthorizedInterceptor mockUnauthorizedInterceptor;
    @Mock OkReplayInterceptor mockOkReplayInterceptor;
    @Mock NotModifiedInterceptor mockNotModifiedInterceptor;
    @Mock GsonBuilder mockGsonBuilder;
    @Mock EncounterTypeAdapterFactory mockEncounterTypeAdapterFactory;
    @Mock DiagnosisTypeAdapterFactory mockDiagnosisTypeAdapterFactory;
    @Mock GsonConverterFactory mockGsonConverterFactory;
    @Mock Retrofit.Builder mockRetrofitBuilder;
    @Mock Retrofit mockRetrofit;
    @Mock UhpApi mockApi;
    @Mock Call<AuthenticationToken> mockAuthRequest;
    @Mock Response<AuthenticationToken> mockAuthResponse;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(AccountManager.class);
        mockStatic(Credentials.class);
        mockStatic(GsonConverterFactory.class);
    }

    @Test
    public void requestBuilder() throws Exception {
        Gson gson = new Gson();

        when(AccountManager.get(mockContext)).thenReturn(mockAccountManager);
        whenNew(OkHttpClient.Builder.class).withNoArguments().thenReturn(mockHttpClientBuilder);
        whenNew(NotModifiedInterceptor.class).withNoArguments().thenReturn(mockNotModifiedInterceptor);
        whenNew(UnauthorizedInterceptor.class).withArguments(mockAccountManager)
                .thenReturn(mockUnauthorizedInterceptor);
        whenNew(OkReplayInterceptor.class).withNoArguments().thenReturn(mockOkReplayInterceptor);
        when(mockHttpClientBuilder.cache(any(Cache.class)))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.connectTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.readTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.writeTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.addNetworkInterceptor(mockUnauthorizedInterceptor))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.addInterceptor(mockOkReplayInterceptor))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.addInterceptor(mockNotModifiedInterceptor))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.retryOnConnectionFailure(false))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);
        whenNew(GsonBuilder.class).withNoArguments().thenReturn(mockGsonBuilder);
        whenNew(EncounterTypeAdapterFactory.class).withNoArguments()
                .thenReturn(mockEncounterTypeAdapterFactory);
        whenNew(DiagnosisTypeAdapterFactory.class).withNoArguments()
                .thenReturn(mockDiagnosisTypeAdapterFactory);
        when(mockGsonBuilder.excludeFieldsWithoutExposeAnnotation()).thenReturn(mockGsonBuilder);
        when(mockGsonBuilder.setDateFormat(Clock.ISO_DATE_FORMAT_STRING))
                .thenReturn(mockGsonBuilder);
        when(mockGsonBuilder.registerTypeAdapterFactory(mockEncounterTypeAdapterFactory))
                .thenReturn(mockGsonBuilder);
        when(mockGsonBuilder.registerTypeAdapterFactory(mockDiagnosisTypeAdapterFactory))
                .thenReturn(mockGsonBuilder);
        when(mockGsonBuilder.create()).thenReturn(gson);
        whenNew(Retrofit.Builder.class).withNoArguments().thenReturn(mockRetrofitBuilder);
        when(GsonConverterFactory.create(gson)).thenReturn(mockGsonConverterFactory);
        when(mockRetrofitBuilder.baseUrl(BuildConfig.API_HOST)).thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.addConverterFactory(mockGsonConverterFactory))
                .thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.client(mockHttpClient)).thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.build()).thenReturn(mockRetrofit);
        when(mockRetrofit.create(UhpApi.class)).thenReturn(mockApi);

        UhpApi result = ApiService.requestBuilder(mockContext);

        assertEquals(result, mockApi);
    }

    @Test
    public void authenticate() throws Exception {
        String username = "username";
        String password = "password";
        String credentials = "credentials";

        whenNew(Retrofit.Builder.class).withNoArguments().thenReturn(mockRetrofitBuilder);
        when(GsonConverterFactory.create()).thenReturn(mockGsonConverterFactory);
        when(mockRetrofitBuilder.baseUrl(BuildConfig.API_HOST)).thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.addConverterFactory(mockGsonConverterFactory))
                .thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.client(mockHttpClient)).thenReturn(mockRetrofitBuilder);
        when(mockRetrofitBuilder.build()).thenReturn(mockRetrofit);
        when(mockRetrofit.create(UhpApi.class)).thenReturn(mockApi);
        when(Credentials.basic(username, password)).thenReturn(credentials);
        when(mockApi.getAuthToken(credentials)).thenReturn(mockAuthRequest);
        when(mockAuthRequest.execute()).thenReturn(mockAuthResponse);

        Response<AuthenticationToken> response = ApiService.authenticate(username, password);

        assertEquals(response, mockAuthResponse);
    }
}
