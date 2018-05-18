package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import edu.emory.mathcs.backport.java.util.Arrays
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.ResponseBody
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
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.AuthenticationTokenFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.MemberFactory
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

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = MemberRepositoryImpl(
                mockDao, mockApi, mockSessionManager, mockPreferencesManager, mockPhotoDao, clock)
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
        whenever(mockDao.findFlowable(model.id)).thenReturn(Flowable.just(model))

        repository.find(model.id).test().assertValue(model.toMember())
    }

    @Test
    fun create() {
        val member = MemberFactory.build()
        val delta = DeltaFactory.build(modelName = Delta.ModelName.MEMBER)

        repository.save(member, listOf(delta)).test().assertComplete()

        verify(mockDao).upsert(
                MemberModel.fromMember(member, clock), listOf(DeltaModel.fromDelta(delta, clock)))
    }

    @Test
    fun fetch_noCurrentToken_completes() {
        whenever(mockSessionManager.currentToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_succeeds_updatesMembers() {
        val authToken = AuthenticationTokenFactory.build()
        val syncedModel = MemberModelFactory.build(clock = clock,
                                                   thumbnailPhotoId = UUID.randomUUID(),
                                                   photoUrl = null)
        val syncedModelPhotoUrl = "https://watsi.org/photo"
        val syncedModelApi = MemberApi(syncedModel.id, syncedModel.householdId, syncedModel.cardId,
                syncedModel.name, syncedModel.gender, syncedModel.birthdate,
                syncedModel.birthdateAccuracy, syncedModel.fingerprintsGuid,
                syncedModel.phoneNumber, syncedModelPhotoUrl)
        val inactiveMember = MemberModelFactory.build(clock = clock)
        val newMember = MemberModelFactory.build(clock = clock)
        val newMemberApi = MemberApi(newMember.id, newMember.householdId, newMember.cardId,
                newMember.name, newMember.gender, newMember.birthdate, newMember.birthdateAccuracy,
                newMember.fingerprintsGuid, newMember.phoneNumber, newMember.photoUrl)
        val unsyncedMember = MemberModelFactory.build(clock = clock)
        val unsyncedMemberApi = MemberApi(unsyncedMember.id, unsyncedMember.householdId,
                unsyncedMember.cardId, unsyncedMember.name, unsyncedMember.gender,
                unsyncedMember.birthdate, unsyncedMember.birthdateAccuracy,
                unsyncedMember.fingerprintsGuid, unsyncedMember.phoneNumber, unsyncedMember.photoUrl)

        whenever(mockSessionManager.currentToken()).thenReturn(authToken)
        whenever(mockApi.members(any(), any())).thenReturn(
                Single.just(listOf(syncedModelApi, newMemberApi, unsyncedMemberApi)))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(unsyncedMember)))
        whenever(mockDao.all()).thenReturn(
                Flowable.just(listOf(syncedModel, inactiveMember, unsyncedMember)))

        repository.fetch().test().assertComplete()

        verify(mockApi).members(authToken.getHeaderString(), authToken.user.providerId)
        verify(mockDao).deleteNotInList(listOf(syncedModel.id, newMember.id, unsyncedMember.id))
        verify(mockDao).upsert(listOf(syncedModel.copy(photoUrl = syncedModelPhotoUrl), newMember))
        verify(mockPreferencesManager).updateMemberLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_fails_returnsError() {
        val authToken = AuthenticationTokenFactory.build()
        val exception = Exception()
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)
        whenever(mockApi.members(any(), any())).then { throw exception }

        repository.fetch().test().assertError(exception)

        verify(mockApi).members(authToken.getHeaderString(), authToken.user.providerId)
        verify(mockDao, never()).unsynced()
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
        val modelList = listOf(MemberModelFactory.build())
        whenever(mockDao.checkedInMembers()).thenReturn(Flowable.just(modelList))

        repository.checkedInMembers().test().assertValue(modelList.map { it.toMember() })
    }

    @Test
    fun remainingHouseholdMembers() {
        val member = MemberFactory.build()
        val householdMembers = listOf(MemberFactory.build(householdId = member.householdId))
        whenever(mockDao.remainingHouseholdMembers(member.householdId, member.id)).thenReturn(
                Flowable.just(householdMembers.map { MemberModel.fromMember(it, clock) }))

        repository.remainingHouseholdMembers(member).test().assertValue(householdMembers)
    }

    @Test
    fun downloadPhotos() {
        val photoUrl = "http://localhost:5000/dragonfly/media/foo-9ce2ca927c19c2b0"
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
}
