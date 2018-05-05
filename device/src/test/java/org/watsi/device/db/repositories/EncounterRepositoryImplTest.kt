package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.verify
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms

@RunWith(MockitoJUnitRunner::class)
class EncounterRepositoryImplTest {

    @Mock lateinit var mockDao: EncounterDao
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: EncounterRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = EncounterRepositoryImpl(mockDao, clock)
    }

    @Test
    fun create() {
        val deltas = listOf(DeltaFactory.build(modelName = Delta.ModelName.ENCOUNTER))
        val encounter = EncounterFactory.build()
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id)
        val billable = BillableFactory.build()
        val encounterItemWithBillable = EncounterItemWithBillable(encounterItem, billable)
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndForms(
                encounter, listOf(encounterItemWithBillable), listOf(encounterForm))

        repository.create(encounterWithItemsAndForms, deltas).test().assertComplete()

        verify(mockDao).insert(encounter = EncounterModel.fromEncounter(encounter, clock),
                               items = listOf(EncounterItemModel.fromEncounterItem(
                                       encounterItem, clock)),
                               createdBillables = emptyList(),
                               forms = listOf(EncounterFormModel.fromEncounterForm(
                                       encounterForm, clock)),
                               deltas = deltas.map { DeltaModel.fromDelta(it, clock) })
    }
}
