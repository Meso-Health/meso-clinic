package org.watsi.uhp.api;

import android.accounts.AccountManager;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AuthenticationToken;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static UhpApi instance;

    public static long HTTP_TIMEOUT_IN_SECONDS = 30L;

    public static synchronized UhpApi requestBuilder(Context context) throws IllegalStateException {
        if (instance == null) {
            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(context.getCacheDir(), cacheSize);

            AccountManager accountManager = AccountManager.get(context);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .connectTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .addInterceptor(new NotModifiedInterceptor())
                    .addNetworkInterceptor(new UnauthorizedInterceptor(accountManager))
                    .retryOnConnectionFailure(false);
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat(Clock.ISO_DATE_FORMAT_STRING)
                    .registerTypeAdapterFactory(new EncounterTypeAdapterFactory())
                    .registerTypeAdapterFactory(new DiagnosisTypeAdapterFactory())
                    .create();
            Retrofit builder = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
            instance = builder.create(UhpApi.class);
        }
        return instance;
    }

    public static Response<AuthenticationToken> authenticate(String username, String password) {
        UhpApi api = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UhpApi.class);
        Call<AuthenticationToken> request = api.getAuthToken(Credentials.basic(username, password));
        try {
            return request.execute();
        } catch (IOException e) {
            ExceptionManager.reportException(e);
            return null;
        }
    }
}
