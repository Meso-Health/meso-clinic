package org.watsi.device.api

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import org.watsi.device.api.models.AuthenticationTokenApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.api.models.DiagnosisApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.api.models.MemberApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Authorization"

interface CoverageApi {

    @POST("authentication_token")
    fun getAuthToken(
            @Header(AUTHORIZATION_HEADER) authorization: String
    ): Single<AuthenticationTokenApi>

    @GET("providers/{providerId}/members")
    fun getMembers(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Single<List<MemberApi>>

    @GET("providers/{providerId}/billables")
    fun getBillables(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Single<List<BillableApi>>

    @GET("diagnoses")
    fun getDiagnoses(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String
    ): Single<List<DiagnosisApi>>

    @POST("members")
    fun postMember(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Body member: MemberApi
    ): Completable

    @PATCH("members/{memberId}")
    fun patchMember(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("memberId") memberId: UUID,
            @Body patchParams: JsonObject
    ): Completable

    @Multipart
    @PATCH("members/{memberId}")
    fun patchPhoto(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("memberId") memberId: UUID,
            @Part("photo") photo: RequestBody
    ): Completable

    @POST("providers/{providerId}/identification_events")
    fun postIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body unsyncedEvent: IdentificationEventApi
    ): Completable

    @PATCH("identification_events/{identificationEventId}")
    fun patchIdentificationEvent(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("identificationEventId") identificationEventId: UUID,
            @Body patchParams: JsonObject
    ): Completable

    @POST("providers/{providerId}/encounters")
    fun postEncounter(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body unsyncedEncounter: EncounterApi
    ): Completable

    @Multipart
    @PATCH("encounters/{encounterId}")
    fun patchEncounterForm(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("encounterId") encounterId: UUID,
            @Part("forms[]") encounterForm: RequestBody
    ): Completable
}
