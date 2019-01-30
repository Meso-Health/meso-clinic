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
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.PriceScheduleFactory
import org.watsi.domain.relations.EncounterWithItems
import java.util.UUID

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CoverageApiTest : OkReplayTest() {
    private val fixedInstance = Instant.parse("2018-03-23T08:10:36.306Z")
    private val providerId = 1
    private val clinicUser = "klinik"
    private val clinicUserPassword = "123456"

    // Make sure this is a valid token.
    private val tokenString = AuthenticationTokenFactory.build(
        token = "3NRq698o.tWSUMs6Dufv3GJFyzXcFq2TkAYAEYpWh"
    ).getHeaderString()

    // Make sure these IDs exist in the backend
    private val householdId = UUID.fromString("1c3ce616-33f2-4732-9917-b04ade7be357")
    private val billableId = UUID.fromString("7276e9e8-60a1-4596-b795-d7bca07a88ed")
    private val billableLatestPriceScheduleId = UUID.fromString("e76f7dd4-5985-46be-8965-d2cc4707cb4c")

    // When creating new tapes, make sure the following IDs do not exist in the backend
    private val memberId = UUID.fromString("918a6308-28c1-4a4a-b123-4c8233f21b15")
    private val identificationEventId = UUID.fromString("9911e82f2-6e46-4bd4-a9df-ddf11802c9c7")
    private val priceScheduleId = UUID.fromString("94fba921-13cd-4025-b622-7c8200f21b85")
    private val encounterId = UUID.fromString("94fba909-24cd-8026-b653-a0ea44080d2d")
    private val encounterItemId = UUID.fromString("95fba930-29cd-4022-b673-a9ea26180d9a")

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
    fun test000_getAuthToken() {
        api.getAuthToken(
            authorization = Credentials.basic(clinicUser, clinicUserPassword)
        ).test().assertComplete()
    }

    @Test
    fun test001_getMembers() {
        api.getMembers(
            tokenAuthorization = tokenString,
            providerId = providerId
        ).test().assertComplete()
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
        }
    }

    @Test
    fun test003_getDiagnoses() {
        api.getDiagnoses(tokenAuthorization = tokenString).test().assertComplete()
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
            occurredAt = fixedInstance
        )

        val encounterItem = EncounterItemFactory.build(
            id = encounterItemId,
            encounterId = encounter.id,
            billableId = billableId,
            priceScheduleId = priceScheduleId
        )

        val encounterWithItems = EncounterWithItems(
            encounter = encounter,
            encounterItems = listOf(encounterItem)
        )

        api.postEncounter(
            tokenAuthorization = tokenString,
            providerId = providerId,
            encounter = EncounterApi(encounterWithItems)
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
        }
    }
}
