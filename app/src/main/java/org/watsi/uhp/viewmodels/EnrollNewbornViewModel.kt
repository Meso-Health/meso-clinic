package org.watsi.uhp.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.LoadPhotoUseCase
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import java.util.UUID

class EnrollNewbornViewModel(
        private val createMemberUseCase: CreateMemberUseCase,
        private val loadMemberPhotoUseCase: LoadPhotoUseCase,
        private val viewStateObservable: MutableLiveData<ViewState>,
        private val logger: Logger,
        private val clock: Clock
) : ViewModel() {

    @Inject constructor(createMemberUseCase: CreateMemberUseCase,   // not completely sure with this @Inject stuff, look more into Dagger
                        loadMemberPhotoUseCase: LoadPhotoUseCase,
                        logger: Logger,
                        clock: Clock)  :
            this(createMemberUseCase, loadMemberPhotoUseCase, MutableLiveData<ViewState>(), logger, clock)
            // ^ when I originally had logger before MutableLiveData<ViewState>(), was getting type mismatch error ??
    init {
        viewStateObservable.value = ViewState()
    }

    fun saveMember(memberId: UUID, formValidator: FormValidator) : Completable {
        val viewState = viewStateObservable.value

        if (viewState === null || viewState.status === MemberStatus.SAVING) {
            return Completable.never()
        }

        val validationErrors = formValidator.formValidationErrors(viewState)
        if (validationErrors.isNotEmpty()) {
            viewStateObservable.value = viewState.copy(status = MemberStatus.ERROR, errors = validationErrors)
            return Completable.error(ValidationException("Some fields are missing", validationErrors))
        }

        viewStateObservable.value = viewState.copy(status = MemberStatus.SAVING)
        val member = toMember(viewState, memberId, clock)

        return createMemberUseCase.execute(member).doOnError { onError(it) }
                .onErrorResumeNext { Completable.never() }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onNameChange(name: String) {
        viewStateObservable.value?.let { // what is this value? syntax... look more into MutableLiveData and methods on it
            val errors = it.errors.filterNot { it.key === MEMBER_NAME_ERROR } // 'it' here seems to refers to current viewState which is the viewStateObservable
            viewStateObservable.value = it.copy(name = name, errors = errors) // look into copy method, probably makes copy of viewStateObservable and sets with new value of new name and error, then sets it as new value to viewStateObservable?
        }
    }

    fun onBirthdateChange(birthdate: LocalDate) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MEMBER_BIRTHDATE_ERROR }
            viewStateObservable.value = it.copy(birthdate = birthdate, errors = errors)
        }
    }

    fun onGenderChange(gender: Member.Gender) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MEMBER_GENDER_ERROR }
            viewStateObservable.value = it.copy(gender = gender, errors = errors)
        }
    }

    fun onCaptureFingerprintId(fingerprintId: UUID?) { // why the ? after UUID here? look up syntax
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MEMBER_FINGERPRINTS_ERROR }
            viewStateObservable.value = it.copy(fingerprintsGuid = fingerprintId, errors = errors)
        }
    }

    fun onFingerprintScanError(errorMessage: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.toMutableMap()
            errors[MEMBER_FINGERPRINTS_ERROR]
        }
    }

    fun onCardScan(cardId: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MEMBER_CARD_ERROR }
            viewStateObservable.value = it.copy(cardId = cardId, errors = errors)
        }
    }

    fun onPhotoTaken(photoId: UUID, thumbnailPhotoId: UUID) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key === MEMBER_PHOTO_ERROR }
            viewStateObservable.value = it.copy(photoId = photoId, errors = errors)
        }

        loadMemberPhotoUseCase.execute(thumbnailPhotoId).subscribe(
                {
                    thumbnailPhoto  ->
                    viewStateObservable.postValue(viewStateObservable.value?.copy(thumbnailPhoto = thumbnailPhoto))  // I feel like there might be a cleaner way to do this line? why do we have to postValue here and not in others
                },
                {
                    exception ->
                    onError(exception)
                }
        )
    }

    private fun onError(throwable: Throwable) {
        val errors = HashMap<String, String>()
        errors[SAVE_ERROR] = throwable.localizedMessage
        logger.error(throwable, errors)
        viewStateObservable.postValue(viewStateObservable.value?.copy(status = MemberStatus.ERROR, errors = errors) )
    }

    object FormValidator { // why does this have to be a function inside an object instead of just a function?
        fun formValidationErrors(viewState: ViewState): Map<String, String> {
            val errors = HashMap<String, String>()

            if (viewState.name.isEmpty()) {
                errors[MEMBER_NAME_ERROR] = "Name is required"
            }

            if (viewState.gender === null) {
                errors[MEMBER_GENDER_ERROR] = "Gender is required"
            }

            if (viewState.birthdate === null) {
                errors[MEMBER_BIRTHDATE_ERROR] = "Birth date is required"   // we should probably change to Birthdate one word if this error is user facing to keep consistent with user facing designs that say birthdate one word
            }

            if (viewState.photoId === null) {
                errors[MEMBER_PHOTO_ERROR] = "A photo of the member is required"
            }

            if (viewState.cardId === null) {
                errors[MEMBER_CARD_ERROR] = "Member card is required"
            }

            return errors
        }
    }

    data class ViewState(val name: String = "",
                         val birthdate: LocalDate? = null,
                         val gender: Member.Gender? = null,
                         val photoId: UUID? = null,
                         val thumbnailPhoto: Photo? = null,
                         val fingerprintsGuid: UUID? = null,
                         val fingerprintsError: String? = null,
                         val cardId: String? = null,
                         val errors: Map<String, String> = emptyMap(),
                         val status: MemberStatus = MemberStatus.NEW)

    enum class MemberStatus { ERROR, SAVING, NEW }

    data class ValidationException(val msg: String, val errors: Map<String, String>): Exception(msg)

    companion object { // what is a companion?
        const val SAVE_ERROR = "save_error"
        const val MEMBER_NAME_ERROR = "member_name_error"
        const val MEMBER_BIRTHDATE_ERROR = "member_birthdate_error" // in enrollment, birthdate error triggers  MEMBER_AGE_ERROR, changing name here to make more sense for this context, make sure this is okay
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_PHOTO_ERROR = "member_photo_error"
        const val MEMBER_CARD_ERROR = "member_card_error"
        const val MEMBER_FINGERPRINTS_ERROR = "member_fingerprints_error"

        // why is toMember defined in here?
        fun toMember(viewState: ViewState, memberId: UUID, clock: Clock): Member {
            if (FormValidator.formValidationErrors(viewState).isEmpty() &&
                    viewState.gender != null && viewState.cardId != null &&
                    viewState.birthdate != null && viewState.photoId != null && viewState.name != null) {

                return Member(
                        id = memberId,
                        name = viewState.name,
                        enrolledAt = Instant.now(clock),
                        birthdate = viewState.birthdate,
                        gender = viewState.gender,
                        photoId = viewState.photoId,
                        thumbnailPhotoId = viewState.thumbnailPhoto?.id,
                        fingerprintsGuid = viewState.fingerprintsGuid,
                        cardId = viewState.cardId
                        // householdId take from mom?
                        // language from mom?
                        // phoneNumber from mom?
                        // photoUrl from mom?
                )
            } else {
                throw IllegalStateException("ViewStateToEntityMapper.fromMemberViewStateToMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }
}
