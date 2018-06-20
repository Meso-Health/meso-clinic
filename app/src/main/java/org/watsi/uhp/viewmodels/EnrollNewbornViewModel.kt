package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
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

    @Inject constructor(createMemberUseCase: CreateMemberUseCase,
                        loadMemberPhotoUseCase: LoadPhotoUseCase,
                        logger: Logger,
                        clock: Clock)  :
            this(createMemberUseCase, loadMemberPhotoUseCase, MutableLiveData<ViewState>(), logger, clock)
    init {
        viewStateObservable.value = ViewState()
    }

    fun saveMember(memberId: UUID, householdId: UUID, language: String?, formValidator: FormValidator = FormValidator) : Completable {
        val viewState = viewStateObservable.value

        if (viewState == null || viewState.status == MemberStatus.SAVING) {
            return Completable.never()
        }

        val validationErrors = formValidator.formValidationErrors(viewState)
        if (validationErrors.isNotEmpty()) {
            viewStateObservable.value = viewState.copy(status = MemberStatus.ERROR, errors = validationErrors)
            return Completable.error(ValidationException("Some fields are missing", validationErrors))
        }

        viewStateObservable.value = viewState.copy(status = MemberStatus.SAVING)
        val member = toMember(viewState, memberId, householdId, language, clock)

        return createMemberUseCase.execute(member).doOnError { onError(it) }
                .onErrorResumeNext { Completable.never() }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun onNameChange(name: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_NAME_ERROR }
            viewStateObservable.value = it.copy(name = name, errors = errors)
        }
    }

    fun onBirthdateChange(birthdate: LocalDate) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_BIRTHDATE_ERROR }
            viewStateObservable.value = it.copy(birthdate = birthdate, errors = errors)
        }
    }

    fun onGenderChange(gender: Member.Gender) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_GENDER_ERROR }
            viewStateObservable.value = it.copy(gender = gender, errors = errors)
        }
    }

    fun onCardScan(cardId: String) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_CARD_ERROR }
            viewStateObservable.value = it.copy(cardId = cardId, errors = errors)
        }
    }

    fun onPhotoTaken(photoId: UUID, thumbnailPhotoId: UUID) {
        viewStateObservable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_PHOTO_ERROR }
            viewStateObservable.value = it.copy(photoId = photoId, errors = errors)
        }

        loadMemberPhotoUseCase.execute(thumbnailPhotoId).subscribe(
                {
                    thumbnailPhoto  ->
                    viewStateObservable.postValue(viewStateObservable.value?.copy(thumbnailPhoto = thumbnailPhoto))
                },
                {
                    exception ->
                    onError(exception)
                }
        )
    }

    fun getViewStateObservable(): LiveData<ViewState> = viewStateObservable

    private fun onError(throwable: Throwable) {
        val errors = HashMap<String, String>()
        errors[SAVE_ERROR] = throwable.localizedMessage
        logger.error(throwable, errors)
        viewStateObservable.postValue(viewStateObservable.value?.copy(status = MemberStatus.ERROR, errors = errors) )
    }

    object FormValidator {
        fun formValidationErrors(viewState: ViewState): Map<String, String> {
            val errors = HashMap<String, String>()

            if (viewState.name.isEmpty()) {
                errors[MEMBER_NAME_ERROR] = "Name is required"
            }

            if (viewState.gender == null) {
                errors[MEMBER_GENDER_ERROR] = "Gender is required"
            }

            if (viewState.birthdate == null) {
                errors[MEMBER_BIRTHDATE_ERROR] = "Birthdate is required"
            }

            if (viewState.photoId == null) {
                errors[MEMBER_PHOTO_ERROR] = "A photo of the member is required"
            }

            if (viewState.cardId == null) {
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
                         val cardId: String? = null,
                         val errors: Map<String, String> = emptyMap(),
                         val status: MemberStatus = MemberStatus.NEW)

    enum class MemberStatus { ERROR, SAVING, NEW }

    data class ValidationException(val msg: String, val errors: Map<String, String>): Exception(msg)

    companion object {
        const val SAVE_ERROR = "save_error"
        const val MEMBER_NAME_ERROR = "member_name_error"
        const val MEMBER_BIRTHDATE_ERROR = "member_birthdate_error"
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_PHOTO_ERROR = "member_photo_error"
        const val MEMBER_CARD_ERROR = "member_card_error"

        fun toMember(viewState: ViewState, memberId: UUID, householdId: UUID, language: String?, clock: Clock): Member {
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
                        fingerprintsGuid = null,
                        cardId = viewState.cardId,
                        householdId = householdId,
                        language = language,
                        phoneNumber = null,
                        photoUrl = null
                )
            } else {
                throw IllegalStateException("ViewStateToEntityMapper.fromMemberViewStateToMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }
}
