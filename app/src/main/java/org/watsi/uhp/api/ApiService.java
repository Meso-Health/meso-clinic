package org.watsi.uhp.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.managers.ConfigManager;

import java.io.IOException;

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
            httpClient.authenticator(new TokenAuthenticator(context));
            String apiHost = ConfigManager.getApiHost(context);
            if (apiHost == null) {
                throw new IllegalStateException("API hostname not configured");
            }
            Retrofit builder = new Retrofit.Builder()
                    .baseUrl(apiHost)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
            instance = builder.create(UhpApi.class);
        }
        return instance;
    }

    public static retrofit2.Response login(String username, String password, Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.authenticator(new BasicAuthenticator(username, password));
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(ConfigManager.getApiHost(context))
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        UhpApi api = builder.create(UhpApi.class);
        Call<AuthenticationToken> request = api.getAuthToken();
        try {
            retrofit2.Response<AuthenticationToken> response = request.execute();
            if (response.isSuccessful()) {
                Log.d("UHP", "got auth token");
                String token = response.body().getToken();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(TokenAuthenticator.TOKEN_PREFERENCES_KEY, token);
                editor.apply();
            }
            return response;
        } catch (IOException | IllegalStateException e) {
            // TODO: starts a loop if it gets here
            Rollbar.reportException(e);
        }
        return null;
    }
}
