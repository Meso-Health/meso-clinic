package org.watsi.uhp.api;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UhpApi {

    @POST("authentication_token")
    Call<AuthenticationToken> getAuthToken(@Header("Authorization") String authorization);

    @GET("facilities/{facilityId}/members")
    Call<List<Member>> members(
            @Header("If-Modified-Since") String lastModified,
            @Path("facilityId") int facilityId
    );

    @GET("facilities/{facilityId}/billables")
    Call<List<Billable>> billables(
            @Header("If-Modified-Since") String lastModified,
            @Path("facilityId") int facilityId
    );

    @POST("facilities/{facilityId}/identification_events")
    Call<IdentificationEvent> syncIdentificationEvent(
            @Header("Authorization") String tokenAuthorization,
            @Path("facilityId") int facilityId,
            @Body IdentificationEvent unsyncedEvent
    );
}
