package org.watsi.uhp.api;

import org.watsi.uhp.models.Member;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UhpApi {

    @GET("facilities/{facilityId}/members")
    Call<List<Member>> members(@Header("If-Modified-Since") String lastModified, @Path("facilityId") int facilityId);

    @POST("authentication_token")
    Call<AuthenticationToken> getAuthToken();
}
