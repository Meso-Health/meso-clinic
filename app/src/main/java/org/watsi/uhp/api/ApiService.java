package org.watsi.uhp.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NotificationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.User;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private static UhpApi instance;

    public static synchronized UhpApi requestBuilder(Context context) throws IllegalStateException {
        if (instance == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addNetworkInterceptor(new UnauthorizedInterceptor());
            httpClient.addNetworkInterceptor(new TokenInterceptor(context));
            httpClient.retryOnConnectionFailure(false);
            String apiHost = BuildConfig.API_HOST;
            if (apiHost == null) {
                throw new IllegalStateException("API hostname not configured");
            }
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .setDateFormat(Clock.ISO_DATE_FORMAT)
                    .registerTypeAdapterFactory(new EncounterTypeAdapterFactory(Encounter.class))
                    .create();
            Retrofit builder = new Retrofit.Builder()
                    .baseUrl(apiHost)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
            instance = builder.create(UhpApi.class);
        }
        return instance;
    }

    public static retrofit2.Response login(String username, String password, Context context) {
        UhpApi api = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UhpApi.class);
        Call<AuthenticationToken> request = api.getAuthToken(Credentials.basic(username, password));
        try {
            retrofit2.Response<AuthenticationToken> response = request.execute();
            if (response.isSuccessful()) {
                Log.d("UHP", "got auth token");
                String token = response.body().getToken();
                ConfigManager.setLoggedInUserToken(token, context);
                User user = response.body().getUser();
                Rollbar.setPersonData(String.valueOf(user.getId()), user.getUsername(), null);
            } else {
                NotificationManager.requestFailure(
                        "Login failure",
                        request.request(),
                        response.raw()
                );
            }
            return response;
        } catch (IOException | IllegalStateException e) {
            Rollbar.reportException(e);
        }
        return null;
    }
}
