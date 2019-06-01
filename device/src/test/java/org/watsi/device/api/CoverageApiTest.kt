package org.watsi.device.api

import junit.framework.Assert.assertTrue
import okhttp3.Credentials
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.testutils.OkReplayTest
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.factories.AuthenticationTokenFactory
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.PriceScheduleFactory
import org.watsi.domain.factories.ReferralFactory
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CoverageApiTest : OkReplayTest() {
    private val fixedInstance = Instant.parse("2018-03-23T08:10:36.306Z")
    private val providerId = 1
    private val clinicUser = "provider1"
    private val clinicUserPassword = "123456"

    // Make sure this is a valid token.
    private val tokenString = AuthenticationTokenFactory.build(
        token = "RCZzfeoe.XR74SK4cVyAc36TuuuXw1zFUjMiCSJsa"
    ).getHeaderString()

    // Make sure these correspond to real ids in the backend.
    private val householdId = UUID.fromString("f408ffdd-2937-4915-abca-51bef447e058")
    private val billableId = UUID.fromString("fff70242-09ce-4fbc-ad23-cff0cbd3219a")
    private val billableLatestPriceScheduleId = UUID.fromString("69c78097-782f-4f84-b77b-ead4770644bc")

    // When creating new tapes, make sure the following IDs do not exist in the backend.
    private val memberId = UUID.fromString("224a2322-48c1-4a4a-b123-4c2233f21b11")
    private val identificationEventId = UUID.fromString("2214e82f2-6e42-4bd4-a9df-ddf11802c2c1")
    private val priceScheduleId = UUID.fromString("12fba944-13cd-4022-b622-7c8200f21b81")
    private val encounterId = UUID.fromString("11fba129-44cd-8026-b253-a0ea44080d1d")
    private val encounterItemId = UUID.fromString("21ba944-29cd-4022-b273-a9ea26180d1a")
    private val referralId = UUID.fromString("21fba934-44cd-4022-b273-a9ea26180d1a")
    private val identificationEventId2 = UUID.fromString("1df3522c-83e3-11e9-bc42-526af7764f64")
    private val encounterId2 = UUID.fromString("eaa011bc-83e2-11e9-bc42-526af7764f64")

    private val member = MemberFactory.build(
        id = memberId,
        householdId = householdId,
        enrolledAt = fixedInstance
    )

    private val identificationEvent = IdentificationEventFactory.build(
        id = identificationEventId,
        memberId = memberId,
        throughMemberId = memberId,
        occurredAt = fixedInstance
    )

    private val identificationEvent2 = IdentificationEventFactory.build(
        id = identificationEventId2,
        memberId = memberId,
        throughMemberId = memberId,
        occurredAt = fixedInstance
    )


    override fun afterSetup() {
        // no-op
    }

    @Test
    fun test000_login() {
        val result = api.login(
            authorization = Credentials.basic(clinicUser, clinicUserPassword)
        ).test()
        result.assertComplete()
        result.values().first().toAuthenticationToken()
    }

    @Test
    fun test001_getMembers() {
        val result = api.getMembers(
            tokenAuthorization = tokenString,
            providerId = providerId,
            pageKey = null
        ).test()
        result.assertComplete()
        assertTrue(result.values().first().members.map { it.toMember(null) }.isNotEmpty())
    }

    @Test
    fun test002_getBillables() {
        val result = api.getBillables(
            tokenAuthorization = tokenString,
            providerId = providerId
        ).test()

        result.assertComplete()

        // This code block makes sure that some of those billables do not have non-null composition and unit.
        result.values().first().let { billablesWithPriceSchedules ->
            assertTrue(billablesWithPriceSchedules.any { billableWithPriceScheduleApi ->
                billableWithPriceScheduleApi.composition != null &&
                billableWithPriceScheduleApi.unit != null
            })
            assertTrue(billablesWithPriceSchedules.map { it.toBillableWithPriceSchedule() }.isNotEmpty())
        }
    }

    @Test
    fun test003_getDiagnoses() {
        val result = api.getDiagnoses(tokenAuthorization = tokenString).test()
        result.assertComplete()
        assertTrue(result.values().first().map { it.toDiagnosis() }.isNotEmpty())
    }

    @Test
    fun test004_postMember() {
        api.postMember(
            tokenAuthorization = tokenString,
            member = MemberApi(member)
        ).test().assertComplete()
    }

    @Test
    fun test005_patchMember() {
        api.patchMember(
            tokenAuthorization = tokenString,
            memberId = memberId,
            patchParams = MemberApi.patch(
                member,
                listOf(Delta(
                    action = Delta.Action.EDIT,
                    modelName = Delta.ModelName.MEMBER,
                    modelId = member.id,
                    field = MemberApi.MEDICAL_RECORD_NUMBER_FIELD
                ))
            )
        ).test().assertComplete()
    }

    @Test
    fun test006_postIdentificationEvent() {
        api.postIdentificationEvent(
            tokenAuthorization = tokenString,
            providerId = providerId,
            identificationEvent = IdentificationEventApi(identificationEvent)
        ).test().assertComplete()
    }

    @Test
    fun test007_patchIdentificationEvent() {
        api.patchIdentificationEvent(
            tokenAuthorization = tokenString,
            identificationEventId = identificationEvent.id,
            patchParams = IdentificationEventApi.patch(
                identificationEvent,
                listOf(
                    Delta(
                        action = Delta.Action.EDIT,
                        modelName = Delta.ModelName.IDENTIFICATION_EVENT,
                        modelId = identificationEvent.id,
                        field = IdentificationEventApi.DISMISSED_FIELD
                    )
                )
            )
        ).test().assertComplete()
    }

    @Test
    fun test008_postPriceSchedule() {
        val priceSchedule = PriceScheduleFactory.build(
            id = priceScheduleId,
            billableId = billableId,
            previousPriceScheduleModelId = billableLatestPriceScheduleId,
            issuedAt = fixedInstance
        )

        api.postPriceSchedule(
            tokenAuthorization = tokenString,
            providerId = providerId,
            priceSchedule = PriceScheduleApi(priceSchedule)
        ).test().assertComplete()
    }

    @Test
    fun test009_postEncounter() {
        val encounter = EncounterFactory.build(
            id = encounterId,
            memberId = memberId,
            identificationEventId = identificationEventId,
            occurredAt = fixedInstance,
            preparedAt = fixedInstance,
            submittedAt = fixedInstance
        )

        val encounterItem = EncounterItemFactory.build(
            id = encounterItemId,
            encounterId = encounter.id,
            priceScheduleId = priceScheduleId
        )

        val encounterWithExtras = EncounterWithExtras(
            encounter = encounter,
            encounterItemRelations = listOf(EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = BillableWithPriceScheduleFactory.build(
                    billable = BillableFactory.build(id = billableId),
                    priceSchedule = PriceScheduleFactory.build(
                        id = priceScheduleId,
                        billableId = billableId
                    )
                ),
                encounterItem = encounterItem
            )),
            referral = ReferralFactory.build(
                id = referralId,
                encounterId = encounter.id,
                date = LocalDate.of(1993, 5, 11)
            ),
            member = MemberFactory.build(
                id = encounter.memberId
            ),
            diagnoses = emptyList(),
            encounterForms = emptyList()
        )

        api.postEncounter(
            tokenAuthorization = tokenString,
            providerId = providerId,
            encounter = EncounterApi(encounterWithExtras)
        ).test().assertComplete()
    }

    @Test
    fun test010_postPartialEncounter() {
        val encounter = EncounterFactory.build(
            id = encounterId2,
            memberId = memberId,
            identificationEventId = identificationEventId2,
            occurredAt = fixedInstance,
            preparedAt = null,
            submittedAt = null,
            visitReason = Encounter.VisitReason.REFERRAL,
            inboundReferralDate = fixedInstance.minus(1, ChronoUnit.DAYS)
        )

        val encounterWithExtras = EncounterWithExtras(
            encounter = encounter,
            encounterItemRelations = emptyList(),
            encounterForms = emptyList(),
            referral = null,
            member = MemberFactory.build(id = encounter.memberId),
            diagnoses = emptyList()
        )

        // Create idEvent first
        api.postIdentificationEvent(
            tokenAuthorization = tokenString,
            providerId = providerId,
            identificationEvent = IdentificationEventApi(identificationEvent2)
        ).test().assertComplete()

        api.postEncounter(
            tokenAuthorization = tokenString,
            providerId = providerId,
            encounter = EncounterApi(encounterWithExtras)
        ).test().assertComplete()
    }

    @Test
    fun test011_getReturnedClaims() {
        val result = api.getReturnedClaims(
            tokenAuthorization = tokenString,
            providerId = providerId
        ).test()

        result.assertComplete()

        // Assert that the claims in the backend when recording have:
        // - at least one returned claim with a provider comment
        // - at least one returned claim with a referral
        result.values().first().let { returnedClaims ->
            assertTrue(
                returnedClaims.any { returnedClaim ->
                    returnedClaim.providerComment != null
                }
            )
            assertTrue(
                returnedClaims.any { returnedClaim ->
                    returnedClaim.referrals.isNotEmpty()
                }
            )
            assertTrue(
                returnedClaims.any { returnedClaim ->
                    returnedClaim.patientOutcome != null
                }
            )
            assertTrue(
                returnedClaims.map { it.toEncounterWithExtras(null) }.isNotEmpty()
            )
        }
    }
}
