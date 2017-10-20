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

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okreplay.OkReplayInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static UhpApi instance;
    public static OkReplayInterceptor replayInterceptor;

    public static long HTTP_TIMEOUT_IN_SECONDS = 30L;

    public static synchronized UhpApi requestBuilder(Context context) throws IllegalStateException {
        if (instance == null) {
            AccountManager accountManager = AccountManager.get(context);
            replayInterceptor = new OkReplayInterceptor();
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .addNetworkInterceptor(new UnauthorizedInterceptor(accountManager))
                    .addInterceptor(replayInterceptor)
                    .retryOnConnectionFailure(false);
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat(Clock.ISO_DATE_FORMAT_STRING)
                    .registerTypeAdapterFactory(new EncounterTypeAdapterFactory())
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
