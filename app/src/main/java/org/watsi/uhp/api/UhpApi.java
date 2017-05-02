package org.watsi.uhp.api;

import org.watsi.uhp.models.AuthenticationToken;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

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

    String AUTHORIZATION_HEADER = "Authorization";
    String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

    @POST("authentication_token")
    Call<AuthenticationToken> getAuthToken(@Header(AUTHORIZATION_HEADER) String authorization);

    @GET("providers/{providerId}/members")
    Call<List<Member>> members(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Header(IF_MODIFIED_SINCE_HEADER) String lastModified,
            @Path("providerId") int providerId
    );

    @GET("providers/{providerId}/billables")
    Call<List<Billable>> billables(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Header(IF_MODIFIED_SINCE_HEADER) String lastModified,
            @Path("providerId") int providerId
    );

    @POST("providers/{providerId}/identification_events")
    Call<IdentificationEvent> postIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body IdentificationEvent unsyncedEvent
    );

    @Multipart
    @PATCH("identification_events/{identificationEventId}")
    Call<IdentificationEvent> patchIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Path("identificationEventId") UUID identificationEventId,
            @PartMap Map<String, RequestBody> params
    );

    @POST("providers/{providerId}/encounters")
    Call<Encounter> syncEncounter(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Path("providerId") int providerId,
            @Body Encounter unsyncedEncounter
    );

    @Multipart
    @PATCH("encounters/{encounterId}")
    Call<Encounter> syncEncounterForm(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Path("encounterId") UUID encounterId,
            @Part("forms[]") RequestBody encounterForm
    );

    @Multipart
    @PATCH("members/{memberId}")
    Call<Member> syncMember(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @Path("memberId") UUID memberId,
            @PartMap Map<String, RequestBody> params
    );

    @Multipart
    @POST("members")
    Call<Member> enrollMember(
            @Header(AUTHORIZATION_HEADER) String tokenAuthorization,
            @PartMap Map<String, RequestBody> params
    );
}
