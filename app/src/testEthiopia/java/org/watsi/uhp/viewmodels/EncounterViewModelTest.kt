package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.usecases.LoadAllBillablesUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.testutils.AACBaseTest
import java.util.UUID

class EncounterViewModelTest : AACBaseTest() {
    private lateinit var viewModel: EncounterViewModel
    private lateinit var observable: LiveData<EncounterViewModel.ViewState>
    @Mock lateinit var mockBillableRepository: BillableRepository
    @Mock lateinit var mockLoadAllBillablesUseCase: LoadAllBillablesUseCase
    @Mock lateinit var mockLogger: Logger

    private val encounterId = UUID.randomUUID()
    private val memberId = UUID.randomUUID()
    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val initialViewState = EncounterViewModel.ViewState(encounterId = encounterId, encounterItemRelations = emptyList())
    private val serviceBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Service A", type = Billable.Type.SERVICE))
    private val serviceBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Service B", type = Billable.Type.SERVICE))
    private val drugBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Vitamin A", composition = "capsule", unit = "25 mg", type = Billable.Type.DRUG))
    private val drugBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Vitamin A", composition = "capsule", unit = "10 mg", type = Billable.Type.DRUG))
    private val drugBillable3 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Vitamin A", composition = "capsule", unit = "50 mg", type = Billable.Type.DRUG))
    private val drugBillable4 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Catgut", type = Billable.Type.DRUG))
    private val drugBillable5 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Anti malaria drugs", type = Billable.Type.DRUG))
    private val drugBillable6 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Panadol", type = Billable.Type.DRUG))
    private val labBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Lab A", type = Billable.Type.LAB))
    private val labBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Lab B", type = Billable.Type.LAB))
    private val labBillable3 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Lab C", type = Billable.Type.LAB))

    @Before
    fun setup() {
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        whenever(mockBillableRepository.all()).thenReturn(Single.just(listOf(
            serviceBillable1,
            serviceBillable2,
            drugBillable1,
            drugBillable2,
            drugBillable3,
            drugBillable4,
            drugBillable5,
            drugBillable6,
            labBillable1,
            labBillable2,
            labBillable3
        )))
        mockLoadAllBillablesUseCase = LoadAllBillablesUseCase(mockBillableRepository)
        viewModel = EncounterViewModel(mockLoadAllBillablesUseCase, mockLogger)
        observable = viewModel.getObservable(encounterId, emptyList())
        observable.observeForever{}
    }

    @Test
    fun init() {
        assertEquals(initialViewState, observable.value)
    }

    @Test
    fun selectType_lab() {
        viewModel.selectType(Billable.Type.LAB)
        assertEquals(
            observable.value,
            initialViewState.copy(
                type = Billable.Type.LAB,
                selectableBillables = listOf(labBillable1, labBillable2, labBillable3)
            )
        )
    }

    @Test
    fun selectType_service() {
        viewModel.selectType(Billable.Type.SERVICE)
        assertEquals(
            observable.value,
            initialViewState.copy(
                type = Billable.Type.SERVICE,
                selectableBillables = listOf(serviceBillable1, serviceBillable2)
            )
        )
    }

    @Test
    fun selectType_service_alreadyAdded() {
        viewModel.addItem(serviceBillable2)
        viewModel.selectType(Billable.Type.SERVICE)
        assertEquals(
            observable.value?.encounterItemRelations?.map { it.billableWithPriceSchedule },
            listOf(serviceBillable2)
        )
        assertEquals(observable.value?.type, Billable.Type.SERVICE)
        assertEquals(observable.value?.selectableBillables, listOf(serviceBillable1))
    }

    @Test
    fun selectType_drug() {
        viewModel.selectType(Billable.Type.DRUG)
        assertEquals(observable.value, initialViewState.copy(type = Billable.Type.DRUG))
    }

