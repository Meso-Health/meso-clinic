package org.watsi.device.api

import io.reactivex.Single
import okhttp3.RequestBody
import org.watsi.device.api.models.AuthenticationTokenApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.api.models.DiagnosisApi
import org.watsi.device.api.models.MemberApi
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Authorization"

interface CoverageApi {

    @POST("authentication_token")
    fun getAuthToken(@Header(AUTHORIZATION_HEADER) authorization: String): Single<AuthenticationTokenApi>

    @GET("providers/{providerId}/members")
    fun members(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Call<List<MemberApi>>

    @GET("providers/{providerId}/billables")
    fun billables(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Call<List<BillableApi>>

    @GET("diagnoses")
    fun diagnoses(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String
    ): Call<List<DiagnosisApi>>

    @POST("providers/{providerId}/identification_events")
    fun postIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body unsyncedEvent: IdentificationEvent
    ): Call<IdentificationEvent>

    @Multipart
    @PATCH("identification_events/{identificationEventId}")
    fun patchIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("identificationEventId") identificationEventId: UUID,
            @PartMap params: Map<String, RequestBody>
    ): Call<IdentificationEvent>

    @POST("providers/{providerId}/encounters")
    fun syncEncounter(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body unsyncedEncounter: Encounter
    ): Call<Encounter>

    @Multipart
    @PATCH("encounters/{encounterId}")
    fun syncEncounterForm(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("encounterId") encounterId: UUID,
            @Part("forms[]") encounterForm: RequestBody
    ): Call<EncounterForm>

    @Multipart
    @PATCH("members/{memberId}")
    fun syncMember(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("memberId") memberId: UUID,
            @PartMap params: Map<String, RequestBody>
    ): Call<Member>

    @Multipart
    @POST("members")
    fun enrollMember(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @PartMap params: Map<String, RequestBody>
    ): Call<Member>
}
