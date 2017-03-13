package org.watsi.uhp.api;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface UhpApi {

    @POST("authentication_token")
    Call<AuthenticationToken> getAuthToken(@Header("Authorization") String authorization);

    @GET("providers/{providerId}/members")
    Call<List<Member>> members(
            @Header("If-Modified-Since") String lastModified,
            @Path("providerId") int providerId
    );

    @GET("providers/{providerId}/billables")
    Call<List<Billable>> billables(
            @Header("If-Modified-Since") String lastModified,
            @Path("providerId") int providerId
    );

    @POST("providers/{providerId}/identification_events")
    Call<IdentificationEvent> syncIdentificationEvent(
            @Header("Authorization") String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body IdentificationEvent unsyncedEvent
    );

    @POST("providers/{providerId}/encounters")
    Call<Encounter> syncEncounter(
            @Header("Authorization") String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body Encounter unsyncedEncounter
    );

    @Multipart
    @PATCH("members/{memberId}")
    Call<Member> syncMember(
            @Header("Authorization") String tokenAuthorization,
            @Path("memberId") String memberId,
            @PartMap Map<String, RequestBody> params
    );

    @Multipart
    @POST("members/{memberId}")
    Call<Member> enrollMember(
            @Header("Authorization") String tokenAuthorization,
            @Path("memberId") String memberId,
            @PartMap Map<String, RequestBody> params
    );
}
