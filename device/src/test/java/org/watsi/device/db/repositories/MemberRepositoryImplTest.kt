package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import edu.emory.mathcs.backport.java.util.Arrays
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.api.models.MemberPaginationApi
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel
import org.watsi.device.db.models.MemberWithRawPhotoModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.MemberWithIdEventAndThumbnailPhotoModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member.ArchivedReason
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.UserFactory
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class MemberRepositoryImplTest {

    @Mock lateinit var mockDao: MemberDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    @Mock lateinit var mockPhotoDao: PhotoDao
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: MemberRepositoryImpl
    
    val user = UserFactory.build()
    val token = AuthenticationToken("token", clock.instant(), user)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = MemberRepositoryImpl(
                mockDao, mockApi, mockSessionManager, mockPreferencesManager, mockPhotoDao, clock)
    }

    @Test
    fun all() {
        val member1 = MemberModelFactory.build()
        val member2 = MemberModelFactory.build()
        val archivedMember = MemberModelFactory.build(archivedReason = ArchivedReason.DEATH, archivedAt = Instant.now())
        val memberList = listOf(member1, member2, archivedMember)
        val filteredMemberList = listOf(member1, member2)
        whenever(mockDao.all()).thenReturn(Flowable.just(memberList))
        whenever(mockDao.allUnarchived()).thenReturn(Flowable.just(filteredMemberList))

        repository.all(excludeArchived = true).test().assertValue(filteredMemberList.map { it.toMember() })
        repository.all(excludeArchived = false).test().assertValue(memberList.map { it.toMember() })
    }

    @Test
    fun find() {
        val model = MemberModelFactory.build()
        whenever(mockDao.find(model.id)).thenReturn(Maybe.just(model))

        repository.find(model.id).test().assertValue(model.toMember())
    }

    @Test
    fun findByCardId() {
        val cardId = "RWI123456"
        val archivedCardId = "RWI123457"
        val member = MemberFactory.build(cardId = cardId)
        val archivedMember = MemberFactory.build(cardId = archivedCardId, archivedReason = ArchivedReason.DEATH, archivedAt = Instant.now())
        whenever(mockDao.findByCardId(archivedCardId)).thenReturn(
            Maybe.just(MemberModel.fromMember(archivedMember, clock)))
        whenever(mockDao.findByCardIdUnarchived(cardId)).thenReturn(
            Maybe.just(MemberModel.fromMember(member, clock))
        )

        repository.findByCardId(cardId = archivedCardId, excludeArchived = false).test().assertValue(archivedMember)
        repository.findByCardId(cardId = cardId, excludeArchived = true).test().assertValue(member)

    }

    @Test
    fun checkedInMembers() {
        val memberModel = MemberModelFactory.build()
        val idEventModel = IdentificationEventModelFactory.build(memberId = memberModel.id)
        val memberRelation = MemberWithIdEventAndThumbnailPhotoModel(memberModel, listOf(idEventModel))
        whenever(mockDao.checkedInMembers()).thenReturn(Flowable.just(listOf(memberRelation)))

        repository.checkedInMembers().test().assertValue(
                listOf(memberRelation.toMemberWithIdEventAndThumbnailPhoto()))
    }

    @Test
    fun findHouseholdMembers() {
        val householdId = UUID.randomUUID()
        val member1 = MemberFactory.build(householdId = householdId)
        val member2 = MemberFactory.build(householdId = householdId)
        val archivedMember = MemberFactory.build(
            householdId = householdId,
            archivedReason = ArchivedReason.DEATH,
            archivedAt = Instant.now()
        )
        val memberRelation1 = MemberWithIdEventAndThumbnailPhoto(
            member = member1,
            identificationEvent = null,
            thumbnailPhoto = null
        )
        val memberRelation2 = MemberWithIdEventAndThumbnailPhoto(
            member = member2,
            identificationEvent = null,
            thumbnailPhoto = null
        )
        val archivedMemberRelation = MemberWithIdEventAndThumbnailPhoto(
            member = archivedMember,
            identificationEvent = null,
            thumbnailPhoto = null
        )
        val householdMemberRelations =
            listOf(memberRelation1, memberRelation2, archivedMemberRelation)
        val filteredHouseholdMemberRelations =
            listOf(memberRelation1, memberRelation2)


        whenever(mockDao.findHouseholdMembers(householdId)).thenReturn(
            Flowable.just(householdMemberRelations.map {
                MemberWithIdEventAndThumbnailPhotoModel(memberModel = MemberModel.fromMember(it.member, clock))
            })
        )

        whenever(mockDao.findHouseholdMembersUnarchived(householdId)).thenReturn(
            Flowable.just(filteredHouseholdMemberRelations.map {
                MemberWithIdEventAndThumbnailPhotoModel(memberModel = MemberModel.fromMember(it.member, clock))
            })
        )

        repository.findHouseholdMembers(householdId = householdId, excludeArchived = true).test()
            .assertValue(filteredHouseholdMemberRelations)
        repository.findHouseholdMembers(householdId = householdId, excludeArchived = false).test()
            .assertValue(householdMemberRelations)
    }

    @Test
    fun upsert() {
        val member = MemberFactory.build()
        val delta = DeltaFactory.build(modelName = Delta.ModelName.MEMBER)

        repository.upsert(member, listOf(delta)).test().assertComplete()

        verify(mockDao).upsert(
                MemberModel.fromMember(member, clock), listOf(DeltaModel.fromDelta(delta, clock)))
    }

    @Test
    fun fetch_noCurrentAuthenticationToken_completes() {
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_initialFetch_downloadsAllPaginatedMembers_updatesPageKey_updatesLastUpdatedAt() {
        val member1 = MemberModelFactory.build(clock = clock)
        val member2 = MemberModelFactory.build(clock = clock)
        val member3 = MemberModelFactory.build(clock = clock)
        val member4 = MemberModelFactory.build(clock = clock)
        val member5 = MemberModelFactory.build(clock = clock)
        val member6 = MemberModelFactory.build(clock = clock)
        val member7 = MemberModelFactory.build(clock = clock)
        val member8 = MemberModelFactory.build(clock = clock)
        val storedMemberLastFetched = Instant.ofEpochMilli(0)
        val storedPageKey = null
        val memberPaginationApi1 = MemberPaginationApi(
            pageKey = "page key 1",
            hasMore = true,
            members = listOf(
                MemberApi(member1.toMember()),
                MemberApi(member2.toMember()),
                MemberApi(member3.toMember())
            )
        )
        val memberPaginationApi2 = MemberPaginationApi(
            pageKey = "page key 2",
            hasMore = true,
            members = listOf(
                MemberApi(member4.toMember()),
                MemberApi(member5.toMember()),
                MemberApi(member6.toMember())
            )
        )
        val memberPaginationApi3 = MemberPaginationApi(
            pageKey = "page key 3",
            hasMore = false,
            members = listOf(
                MemberApi(member7.toMember()),
                MemberApi(member8.toMember())
            )
        )

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPreferencesManager.getMembersPageKey()).thenReturn(storedPageKey, memberPaginationApi1.pageKey, memberPaginationApi2.pageKey)
        whenever(mockApi.getMembers(any(), any(), eq(storedPageKey))).thenReturn(Single.just(memberPaginationApi1))
        whenever(mockApi.getMembers(any(), any(), eq(memberPaginationApi1.pageKey))).thenReturn(Single.just(memberPaginationApi2))
        whenever(mockApi.getMembers(any(), any(), eq(memberPaginationApi2.pageKey))).thenReturn(Single.just(memberPaginationApi3))
        whenever(mockDao.unsynced()).thenReturn(Single.just(emptyList()))
        whenever(mockDao.findAll(listOf(member1.id, member2.id, member3.id))).thenReturn(Single.just(listOf(member1, member2, member3)))
        whenever(mockDao.findAll(listOf(member4.id, member5.id, member6.id))).thenReturn(Single.just(listOf(member4, member5, member6)))
        whenever(mockDao.findAll(listOf(member7.id, member8.id))).thenReturn(Single.just(listOf(member7, member8)))

        repository.fetch().test().assertComplete()
        verify(mockDao).upsert(listOf(member1, member2, member3))
        verify(mockPreferencesManager).updateMembersPageKey(memberPaginationApi1.pageKey)
        verify(mockDao).upsert(listOf(member4, member5, member6))
        verify(mockPreferencesManager).updateMembersPageKey(memberPaginationApi2.pageKey)
        verify(mockDao).upsert(listOf(member7, member8))
        verify(mockPreferencesManager).updateMembersPageKey(memberPaginationApi3.pageKey)
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_subsequentFetch_serverReturnsChanges_updatesMembersWithoutUnsyncedChanges_updatesPageKey_updatesLastUpdatedAt() {
        val member1 = MemberModelFactory.build(clock = clock)
        val member2 = MemberModelFactory.build(clock = clock)
        val member3 = MemberModelFactory.build(clock = clock)
        val member4 = MemberModelFactory.build(clock = clock)
        val storedPageKey = "stored page key"
        val memberPaginationApi1 = MemberPaginationApi(
            pageKey = "page key 1",
            hasMore = true,
            members = listOf(
                MemberApi(member1.toMember()),
                MemberApi(member2.toMember()),
                MemberApi(member3.toMember())
            )
        )
        val memberPaginationApi2 = MemberPaginationApi(
            pageKey = "page key 2",
            hasMore = false,
            members = listOf(
                MemberApi(member4.toMember())
            )
        )

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPreferencesManager.getMembersPageKey()).thenReturn(storedPageKey, memberPaginationApi1.pageKey)
        whenever(mockApi.getMembers(any(), any(), eq(storedPageKey))).thenReturn(Single.just(memberPaginationApi1))
        whenever(mockApi.getMembers(any(), any(), eq(memberPaginationApi1.pageKey))).thenReturn(Single.just(memberPaginationApi2))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(member1, member3)), Single.just(emptyList()))
        whenever(mockDao.findAll(listOf(member2.id))).thenReturn(Single.just(listOf(member2)))
        whenever(mockDao.findAll(listOf(member4.id))).thenReturn(Single.just(listOf(member4)))

        repository.fetch().test().assertComplete()

        verify(mockDao).upsert(listOf(member2))
        verify(mockPreferencesManager).updateMembersPageKey(memberPaginationApi1.pageKey)
        verify(mockDao).upsert(listOf(member4))
        verify(mockPreferencesManager).updateMembersPageKey(memberPaginationApi2.pageKey)
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_subsequentFetch_serverReturnsEmptyPage_doesNotUpdateMembers_doesNotUpdatePageKey_updatesLastUpdatedAt() {
        val storedPageKey = "stored page key"
        val emptyPage = MemberPaginationApi(
            pageKey = storedPageKey,
            hasMore = false,
            members = emptyList()
        )

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPreferencesManager.getMembersPageKey()).thenReturn(storedPageKey)
        whenever(mockApi.getMembers(any(), any(), eq(storedPageKey))).thenReturn(Single.just(emptyPage))

        repository.fetch().test().assertComplete()

        verify(mockDao, never()).upsert(anyList())
        verify(mockPreferencesManager, never()).updateMembersPageKey(any())
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_fails_returnsError_doesNotUpdateAnything() {
        val exception = Exception()
        val storedPageKey = null

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPreferencesManager.getMembersPageKey()).thenReturn(storedPageKey)
        whenever(mockApi.getMembers(token.getHeaderString(), token.user.providerId, storedPageKey)).then { throw exception }

        repository.fetch().test().assertError(exception)

        verify(mockDao, never()).upsert(anyList())
        verify(mockPreferencesManager, never()).updateMemberLastFetched(any())
        verify(mockPreferencesManager, never()).updateMembersPageKey(any())
    }

    @Test
    fun downloadPhotos() {
        val photoUrl = "/dragonfly/media/foo-9ce2ca927c19c2b0"
        val photoBytes = ByteArray(1, { 0xa })
        val member = MemberFactory.build(photoUrl = photoUrl)
        val responseBody = ResponseBody.create(MediaType.parse("image/jpeg"), photoBytes)
        whenever(mockDao.needPhotoDownload()).thenReturn(
                Single.just(listOf(MemberModel.fromMember(member, clock))))
        whenever(mockApi.fetchPhoto(photoUrl)).thenReturn(Single.just(responseBody))

        repository.downloadPhotos().test().assertComplete()

        val captor = argumentCaptor<PhotoModel>()
        verify(mockPhotoDao).insert(captor.capture())
        val photo = captor.firstValue
        assert(Arrays.equals(photoBytes, photo.bytes))
        verify(mockDao).upsert(MemberModel.fromMember(member.copy(thumbnailPhotoId = photo.id), clock))
    }

    @Test
    fun sync_post() {
        val member = MemberFactory.build()
        val memberModel = MemberModel.fromMember(member, clock)
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.MEMBER,
                modelId = memberModel.id,
                synced = false
        )

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockDao.find(memberModel.id)).thenReturn(Maybe.just(memberModel))
        whenever(mockApi.postMember(token.getHeaderString(), MemberApi(member)))
                .thenReturn(Completable.complete())

        repository.sync(listOf(delta)).test().assertComplete()
    }

    @Test
    fun sync_patch() {
        val member = MemberFactory.build()
        val memberModel = MemberModel.fromMember(member, clock)
        val deltas = listOf("name", "gender").map { field -> Delta(
                action = Delta.Action.EDIT,
                modelName = Delta.ModelName.MEMBER,
                modelId = memberModel.id,
                field = field,
                synced = false
        ) }

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockDao.find(memberModel.id)).thenReturn(Maybe.just(memberModel))
        whenever(mockApi.patchMember(token.getHeaderString(), member.id, MemberApi.patch(member, deltas)))
                .thenReturn(Completable.complete())

        repository.sync(deltas).test().assertComplete()
    }

    @Test
    fun syncPhotos() {
        val photoModel = PhotoModelFactory.build()
        val memberModel = MemberModelFactory.build(photoId = photoModel.id, clock = clock)
        val memberWithRawPhotoModel = MemberWithRawPhotoModel(memberModel, listOf(photoModel))
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PHOTO,
                modelId = memberModel.id,
                synced = false
        )
        val captor = argumentCaptor<RequestBody>()

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPhotoDao.findMemberWithRawPhoto(memberModel.id))
                .thenReturn(Single.just(memberWithRawPhotoModel))
        whenever(mockApi.patchPhoto(eq(token.getHeaderString()), eq(memberModel.id), captor.capture()))
                .thenReturn(Completable.complete())

        repository.syncPhotos(listOf(delta)).test().assertComplete()

        val requestBody = captor.firstValue
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        assertTrue(java.util.Arrays.equals(photoModel.bytes, buffer.readByteArray()))
        verify(mockDao).upsert(memberModel.copy(photoId = null), emptyList())
    }

    @Test
    fun syncPhotos_syncFails_doesNotUpdatePhotoId() {
        val photoModel = PhotoModelFactory.build()
        val memberModel = MemberModelFactory.build(photoId = photoModel.id, clock = clock)
        val memberWithRawPhotoModel = MemberWithRawPhotoModel(memberModel, listOf(photoModel))
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PHOTO,
                modelId = memberModel.id,
                synced = false
        )
        val exception = Exception()

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockPhotoDao.findMemberWithRawPhoto(memberModel.id))
                .thenReturn(Single.just(memberWithRawPhotoModel))
        whenever(mockApi.patchPhoto(eq(token.getHeaderString()), eq(memberModel.id), any()))
                .then { throw exception }

        repository.syncPhotos(listOf(delta)).test().assertError(exception)

        verify(mockDao, never()).upsert(memberModel.copy(photoId = null), emptyList())
    }
    @Test
    fun byIds() {
        val models = listOf(
            MemberWithIdEventAndThumbnailPhotoModelFactory.build(MemberModelFactory.build()),
            MemberWithIdEventAndThumbnailPhotoModelFactory.build(MemberModelFactory.build())
        )
        val modelIds = models.mapNotNull { it.memberModel?.id }
        whenever(mockDao.findMemberRelationsByIds(modelIds)).thenReturn(Single.just(models))

        repository.byIds(modelIds).test().assertValue(models.map { memberWithIdEventAndThumbnailPhotoModel ->
            memberWithIdEventAndThumbnailPhotoModel.toMemberWithIdEventAndThumbnailPhoto()
        })
    }

    @Test
    fun byIds_moreThan1000_findsByChunk() {
        val models = (0..1000).map {
            MemberWithIdEventAndThumbnailPhotoModelFactory.build(MemberModelFactory.build())
        }
        val modelIds = models.mapNotNull { it.memberModel?.id }
        whenever(mockDao.findMemberRelationsByIds(any())).thenReturn(Single.just(models))

        repository.byIds(modelIds).test().assertComplete()

        verify(mockDao, times(1)).findMemberRelationsByIds(modelIds.slice(0..998))
        verify(mockDao, times(1)).findMemberRelationsByIds(modelIds.slice(999..1000))
    }
}
