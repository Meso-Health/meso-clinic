package org.watsi.uhp.services

import okreplay.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.watsi.uhp.database.BillableDao
import org.watsi.uhp.database.MemberDao
import org.watsi.uhp.managers.Clock
import org.watsi.uhp.managers.PreferencesManager
import org.watsi.uhp.models.*
import java.util.*

/**
 * Integration test that uses OkReplay to test behavior of FetchService to actual HTTP responses
 *
 * Recorded responses (tapes) are stored in resources/okreplay.tapes/FetchServiceIntegrationTest.yaml
 * to record new responses or edit existing responses, temporarily add the following code to the class:
 *
 *      override fun tapeMode(): TapeMode = TapeMode.WRITE_SEQUENTIAL
 */
class FetchServiceIntegrationTest: ReplayTest() {

    private val preferencesManager = mock(PreferencesManager::class.java)

    private val service = FetchService()

    @Test
    @OkReplay
    fun fetchBillables_returns200_updatesBillables() {
        createBillable("foo")
        val billableWithUnsyncedEncounter = createBillable("bar")
        createUnsyncedEncounter(billableWithUnsyncedEncounter)

        service.fetchBillables("returns200", preferencesManager)

        assertEquals(BillableDao.all().size, 3)
        assertNotNull(BillableDao.findById(UUID.fromString("4438d9c5-41c9-4c68-ae3f-febf2ad4369a")))
        assertNotNull(BillableDao.findById(UUID.fromString("d40eec44-65b9-4202-83ba-d95126b54418")))
        assertNotNull(BillableDao.findById(billableWithUnsyncedEncounter.id))
        verify(preferencesManager).updateBillableLastModified()
    }

    @Test
    @OkReplay
    fun fetchBillables_returns304_doesNotUpdateBillables() {
        createBillable("foo")
        createBillable("bar")

        service.fetchBillables("returns304", preferencesManager)

        assertEquals(BillableDao.all().size, 2)
        verify(preferencesManager, never()).updateBillableLastModified()
    }

    //    TODO: implement when we introduce dependency injection so we can inject mock ExceptionManager
//    @Test
//    @OkReplay
//    fun fetchBillables_returns500_doesNotUpdateBillablesAndReportsError() {}

    @Test
    @OkReplay
    fun fetchMembers_returns200_updatesMembers() {
        createMember()

        service.fetchMembers("returns200", preferencesManager)

        assertEquals(MemberDao.all().size, 2)
        assertNotNull(UUID.fromString("44a4cdc1-a6a0-496a-8224-a4cae870ff97"))
        assertNotNull(UUID.fromString("0c318af8-6de4-4427-8405-5e4818f86618"))
        verify(preferencesManager).updateMembersLastModified()
    }

    @Test
    @OkReplay
    fun fetchMembers_returns304_doesNotUpdateMembers() {
        createMember()

        service.fetchMembers("returns304", preferencesManager)
        service.fetchMembers("returns304", preferencesManager)

        assertEquals(MemberDao.all().size, 1)
        verify(preferencesManager, never()).updateMembersLastModified()
    }

    //    TODO: implement when we introduce dependency injection so we can inject mock ExceptionManager
//    @Test
//    @OkReplay
//    fun fetchMembers_returns500_doesNotUpdateMembersAndReportsError() {}

    private fun createBillable(name: String): Billable {
        val billable = Billable()
        billable.name = name
        billable.type = Billable.TypeEnum.SERVICE
        billable.price = 0
        billable.generateId()
        BillableDao.create(billable)
        return billable
    }

    private fun createMember(): Member {
        val member = Member()
        member.id = UUID.randomUUID()
        member.fullName = "Foo"
        MemberDao.create(member)
        return member
    }

    private fun createUnsyncedEncounter(billable: Billable) {
        val encounter = Encounter()
        encounter.addEncounterItem(EncounterItem(billable, 2))
        encounter.occurredAt = Clock.getCurrentTime()
        val member = Member()
        member.id = UUID.randomUUID()
        encounter.member = member
        val identificationEvent = IdentificationEvent(member, IdentificationEvent.SearchMethodEnum.SEARCH_ID, null)
        identificationEvent.id = UUID.randomUUID()
        encounter.identificationEvent = identificationEvent
        encounter.saveChanges(null)
    }
}
