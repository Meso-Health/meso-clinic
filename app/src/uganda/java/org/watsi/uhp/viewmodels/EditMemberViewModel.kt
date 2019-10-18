package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.LocalDate
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel(
        private val loadMemberUseCase: LoadMemberUseCase,
        private val updateMemberUseCase: UpdateMemberUseCase,
        private val logger: Logger,
        private val viewStateObservable: MediatorLiveData<ViewState>
) : ViewModel() {

    @Inject constructor(loadMemberUseCase: LoadMemberUseCase,
                        updateMemberUseCase: UpdateMemberUseCase,
                        logger: Logger) :
            this(loadMemberUseCase, updateMemberUseCase, logger, MediatorLiveData<ViewState>())

    internal var sourceLiveData: LiveData<ViewState>? = null

    data class ViewState(val memberWithThumbnail: MemberWithThumbnail?)

    fun getObservable(memberId: UUID): LiveData<ViewState> {
        setLiveDataSource(memberId)
        return viewStateObservable
    }

    @Synchronized
    internal fun setLiveDataSource(memberId: UUID) {
        sourceLiveData?.let { viewStateObservable.removeSource(it) }
        sourceLiveData = createLiveDataFromLoadMember(memberId)
        sourceLiveData?.let { viewStateObservable.addSource(it, ViewStateObserver()) }
    }

    internal inner class ViewStateObserver : Observer<ViewState> {
        override fun onChanged(viewState: ViewState?) {
            viewState?.let { viewStateObservable.postValue(it) }
        }
    }

    internal fun createLiveDataFromLoadMember(memberId: UUID): LiveData<ViewState> {
        val flowable = loadMemberUseCase.execute(memberId)
                .map { ViewState(it) }
                .onErrorReturn {
                    logger.error(it)
                    ViewState(null)
                }
        return LiveDataReactiveStreams.fromPublisher(flowable)
    }

    internal fun callIfMemberExists(updateMember: (member: Member) -> Completable): Completable {
        viewStateObservable.value?.memberWithThumbnail?.member?.let { member ->
            return updateMember(member)
        }
        return Completable.never()
    }

    fun updateName(name: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(name = name))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateGender(gender: Member.Gender): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(gender = gender))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateBirthdate(birthdate: LocalDate, birthdateAccuracy: Member.DateAccuracy): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(birthdate = birthdate, birthdateAccuracy = birthdateAccuracy))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updatePhoneNumber(phoneNumberString: String): Completable {
        return callIfMemberExists { member ->
            val phoneNumber = if (phoneNumberString.isBlank()) null else phoneNumberString
            updateMemberUseCase.execute(member.copy(phoneNumber = phoneNumber))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateLanguage(language: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(language = language))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updatePhoto(rawPhotoId: UUID, thumbnailPhotoId: UUID): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(
                    photoId = rawPhotoId, thumbnailPhotoId = thumbnailPhotoId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updateMemberCard(cardId: String): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(cardId = cardId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
