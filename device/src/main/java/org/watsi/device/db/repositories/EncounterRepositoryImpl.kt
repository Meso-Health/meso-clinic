package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
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
import org.watsi.device.db.models.EncounterWithExtrasModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.db.models.ReferralModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class EncounterRepositoryImpl(
    private val encounterDao: EncounterDao,
    private val encounterItemDao: EncounterItemDao,
    private val diagnosisDao: DiagnosisDao,
    private val memberDao: MemberDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val clock: Clock,
    private val okHttpClient: OkHttpClient
) : EncounterRepository {
    override fun revisedIds(): Single<List<UUID>> {
        return encounterDao.revisedIds()
    }

    override fun update(encounters: List<Encounter>): Completable {
        return Completable.fromAction {
            encounterDao.update(encounters.map { EncounterModel.fromEncounter(it, clock) })
        }.subscribeOn(Schedulers.io())
    }

    override fun findAll(ids: List<UUID>): Single<List<Encounter>> {
        return Single.fromCallable {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                encounterDao.findAll(it).blockingGet()
            }.flatten().map {
                it.toEncounter()
            }
        }
    }

    override fun fetchReturnedClaims(): Single<List<EncounterWithExtras>> {
        return sessionManager.currentAuthenticationToken()?.let { token ->
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

    fun loadClaim(encounterModel: EncounterWithExtrasModel): EncounterWithExtras {
        val diagnoses = diagnosisDao.findAll(encounterModel.encounterModel?.diagnoses.orEmpty()).blockingGet()
                .map { it.toDiagnosis() }
        val encounterRelation = encounterModel.toEncounterWithExtras(diagnoses)
        return EncounterWithExtras(
            encounter = encounterRelation.encounter,
            member = encounterRelation.member,
            encounterItemRelations = encounterRelation.encounterItemRelations,
            diagnoses = diagnoses,
            encounterForms = encounterRelation.encounterForms,
            referral = encounterRelation.referral
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

    override fun find(id: UUID): Single<EncounterWithExtras> {
        return encounterDao.find(id).map {
            loadClaim(it)
        }
    }

    override fun returnedIds(): Single<List<UUID>> {
        return encounterDao.returnedIds()
    }

    override fun insert(encounterWithExtras: EncounterWithExtras, deltas: List<Delta>): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterWithExtras.encounter, clock)
            val encounterItemModels = encounterWithExtras.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }
            // TODO: select any billables that need to be inserted.
            // This will need to happen when we start de-activating billables on the backend.
            val encounterFormModels = encounterWithExtras.encounterForms.map {
                EncounterFormModel.fromEncounterForm(it, clock)
            }

            val referralModels= mutableListOf<ReferralModel>()
            encounterWithExtras.referral?.let {
                referralModels.add(ReferralModel.fromReferral(it))
            }

            encounterDao.insert(
                encounterModel = encounterModel,
                encounterItemModels = encounterItemModels,
                billableModels = emptyList(),
                encounterFormModels = encounterFormModels,
                referralModels = referralModels,
                deltaModels = deltas.map { DeltaModel.fromDelta(it, clock) }
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun upsert(encounterWithExtras: EncounterWithExtras): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterWithExtras.encounter, clock)
            val encounterItemModels = encounterWithExtras.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }
            val referralModels= mutableListOf<ReferralModel>()
            encounterWithExtras.referral?.let {
                referralModels.add(ReferralModel.fromReferral(it))
            }

            encounterDao.upsert(
                encounterModels = listOf(encounterModel),
                encounterItemModels = encounterItemModels,
                billableModels = emptyList(),
                priceScheduleModels = emptyList(),
                memberModels = emptyList(),
                referralModels = referralModels
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

            val referralModels= mutableListOf<ReferralModel>()
            encounters.forEach {
                it.referral?.let { referral ->
                    referralModels.add(ReferralModel.fromReferral(referral))
                }
            }

            encounterDao.upsert(
                encounterModels = encounterModels,
                encounterItemModels = encounterItemModels,
                billableModels = billableModels,
                priceScheduleModels = priceScheduleModels,
                memberModels = memberModels,
                referralModels = referralModels
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun delete(encounterRelation: EncounterWithExtras): Completable {
        return Completable.fromAction {
            val encounterModel = EncounterModel.fromEncounter(encounterRelation.encounter, clock)

            val encounterItemModels = encounterRelation.encounterItemRelations.map {
                EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
            }

            encounterDao.delete(
                encounterModel = encounterModel,
                encounterItemModels = encounterItemModels
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun deleteEncounterItems(ids: List<UUID>): Completable {
        return Completable.fromAction {
            encounterItemDao.delete(ids)
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            find(delta.modelId).flatMapCompletable { encounterWithExtras ->
                api.postEncounter(
                    tokenAuthorization= token.getHeaderString(),
                    providerId = token.user.providerId,
                    encounter = EncounterApi(encounterWithExtras)
                )
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache().evictAll()
            encounterDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}
