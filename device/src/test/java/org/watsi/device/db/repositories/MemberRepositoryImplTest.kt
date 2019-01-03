package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
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
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.MemberApi
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterWithMemberAndItemsAndFormsModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel
import org.watsi.device.db.models.MemberWithRawPhotoModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.MemberWithIdEventAndThumbnailPhotoModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
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
    @Mock lateinit var mockEncounterDao: EncounterDao
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: MemberRepositoryImpl
    
    val user = UserFactory.build()
    val token = AuthenticationToken("token", clock.instant(), user)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = MemberRepositoryImpl(
                mockDao, mockApi, mockSessionManager, mockPreferencesManager, mockPhotoDao, mockEncounterDao, clock)
    }

    @Test
    fun all() {
        val memberList = listOf(MemberModelFactory.build(), MemberModelFactory.build())
        whenever(mockDao.all()).thenReturn(Flowable.just(memberList))

        repository.all().test().assertValue(memberList.map { it.toMember() })
    }

    @Test
    fun find() {
        val model = MemberModelFactory.build()
        whenever(mockDao.find(model.id)).thenReturn(Single.just(model))

        repository.find(model.id).test().assertValue(model.toMember())
    }

    @Test
    fun findByCardId() {
        val cardId = "RWI123456"
        val member = MemberFactory.build(cardId = cardId)
        whenever(mockDao.findByCardId(cardId)).thenReturn(
                Maybe.just(MemberModel.fromMember(member, clock)))

        repository.findByCardId(cardId).test().assertValue(member)
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
        val householdMemberRelations = listOf(
            MemberWithIdEventAndThumbnailPhoto(member = member1, identificationEvent = null, thumbnailPhoto = null),
            MemberWithIdEventAndThumbnailPhoto(member = member2, identificationEvent = null, thumbnailPhoto = null)
        )

        whenever(mockDao.findHouseholdMembers(householdId)).thenReturn(
            Flowable.just(householdMemberRelations.map {
                MemberWithIdEventAndThumbnailPhotoModel(memberModel = MemberModel.fromMember(it.member, clock))
            })
        )

        repository.findHouseholdMembers(householdId).test().assertValue(householdMemberRelations)
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
    fun fetch_noCurrentToken_completes() {
        whenever(mockSessionManager.currentToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_noActiveMembers_succeeds_updatesMembers() {
        val noChange = MemberModelFactory.build(clock = clock)
        val noChangeApi = MemberApi(noChange.toMember())
        val serverEdited = MemberModelFactory.build(name = "Bryan", clock = clock)
        val serverEditedApi = MemberApi(serverEdited.copy(name = "Byron").toMember())
        val serverAdded = MemberModelFactory.build(clock = clock)
        val serverAddedApi = MemberApi(serverAdded.toMember())
        val serverRemoved = MemberModelFactory.build(clock = clock)
        val clientEdited = MemberModelFactory.build(name = "Ross", clock = clock)
        val clientEditedApi = MemberApi(clientEdited.copy(name = "Rocks").toMember())
        val clientAdded = MemberModelFactory.build(clock = clock)
        val clientEditedServerEdited = MemberModelFactory.build(name = "Mike", clock = clock)
        val clientEditedServerEditedApi = MemberApi(clientEditedServerEdited.copy(name = "Michael").toMember())
        val clientEditedServerRemoved = MemberModelFactory.build(clock = clock)

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockApi.getMembers(any(), any())).thenReturn(Single.just(listOf(
                noChangeApi,
                serverEditedApi,
                serverAddedApi,
                clientEditedApi,
                clientEditedServerEditedApi
        )))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(
                clientEdited,
                clientAdded,
                clientEditedServerEdited,
                clientEditedServerRemoved
        )))
        whenever(mockDao.all()).thenReturn(Flowable.just(listOf(
                noChange,
                serverEdited,
                serverRemoved,
                clientEdited,
                clientAdded,
                clientEditedServerEdited,
                clientEditedServerRemoved
        )))
        whenever(mockDao.checkedInMembers()).thenReturn(Flowable.just(emptyList()))
        whenever(mockEncounterDao.pending()).thenReturn(Flowable.just(emptyList()))
        whenever(mockEncounterDao.returned()).thenReturn(Flowable.just(emptyList()))
        whenever(mockEncounterDao.unsynced()).thenReturn(Single.just(emptyList()))

        repository.fetch().test().assertComplete()

        verify(mockApi).getMembers(token.getHeaderString(), token.user.providerId)
        verify(mockDao).delete(listOf(serverRemoved.id))
        verify(mockDao).upsert(listOf(
                noChange,
                serverEdited.copy(name = "Byron"),
                serverAdded
        ))
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_hasActiveMembers_succeeds_updatesMembers_preservesActiveMembers() {
        val noChange = MemberModelFactory.build(clock = clock)
        val noChangeApi = MemberApi(noChange.toMember())
        val serverRemoved = MemberModelFactory.build(clock = clock)
        val clientAdded = MemberModelFactory.build(clock = clock)
        val clientEditedServerEdited = MemberModelFactory.build(name = "Mike", clock = clock)
        val clientEditedServerEditedApi = MemberApi(clientEditedServerEdited.copy(name = "Michael").toMember())
        val clientEditedServerRemoved = MemberModelFactory.build(clock = clock)
        val clientCheckedInServerNotRemoved = MemberModelFactory.build(clock = clock)
        val clientCheckedInServerNotRemovedApi = MemberApi(clientCheckedInServerNotRemoved.toMember())
        val clientCheckedInServerNotRemovedWithId = MemberWithIdEventAndThumbnailPhotoModelFactory.build(
            memberModel = clientCheckedInServerNotRemoved,
            idEvent = IdentificationEventModelFactory.build(memberId = clientCheckedInServerNotRemoved.id)
        )
        val clientCheckedInServerRemoved = MemberModelFactory.build(clock = clock)
        val clientCheckedInServerRemovedWithId = MemberWithIdEventAndThumbnailPhotoModelFactory.build(
            memberModel = clientCheckedInServerRemoved,
            idEvent = IdentificationEventModelFactory.build(memberId = clientCheckedInServerRemoved.id)
        )
        val pendingEncounterMember = MemberModelFactory.build(clock = clock)
        val pendingEncounter = EncounterWithMemberAndItemsAndFormsModel(EncounterModelFactory.build(), listOf(pendingEncounterMember), null, null)
        val returnedEncounterMember = MemberModelFactory.build(clock = clock)
        val returnedEncounter = EncounterWithMemberAndItemsAndFormsModel(EncounterModelFactory.build(), listOf(returnedEncounterMember), null, null)
        val unsyncedEncounterMember = MemberModelFactory.build(clock = clock)
        val unsyncedEncounter = EncounterWithMemberAndItemsAndFormsModel(EncounterModelFactory.build(), listOf(unsyncedEncounterMember), null, null)

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockApi.getMembers(any(), any())).thenReturn(Single.just(listOf(
            noChangeApi,
            clientCheckedInServerNotRemovedApi,
            clientEditedServerEditedApi
        )))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(
            clientAdded,
            clientEditedServerEdited,
            clientEditedServerRemoved
        )))
        whenever(mockDao.all()).thenReturn(Flowable.just(listOf(
            noChange,
            clientCheckedInServerNotRemoved,
            serverRemoved,
            clientAdded,
            clientEditedServerEdited,
            clientEditedServerRemoved,
            clientCheckedInServerRemoved,
            pendingEncounterMember,
            returnedEncounterMember,
            unsyncedEncounterMember
        )))
        whenever(mockDao.checkedInMembers()).thenReturn(Flowable.just(listOf(
            clientCheckedInServerNotRemovedWithId,
            clientCheckedInServerRemovedWithId
        )))
        whenever(mockEncounterDao.pending()).thenReturn(Flowable.just(listOf(
            pendingEncounter
        )))
        whenever(mockEncounterDao.returned()).thenReturn(Flowable.just(listOf(
            returnedEncounter
        )))
        whenever(mockEncounterDao.unsynced()).thenReturn(Single.just(listOf(
            unsyncedEncounter
        )))

        repository.fetch().test().assertComplete()

        verify(mockApi).getMembers(token.getHeaderString(), token.user.providerId)
        verify(mockDao).delete(listOf(serverRemoved.id))
        verify(mockDao).upsert(listOf(
            noChange,
            clientCheckedInServerNotRemoved
        ))
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_fails_returnsError() {
        val exception = Exception()
        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockApi.getMembers(any(), any())).then { throw exception }

        repository.fetch().test().assertError(exception)

        verify(mockApi).getMembers(token.getHeaderString(), token.user.providerId)
        verify(mockDao, never()).unsynced()
        verify(mockDao, never()).checkedInMembers()
        verify(mockEncounterDao, never()).pending()
        verify(mockEncounterDao, never()).returned()
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

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(memberModel.id)).thenReturn(Single.just(memberModel))
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

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(memberModel.id)).thenReturn(Single.just(memberModel))
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

        whenever(mockSessionManager.currentToken()).thenReturn(token)
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

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockPhotoDao.findMemberWithRawPhoto(memberModel.id))
                .thenReturn(Single.just(memberWithRawPhotoModel))
        whenever(mockApi.patchPhoto(eq(token.getHeaderString()), eq(memberModel.id), any()))
                .then { throw exception }

        repository.syncPhotos(listOf(delta)).test().assertError(exception)

        verify(mockDao, never()).upsert(memberModel.copy(photoId = null), emptyList())
    }
}
