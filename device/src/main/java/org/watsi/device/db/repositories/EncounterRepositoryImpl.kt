package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.db.DbHelper
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithMemberAndItemsAndFormsModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.EncounterWithItems
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class EncounterRepositoryImpl(
    private val encounterDao: EncounterDao,
    private val encounterItemDao: EncounterItemDao,
    private val diagnosisDao: DiagnosisDao,
    private val memberDao: MemberDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val clock: Clock
) : EncounterRepository {
    override fun revisedIds(): Single<List<UUID>> {
        return encounterDao.revisedIds()
    }

    override fun update(encounters: List<Encounter>): Completable {
        return Completable.fromAction {
            encounterDao.update(encounters.map { EncounterModel.fromEncounter(it, clock) })
        }.subscribeOn(Schedulers.io())
    }

    override fun find(ids: List<UUID>): Single<List<Encounter>> {
        return Single.fromCallable {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                encounterDao.find(ids).blockingGet()
            }.flatten().map {
                it.toEncounter()
            }
        }
    }

    override fun fetchReturnedClaims(): Single<List<EncounterWithExtras>> {
        return sessionManager.currentToken()?.let { token ->
            Single.fromCallable {
                val returnedClaims = api.getReturnedClaims(token.getHeaderString(), token.user.providerId).blockingGet()
                val returnedClaimsMemberIds = returnedClaims.map { it.memberId }
                val alreadyPersistedMembers = memberDao.findMembersByIds(returnedClaimsMemberIds).blockingGet().map { it.toMember() }

                returnedClaims.map { returnedClaim ->
                    val persistedMember = alreadyPersistedMembers.find { it.id == returnedClaim.memberId }
                    returnedClaim.toEncounterWithExtras(persistedMember)
                }
            }.subscribeOn(Schedulers.io())
        } ?: Single.error(Exception("Current token is null while calling EncounterRepositoryImpl.fetchingReturnedClaims"))
    }

    fun loadClaim(encounterModel: EncounterWithMemberAndItemsAndFormsModel): EncounterWithExtras {
        val encounterRelation = encounterModel.toEncounterWithMemberAndItemsAndForms()
        val diagnoses = diagnosisDao.findAll(encounterRelation.encounter.diagnoses).blockingGet()
                .map { it.toDiagnosis() }
        return EncounterWithExtras(
            encounterRelation.encounter,
            encounterRelation.member,
            encounterRelation.encounterItemRelations,
            diagnoses,
            encounterRelation.encounterForms
        )
    }

    override fun loadPendingClaimsCount(): Flowable<Int> {
        return encounterDao.pendingCount()
    }

    override fun loadPendingClaims(): Flowable<List<EncounterWithExtras>> {
        return encounterDao.pending().map { encounterModelList ->
            encounterModelList.map { encounterModel -> loadClaim(encounterModel) }
        }
    }

    override fun loadReturnedClaimsCount(): Flowable<Int> {
        return encounterDao.returnedCount()
    }

    override fun loadReturnedClaims(): Flowable<List<EncounterWithExtras>> {
        return encounterDao.returned().map { encounterModelList ->
            encounterModelList.map { encounterModel -> loadClaim(encounterModel) }
        }
    }

    override fun loadOnePendingClaim(): Maybe<EncounterWithExtras> {
        return encounterDao.loadOnePendingClaim().map { loadClaim(it) }.subscribeOn(Schedulers.io())
    }

    override fun loadOneReturnedClaim(): Maybe<EncounterWithExtras> {
        return encounterDao.loadOneReturnedClaim().map { loadClaim(it) }.subscribeOn(Schedulers.io())
    }

    override fun findWithExtras(id: UUID): Single<EncounterWithExtras> {
        return encounterDao.findWithMemberAndForms(id).map { loadClaim(it) }
    }

    override fun returnedIds(): Single<List<UUID>> {
        return encounterDao.returnedIds()
    }

    override fun find(id: UUID): Single<EncounterWithItems> {
        return encounterDao.find(id).map { it.toEncounterWithItems() }.subscribeOn(Schedulers.io())
    }

    override fun insert(encounterWithItemsAndForms: EncounterWithItemsAndForms, deltas: List<Delta>): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterWithItemsAndForms.encounter, clock)
            val encounterItemModels = encounterWithItemsAndForms.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }
            // TODO: select any billables that need to be inserted
            val encounterFormModels = encounterWithItemsAndForms.encounterForms.map {
                EncounterFormModel.fromEncounterForm(it, clock)
            }

            encounterDao.insert(
                encounterModel,
                encounterItemModels,
                emptyList(),
                encounterFormModels,
                deltas.map { DeltaModel.fromDelta(it, clock) }
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun upsert(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterWithItemsAndForms.encounter, clock)
            val encounterItemModels = encounterWithItemsAndForms.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }

            encounterDao.upsert(
                encounterModels = listOf(encounterModel),
                encounterItemModels = encounterItemModels,
                billableModels = emptyList(),
                priceScheduleModels = emptyList(),
                memberModels = emptyList()
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun upsert(encounters: List<EncounterWithExtras>): Completable {
        return Completable.fromAction {
            val encounterModels = encounters.map {
                EncounterModel.fromEncounter(it.encounter, clock)
            }
            val encounterItemModels = encounters.map {
                it.encounterItemRelations.map {
                    EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
                }
            }.flatten()
            val billableModels = encounters.map {
                it.encounterItemRelations.map {
                    BillableModel.fromBillable(it.billableWithPriceSchedule.billable, clock)
                }
            }.flatten()
            val priceScheduleModels = encounters.map {
                it.encounterItemRelations.map {
                    it.billableWithPriceSchedule.priceSchedules().map {
                        PriceScheduleModel.fromPriceSchedule(it, clock)
                    }
                }.flatten()
            }.flatten()
            val memberModels = encounters.map { MemberModel.fromMember(it.member, clock) }

            encounterDao.upsert(
                encounterModels = encounterModels,
                encounterItemModels = encounterItemModels,
                billableModels = billableModels,
                priceScheduleModels = priceScheduleModels,
                memberModels = memberModels
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun delete(encounterRelation: EncounterWithExtras): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterRelation.encounter, clock)

            val encounterItemModels = encounterRelation.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }
            val memberModel = MemberModel.fromMember(encounterRelation.member, clock)

            encounterDao.delete(
                encounterModel = encounterModel,
                encounterItemModels = encounterItemModels,
                memberModel = memberModel
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteEncounterItems(ids: List<UUID>): Completable {
        return Completable.fromAction {
            encounterItemDao.delete(ids)
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentToken()?.let { token ->
            find(delta.modelId).flatMapCompletable { encounterModel ->
                api.postEncounter(token.getHeaderString(), token.user.providerId, EncounterApi(encounterModel))
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
