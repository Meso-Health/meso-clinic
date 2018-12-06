package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class MemberRepositoryImpl(private val memberDao: MemberDao,
                           private val api: CoverageApi,
                           private val sessionManager: SessionManager,
                           private val preferencesManager: PreferencesManager,
                           private val photoDao: PhotoDao,
                           private val clock: Clock) : MemberRepository {

    override fun all(): Flowable<List<Member>> {
        return memberDao.all().map { it.map { it.toMember() } }.subscribeOn(Schedulers.io())
    }

    override fun find(id: UUID): Single<Member> {
        return memberDao.find(id).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun findMemberWithThumbnailFlowable(id: UUID): Flowable<MemberWithThumbnail> {
        return memberDao.findFlowableMemberWithThumbnail(id).map { it.toMemberWithThumbnail() }
    }

    override fun findByCardId(cardId: String): Maybe<Member> {
        return memberDao.findByCardId(cardId).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun findHouseholdIdByMembershipNumber(cardId: String): Maybe<UUID> {
        return memberDao.findHouseholdIdByMembershipNumber(cardId).subscribeOn(Schedulers.io())
    }

    override fun findHouseholdIdByCardId(cardId: String): Maybe<UUID> {
        return memberDao.findHouseholdIdByCardId(cardId).subscribeOn(Schedulers.io())
    }

    override fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberDao.byIds(ids).map {
            it.map { it.toMemberWithIdEventAndThumbnailPhoto() }
        }.subscribeOn(Schedulers.io())
    }

    override fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberDao.checkedInMembers().map {
            it.map { it.toMemberWithIdEventAndThumbnailPhoto() }
        }
    }

    override fun isMemberCheckedIn(memberId: UUID): Flowable<Boolean> {
        return memberDao.isMemberCheckedIn(memberId).subscribeOn(Schedulers.io())
    }

    override fun findHouseholdMembers(householdId: UUID): Flowable<List<MemberWithIdEventAndThumbnailPhoto>> {
        return memberDao.findHouseholdMembers(householdId).map { memberWithIdEventAndThumbnailModels ->
            memberWithIdEventAndThumbnailModels.map { memberWithIdEventAndThumbnailModel ->
                memberWithIdEventAndThumbnailModel.toMemberWithIdEventAndThumbnailPhoto()
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun upsert(member: Member, deltas: List<Delta>): Completable {
        return Completable.fromAction {
            val deltaModels = deltas.map { DeltaModel.fromDelta(it, clock) }
            memberDao.upsert(MemberModel.fromMember(member, clock), deltaModels)
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Removes any synced client members that are not returned in the API results and
     * overwrites any synced client members if the API response contains updated data. Does not
     * remove or overwrite any unsynced data (new or edited members).
     */
    override fun fetch(): Completable {
        return sessionManager.currentToken()?.let { token ->
            Completable.fromAction {
                val serverMembers = api.getMembers(token.getHeaderString(), token.user.providerId).blockingGet()
                val serverMemberIds = serverMembers.map { it.id }
                val clientMembers = memberDao.all().blockingFirst()
                val clientMemberIds = clientMembers.map { it.id }
                val clientMembersById = clientMembers.groupBy { it.id }
                val unsyncedClientMembers = memberDao.unsynced().blockingGet()
                val unsyncedClientMemberIds = unsyncedClientMembers.map { it.id }
                val syncedClientMemberIds = clientMemberIds.minus(unsyncedClientMemberIds)
                val serverRemovedMemberIds = syncedClientMemberIds.minus(serverMemberIds)
                val serverMembersWithoutUnsynced = serverMembers.filter { !unsyncedClientMemberIds.contains(it.id) }

                memberDao.delete(serverRemovedMemberIds)
                memberDao.upsert(serverMembersWithoutUnsynced.map { memberApi ->
                    val persistedMember = clientMembersById[memberApi.id]?.firstOrNull()?.toMember()
                    MemberModel.fromMember(memberApi.toMember(persistedMember), clock)
                })
                preferencesManager.updateMemberLastFetched(clock.instant())
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun downloadPhotos(): Completable {
        return memberDao.needPhotoDownload().flatMapCompletable { memberModels ->
            Completable.concat(memberModels.map { memberModel ->
                val member = memberModel.toMember()
                api.fetchPhoto(member.photoUrl!!).flatMapCompletable {
                    Completable.fromAction {
                        val photo = Photo(UUID.randomUUID(), it.bytes())
                        photoDao.insert(PhotoModel.fromPhoto(photo, clock))
                        memberDao.upsert(memberModel.copy(thumbnailPhotoId = photo.id))
                    }
                }
            })
        }.subscribeOn(Schedulers.io())
    }

    override fun withPhotosToFetchCount(): Flowable<Int> {
        return memberDao.needPhotoDownloadCount()
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentToken()?.let { token ->
            find(deltas.first().modelId).flatMapCompletable { member ->
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postMember(token.getHeaderString(), MemberApi(member))
                } else {
                    api.patchMember(token.getHeaderString(), member.id, MemberApi.patch(member, deltas))
                }
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun syncPhotos(deltas: List<Delta>): Completable {
        return sessionManager.currentToken()?.let { token ->
            // the modelId in a photo delta corresponds to the member ID and not the photo ID
            // to make this querying and formatting of the sync request simpler
            val memberId = deltas.first().modelId
            photoDao.findMemberWithRawPhoto(memberId).flatMapCompletable { memberWithRawPhotoModel ->
                val memberWithRawPhoto = memberWithRawPhotoModel.toMemberWithRawPhoto()
                val requestBody = RequestBody.create(MediaType.parse("image/jpg"), memberWithRawPhoto.photo.bytes)
                Completable.concatArray(
                    api.patchPhoto(token.getHeaderString(), memberId, requestBody),
                    upsert(memberWithRawPhoto.member.copy(photoId = null), emptyList())
                )
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
