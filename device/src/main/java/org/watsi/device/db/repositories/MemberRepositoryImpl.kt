package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.db.DbHelper
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class MemberRepositoryImpl(
    private val memberDao: MemberDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val photoDao: PhotoDao,
    private val clock: Clock
) : MemberRepository {

    override fun all(): Flowable<List<Member>> {
        return memberDao.all().map { it.map { it.toMember() } }.subscribeOn(Schedulers.io())
    }

    override fun find(id: UUID): Maybe<Member> {
        return memberDao.find(id).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun findMemberWithThumbnailFlowable(id: UUID): Flowable<MemberWithThumbnail> {
        return memberDao.findFlowableMemberWithThumbnail(id).map { it.toMemberWithThumbnail() }
    }

    override fun findByCardId(cardId: String): Maybe<Member> {
        return memberDao.findByCardId(cardId).map { it.toMember() }.subscribeOn(Schedulers.io())
    }

    override fun findHouseholdIdByMembershipNumber(membershipNumber: String): Maybe<UUID> {
        return memberDao.findHouseholdIdByMembershipNumber(membershipNumber).subscribeOn(Schedulers.io())
    }

    override fun findHouseholdIdByCardId(cardId: String): Maybe<UUID> {
        return memberDao.findHouseholdIdByCardId(cardId).subscribeOn(Schedulers.io())
    }

    override fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhoto>> {
        return Single.fromCallable {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map {
                memberDao.findMemberRelationsByIds(it).blockingGet()
            }.flatten().map { it.toMemberWithIdEventAndThumbnailPhoto() }
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
     * Fetches members using pagination. Always overwrites local data with updated
     * server data, unless the local data has unsynced changes.
     */
    override fun fetch(): Completable {
        return sessionManager.currentToken()?.let { token ->
            Completable.fromAction {
                val isInitialFetch = preferencesManager.getMemberLastFetched() == Instant.ofEpochMilli(0)
                paginatedFetch(token, isInitialFetch)
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    private fun paginatedFetch(token: AuthenticationToken, isInitialFetch: Boolean) {
        val paginatedResponse = api.getMembers(
            token.getHeaderString(),
            token.user.providerId,
            preferencesManager.getMembersPageKey()
        ).blockingGet()
        val serverMembers = paginatedResponse.members
        val hasMore = paginatedResponse.hasMore
        val updatedPageKey = paginatedResponse.pageKey

        if (isInitialFetch) {
            memberDao.upsert(serverMembers.map { memberApi ->
                MemberModel.fromMember(memberApi.toMember(null), clock)
            })
        } else {
            // Do not update members with unsynced local changes
            val unsyncedClientMemberIds = memberDao.unsynced().blockingGet().map { it.id }
            val serverMembersWithoutUnsynced = serverMembers.filter { !unsyncedClientMemberIds.contains(it.id) }

            memberDao.upsert(serverMembersWithoutUnsynced.map { memberApi ->
                // Check whether member already exists on phone to perform special logic while upserting
                val persistedMember = memberDao.find(memberApi.id).blockingGet()
                MemberModel.fromMember(memberApi.toMember(persistedMember.toMember()), clock)
            })
        }

        preferencesManager.updateMembersPageKey(updatedPageKey)

        if (hasMore) {
            paginatedFetch(token, isInitialFetch)
        } else {
            preferencesManager.updateMemberLastFetched(clock.instant())
        }
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
