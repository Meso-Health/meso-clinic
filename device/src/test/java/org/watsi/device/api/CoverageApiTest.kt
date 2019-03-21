package org.watsi.device.api

import junit.framework.Assert.assertTrue
import okhttp3.Credentials
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.threeten.bp.Instant
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.testutils.OkReplayTest
import org.watsi.domain.entities.Delta
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
        token = "W5bJhMgC.EHggPdPvgqwVHi7wjqLKSbpLuutmNbqQ"
    ).getHeaderString()

    // Make sure these correspond to real ids in the backend.
    private val householdId = UUID.fromString("e3c4d82a-655f-4219-a651-7fc51bbcd042")
    private val billableId = UUID.fromString("0025609f-4b08-4b4b-9a07-080a6b36ba19")
    private val billableLatestPriceScheduleId = UUID.fromString("c35d1116-7a25-496d-b991-bcdae7bce278")

    // When creating new tapes, make sure the following IDs do not exist in the backend.
    private val memberId = UUID.fromString("444a6322-48c1-4a4a-b123-4c2233f21b11")
    private val identificationEventId = UUID.fromString("9114e82f2-6e42-4bd4-a9df-ddf11802c9c1")
    private val priceScheduleId = UUID.fromString("14fba944-13cd-4022-b622-7c8200f21b81")
    private val encounterId = UUID.fromString("11fba409-44cd-8026-b253-a0ea44080d1d")
    private val encounterItemId = UUID.fromString("2fba944-29cd-4022-b273-a9ea26180d1a")
    private val referralId = UUID.fromString("22fba944-44cd-4022-b273-a9ea26180d1a")

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
            preparedAt = fixedInstance
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
                encounterId = encounter.id
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
    fun test010_getReturnedClaims() {
        val result = api.getReturnedClaims(
            tokenAuthorization = tokenString,
            providerId = providerId
        ).test()

        result.assertComplete()

        // Assert that the providerComment is not null for at least one of the returned claims
        result.values().first().let { returnedClaims ->
            assertTrue(
                returnedClaims.any { returnedClaim ->
                    returnedClaim.providerComment != null
                }
            )
            assertTrue(
                returnedClaims.map { it.toEncounterWithExtras(null) }.isNotEmpty()
            )
        }
    }
}
