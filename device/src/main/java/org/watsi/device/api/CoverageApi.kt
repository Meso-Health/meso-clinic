package org.watsi.device.api

import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.watsi.device.api.models.AuthenticationTokenApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.api.models.BillableWithPriceScheduleApi
import org.watsi.device.api.models.DiagnosisApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.api.models.MemberPaginationApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.api.models.ReturnedEncounterApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Authorization"

interface CoverageApi {

    @POST("authentication_token")
    fun login(
            @Header(AUTHORIZATION_HEADER) authorization: String
    ): Single<AuthenticationTokenApi>

    @GET("providers/{providerId}/members")
    fun getMembers(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Query("page_key") pageKey: String?
    ): Single<MemberPaginationApi>

    @GET("providers/{providerId}/billables")
    fun getBillables(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Single<List<BillableWithPriceScheduleApi>>

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
            @Body identificationEvent: IdentificationEventApi
    ): Completable

    @PATCH("identification_events/{identificationEventId}")
    fun patchIdentificationEvent(
        @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
        @Path("identificationEventId") identificationEventId: UUID,
        @Body patchParams: JsonObject
    ): Completable

    @POST("providers/{providerId}/billables")
    fun postBillable(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body billable: BillableApi
    ): Completable

    @POST("providers/{providerId}/price_schedules")
    fun postPriceSchedule(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body priceSchedule: PriceScheduleApi
    ): Completable

    @POST("providers/{providerId}/encounters")
    fun postEncounter(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int,
            @Body encounter: EncounterApi
    ): Completable

    @GET("providers/{providerId}/encounters/returned")
    fun getReturnedClaims(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("providerId") providerId: Int
    ): Single<List<ReturnedEncounterApi>>

    @Multipart
    @PATCH("encounters/{encounterId}")
    fun patchEncounterForm(
            @Header(AUTHORIZATION_HEADER) tokenAuthorization: String,
            @Path("encounterId") encounterId: UUID,
            @Part("forms[]") encounterForm: RequestBody
    ): Completable

    @GET
    fun fetchPhoto(@Url photoUrl: String): Single<ResponseBody>
}
