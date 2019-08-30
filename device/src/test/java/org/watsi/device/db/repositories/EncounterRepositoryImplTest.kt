package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterItemWithBillableAndPriceModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithExtrasModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.db.models.ReferralModel
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.DiagnosisModelFactory
import org.watsi.device.factories.EncounterFormModelFactory
import org.watsi.device.factories.EncounterItemModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.device.factories.PriceScheduleWithBillableModelFactory
import org.watsi.device.factories.ReturnedEncounterApiFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.DiagnosisFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.ReferralFactory
import org.watsi.domain.factories.UserFactory
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.utils.DateUtils
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class EncounterRepositoryImplTest {

    @Mock lateinit var mockDao: EncounterDao
    @Mock lateinit var mockEncounterItemDao: EncounterItemDao
    @Mock lateinit var mockDiagnosisDao: DiagnosisDao
    @Mock lateinit var mockMemberDao: MemberDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    lateinit var repository: EncounterRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = EncounterRepositoryImpl(mockDao, mockEncounterItemDao, mockDiagnosisDao,mockMemberDao, mockApi, mockSessionManager, mockPreferencesManager, clock)
    }

    @Test
    fun find() {
        val diagnoses = listOf(DiagnosisFactory.build())
        val encounterWithExtras = EncounterWithExtrasFactory.build(
            diagnoses = diagnoses
        )
        val diagnosisModels = diagnoses.map { DiagnosisModel.fromDiagnosis(it, clock) }
        val encounterWithExtrasModel = EncounterWithExtrasModel.fromEncounterWithExtras(encounterWithExtras, clock)

        whenever(mockDiagnosisDao.findAll(any())).thenReturn(Single.just(diagnosisModels))
        whenever(mockDao.find(encounterWithExtras.encounter.id)).thenReturn(Single.just(encounterWithExtrasModel))

        repository.find(encounterWithExtras.encounter.id).test().assertValue(encounterWithExtras)
    }

    @Test
    fun loadClaim() {
        val billableModel1 = BillableModelFactory.build()
        val billableModel2 = BillableModelFactory.build()
        val priceScheduleModel1 = PriceScheduleModelFactory.build(billableId = billableModel1.id)
        val priceScheduleModel2 = PriceScheduleModelFactory.build(billableId = billableModel2.id)

        val diagnosisModel1 = DiagnosisModelFactory.build()
        val diagnosisModel2 = DiagnosisModelFactory.build()
        val diagnosesIdList = listOf(diagnosisModel1.id, diagnosisModel2.id)

        val memberModel = MemberModelFactory.build()
        val referral = ReferralFactory.build()

        val referralModel = ReferralModel.fromReferral(referral)
        val encounterModel = EncounterModelFactory.build(
            memberId = memberModel.id,
            diagnoses = diagnosesIdList
        )
        val encounterItemModel1 = EncounterItemModelFactory.build(
            encounterId = encounterModel.id,
            priceScheduleId = priceScheduleModel1.id
        )
        val encounterItemModel2 = EncounterItemModelFactory.build(
            encounterId = encounterModel.id,
            priceScheduleId = priceScheduleModel2.id
        )
        val encounterItemRelationModel1 = EncounterItemWithBillableAndPriceModel(
            encounterItemModel = encounterItemModel1,
            priceScheduleWithBillableModel = listOf(PriceScheduleWithBillableModelFactory.build(billableModel1, priceScheduleModel1))
        )
        val encounterItemRelationModel2 = EncounterItemWithBillableAndPriceModel(
            encounterItemModel = encounterItemModel2,
            priceScheduleWithBillableModel = listOf(PriceScheduleWithBillableModelFactory.build(billableModel2, priceScheduleModel2))
        )

        val encounterFormModel = EncounterFormModelFactory.build(encounterId = encounterModel.id)
        val encounterWithExtrasModel = EncounterWithExtrasModel(
            encounterModel = encounterModel,
            memberModel = listOf(memberModel),
            encounterItemWithBillableAndPriceModels = listOf(encounterItemRelationModel1, encounterItemRelationModel2),
            encounterFormModels = listOf(encounterFormModel),
            referralModels = listOf(referralModel)
        )

        whenever(mockDiagnosisDao.findAll(diagnosesIdList))
                .thenReturn(Single.just(listOf(diagnosisModel1, diagnosisModel2)))

        assertEquals(repository.loadClaim(encounterWithExtrasModel),
            EncounterWithExtras(
                encounter = encounterModel.toEncounter(),
                member = memberModel.toMember(),
                encounterItemRelations = listOf(
                    encounterItemRelationModel1.toEncounterItemWithBillableAndPrice(),
                    encounterItemRelationModel2.toEncounterItemWithBillableAndPrice()
                ),
                diagnoses = listOf(
                    diagnosisModel1.toDiagnosis(),
                    diagnosisModel2.toDiagnosis()
                ),
                encounterForms = listOf(encounterFormModel.toEncounterForm()),
                referral = referral
            )
        )
    }

    @Test
    fun create() {
        val deltas = listOf(DeltaFactory.build(modelName = Delta.ModelName.ENCOUNTER))
        val encounter = EncounterFactory.build()
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id)
        val billableWithPrice = BillableWithPriceScheduleFactory.build()
        val encounterItemRelation = EncounterItemWithBillableAndPrice(encounterItem, billableWithPrice)
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val referral = ReferralFactory.build(encounterId = encounter.id)
        val member = MemberFactory.build(id = encounter.memberId)
        val encounterWithExtras = EncounterWithExtras(
            encounter = encounter,
            encounterItemRelations = listOf(encounterItemRelation),
            encounterForms = listOf(encounterForm),
            referral = referral,
            member = member,
            diagnoses = emptyList()
        )

        repository.insert(encounterWithExtras, deltas).test().assertComplete()

        verify(mockDao).insert(
            encounterModel = EncounterModel.fromEncounter(encounter, clock),
            encounterItemModels = listOf(EncounterItemModel.fromEncounterItem(encounterItem, clock)),
            encounterFormModels = listOf(EncounterFormModel.fromEncounterForm(encounterForm, clock)),
            referralModels = listOf(ReferralModel.fromReferral(referral)),
            deltaModels = deltas.map { DeltaModel.fromDelta(it, clock) }
        )
    }

    @Test
    fun sync() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)
        val encounterWithExtras = EncounterWithExtrasFactory.build()
        val delta = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithExtras.encounter.id,
            synced = false
        )

        val encounterWithExtrasModel = EncounterWithExtrasModel.fromEncounterWithExtras(encounterWithExtras, clock)

        whenever(mockDiagnosisDao.findAll(any())).thenReturn(Single.just(emptyList()))
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockDao.find(encounterWithExtras.encounter.id)).thenReturn(Single.just(encounterWithExtrasModel))
        whenever(mockApi.postEncounter(
            tokenAuthorization = token.getHeaderString(),
            providerId = user.providerId,
            encounter = EncounterApi(encounterWithExtras))
        ).thenReturn(Completable.complete())

        repository.sync(delta).test().assertComplete()
    }

    @Test
    fun fetchReturnedClaims() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)
        val memberOnPhone = MemberFactory.build()
        val memberNotOnPhone = MemberFactory.build()
        val returnedClaimWithMemberOnPhone = ReturnedEncounterApiFactory.build(
            EncounterWithExtrasFactory.build(member = memberOnPhone)
        )
        val returnedClaimWithMemberNotOnPhone = ReturnedEncounterApiFactory.build(
            EncounterWithExtrasFactory.build(member = memberNotOnPhone)
        )
        val spyReturnedClaimWithMemberOnPhone = spy(returnedClaimWithMemberOnPhone)
        val spyReturnedClaimWithMemberNotOnPhone = spy(returnedClaimWithMemberNotOnPhone)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockApi.getReturnedClaims(token.getHeaderString(), user.providerId)).thenReturn(
            Single.just(listOf(spyReturnedClaimWithMemberOnPhone, spyReturnedClaimWithMemberNotOnPhone))
        )
        whenever(mockMemberDao.findMembersByIds(listOf(memberOnPhone.id, memberNotOnPhone.id))).thenReturn(
            Single.just(listOf(MemberModel.fromMember(memberOnPhone, clock)))
        )

        repository.fetchReturnedClaims().test()

        verify(spyReturnedClaimWithMemberOnPhone).toEncounterWithExtras(persistedMember = memberOnPhone)
        verify(spyReturnedClaimWithMemberNotOnPhone).toEncounterWithExtras(persistedMember = null)
    }

    @Test
    fun upsert() {
        val encounters = (1..10).map {
            EncounterWithExtrasFactory.build()
        }

        repository.upsert(encounters).test().assertComplete()

        verify(mockDao).upsert(
            encounterModels = encounters.map {
                EncounterModel.fromEncounter(it.encounter, clock)
            },
            encounterItemModels = encounters.map {
                it.encounterItemRelations.map {
                    EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
                }
            }.flatten(),
            billableModels = encounters.map {
                it.encounterItemRelations.map {
                    BillableModel.fromBillable(it.billableWithPriceSchedule.billable, clock)
                }
            }.flatten(),
            priceScheduleModels = encounters.map {
                it.encounterItemRelations.map {
                    it.billableWithPriceSchedule.priceSchedules().map {
                        PriceScheduleModel.fromPriceSchedule(it, clock)
                    }
                }.flatten()
            }.flatten(),
            memberModels = encounters.map {
                MemberModel.fromMember(it.member, clock)
            },
            referralModels = encounters.mapNotNull {
                it.referral?.let { referral -> ReferralModel.fromReferral(referral) }
            },
            diagnosisModels = encounters.map {
                it.diagnoses.map {
                    DiagnosisModel.fromDiagnosis(it, clock)
                }
            }.flatten()
        )
    }

    @Test
    fun delete() {
        val encounterRelation = EncounterWithExtrasFactory.build()
        val encounterId = encounterRelation.encounter.id
        val encounterWithExtrasModel = EncounterWithExtrasModel.fromEncounterWithExtras(encounterRelation, clock)
        whenever(mockDao.find(encounterId)).thenReturn(Single.just(encounterWithExtrasModel))
        repository.delete(encounterId).test().assertComplete()

        verify(mockDao).delete(
            referralModels = encounterWithExtrasModel.referralModels!!,
            encounterItemModels = encounterWithExtrasModel.encounterItemWithBillableAndPriceModels!!.mapNotNull {
                it.encounterItemModel
            },
            encounterModel = encounterWithExtrasModel.encounterModel!!
        )
    }

    @Test
    fun encountersOccurredToday_hasEncounters_returnsTrue() {
        val encounter = EncounterFactory.build()
        val referenceTime = clock.instant()
        val startAndEndOfDay = DateUtils.getStartAndEndOfDayInstants(referenceTime, clock)

        whenever(mockDao.encountersForMemberBetween(
            encounter.memberId, startAndEndOfDay.first, startAndEndOfDay.second))
                .thenReturn(Single.just(listOf(encounter.id)))

        repository.encountersOccurredSameDay(referenceTime, encounter.memberId).test().assertValue(true)
    }

    @Test
    fun encountersOccurredToday_noEncounters_returnsFalse() {
        val memberId = UUID.randomUUID()
        val referenceTime = clock.instant()
        val startAndEndOfDay = DateUtils.getStartAndEndOfDayInstants(referenceTime, clock)

        whenever(mockDao.encountersForMemberBetween(
            memberId, startAndEndOfDay.first, startAndEndOfDay.second))
                .thenReturn(Single.just(emptyList()))

        repository.encountersOccurredSameDay(referenceTime, memberId).test().assertValue(false)
    }
}