    @Test
    fun updateQuery_lessThan3Characters_noResults() {
        viewModel.selectType(Billable.Type.DRUG)
        viewModel.updateQuery("v")
        assertEquals(observable.value, initialViewState.copy(type = Billable.Type.DRUG))
        viewModel.updateQuery("vi")
        assertEquals(observable.value, initialViewState.copy(type = Billable.Type.DRUG))
    }

    @Test
    fun updateQuery_atleastThreeCharacters_orderByDetails() {
        viewModel.selectType(Billable.Type.DRUG)
        viewModel.updateQuery("vitamin")
        assertEquals(
            observable.value,
            initialViewState.copy(
                type = Billable.Type.DRUG,
                selectableBillables = listOf(drugBillable2, drugBillable1, drugBillable3)
            )
        )
    }


    @Test
    fun updateQuery_atleastThreeCharacters_oneResult() {
        viewModel.selectType(Billable.Type.DRUG)
        viewModel.updateQuery("malarial drugs")
        assertEquals(
            observable.value,
            initialViewState.copy(
                type = Billable.Type.DRUG,
                selectableBillables = listOf(drugBillable5)
            )
        )
    }

    @Test
    fun updateQuery_atleastThreeCharacters_alreadyAddedDoesNotShowUpInResults() {
        viewModel.addItem(drugBillable1)
        viewModel.selectType(Billable.Type.DRUG)
        viewModel.updateQuery("vitamin")
        assertEquals(
            observable.value?.encounterItemRelations?.map { it.billableWithPriceSchedule },
            listOf(drugBillable1)
        )
        assertEquals(observable.value?.type, Billable.Type.DRUG)
        assertEquals(observable.value?.selectableBillables, listOf(drugBillable2, drugBillable3))
    }

    @Test
    fun addItem() {
        viewModel.selectType(Billable.Type.SERVICE)
        viewModel.addItem(serviceBillable1)
        assertEquals(
            observable.value?.encounterItemRelations?.map { it.billableWithPriceSchedule },
            listOf(serviceBillable1)
        )
        assertEquals(observable.value?.type, Billable.Type.SERVICE)
    }

    @Test
    fun removeItem() {
        viewModel.addItem(serviceBillable1)
        val encounterItemId = observable.value?.encounterItemRelations?.map { it.encounterItem.id }?.first()
        assertNotNull(encounterItemId)
        viewModel.removeItem(encounterItemId!!)
        assertEquals(observable.value, initialViewState)
    }

    @Test
    fun setItemQuantity() {
        viewModel.addItem(drugBillable1)
        val encounterItemId = observable.value?.encounterItemRelations?.map { it.encounterItem.id }?.first()
        assertNotNull(encounterItemId)
        viewModel.setItemQuantity(encounterItemId!!, 5)
        assertEquals(
            observable.value?.encounterItemRelations?.map { it.billableWithPriceSchedule },
            listOf(drugBillable1)
        )
        assertEquals(observable.value?.encounterItemRelations?.map { it.encounterItem.quantity }, listOf(5))
    }

    @Test
    fun updateEncounterWithLineItems() {
        viewModel.addItem(serviceBillable1)
        viewModel.addItem(serviceBillable2)
        viewModel.addItem(drugBillable1)
        val encounterItemId = observable.value?.encounterItemRelations?.map { it.encounterItem.id }?.first()
        assertNotNull(encounterItemId)
        viewModel.setItemQuantity(encounterItemId!!, 5)

        val encounter = Encounter(encounterId, memberId, null, Instant.now(clock))
        val encounterFlowState = EncounterFlowState(encounter, emptyList(), emptyList(), emptyList())
        viewModel.updateEncounterWithLineItems(encounterFlowState)

        assertEquals(encounterFlowState.encounterForms.size, 0)
        assertEquals(encounterFlowState.encounterItemRelations.size, 3)
        assertEquals(
            encounterFlowState.encounterItemRelations.map { it.billableWithPriceSchedule },
            listOf(serviceBillable1, serviceBillable2, drugBillable1)
        )
    }
}
