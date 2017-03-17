package org.watsi.uhp.api;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
    Call<IdentificationEvent> postIdentificationEvent(
            @Header("Authorization") String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body IdentificationEvent unsyncedEvent
    );

    @Multipart
    @PATCH("identification_events/{identificationEventId}")
    Call<IdentificationEvent> patchIdentificationEvent(
            @Header("Authorization") String tokenAuthorization,
            @Path("identificationEventId") UUID identificationEventId,
            @PartMap Map<String, RequestBody> params
    );

    @POST("providers/{providerId}/encounters")
    Call<Encounter> syncEncounter(
            @Header("Authorization") String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body Encounter unsyncedEncounter
    );

    @Multipart
    @PATCH("encounters/{encounterId}")
    Call<Encounter> syncEncounterForm(
            @Header("Authorization") String tokenAuthorization,
            @Path("encounterId") String encounterId,
            @Part("forms[]") RequestBody encounterForm
    );

    @Multipart
    @PATCH("members/{memberId}")
    Call<Member> syncMember(
            @Header("Authorization") String tokenAuthorization,
            @Path("memberId") String memberId,
            @PartMap Map<String, RequestBody> params
    );

    @Multipart
    @POST("members")
    Call<Member> enrollMember(
            @Header("Authorization") String tokenAuthorization,
            @PartMap Map<String, RequestBody> params
    );
}
