package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.utils.Age
import org.watsi.domain.utils.AgeUnit
import java.util.UUID
import javax.inject.Inject


class MemberInformationViewModel @Inject constructor(
        private val createMemberUseCase: CreateMemberUseCase,
        private val clock: Clock
) : ViewModel() {
    private val observable = MutableLiveData<ViewState>()

    fun getObservable(membershipNumber: String): LiveData<ViewState> {
        observable.value = ViewState(membershipNumber = membershipNumber)
        return observable
    }

    fun onAgeChange(age: Int?) {
        observable.value?.let { observable.value = it.copy(age = age) }
    }

    fun onAgeUnitChange(ageUnit: AgeUnit) {
        observable.value?.let { observable.value = it.copy(ageUnit = ageUnit) }
    }

    fun onGenderChange(gender: Member.Gender) {
        observable.value?.let { observable.value = it.copy(gender = gender) }
    }

    fun onMedicalRecordNumberChange(medicalRecordNumber: String?) {
        observable.value?.let { observable.value = it.copy(medicalRecordNumber = medicalRecordNumber) }
    }

    fun buildEncounter(): EncounterWithItemsAndForms {
        val encounterId = UUID.randomUUID()
        val encounter = Encounter(encounterId, UUID.randomUUID(), UUID.randomUUID(), Instant.now(clock))
        return EncounterWithItemsAndForms(encounter, emptyList(), emptyList(), emptyList())
    }

    fun save(): Completable {
        val viewState = observable.value
        return if (viewState == null) {
            Completable.never()
        } else {
            val member = toMember(viewState, UUID.randomUUID(), clock)
            createMemberUseCase.execute(member)
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    companion object {
        fun toMember(viewState: ViewState, memberId: UUID, clock: Clock): Member {
            if (viewState.gender != null && viewState.age != null && viewState.medicalRecordNumber != null) {
                val birthdate = Age(viewState.age, viewState.ageUnit).toBirthdateWithAccuracy().first
                return Member(
                        id = memberId,
                        name = "Member Name", // Placeholder until we make a platform decision that "Member" doesn't require a name.
                        enrolledAt = Instant.now(clock),
                        birthdate = birthdate,
                        gender = viewState.gender,
                        photoId = null,
                        thumbnailPhotoId = null,
                        fingerprintsGuid = null,
                        cardId = null,
                        householdId = null,
                        language = null,
                        phoneNumber = null,
                        photoUrl = null,
                        membershipNumber = viewState.membershipNumber,
                        medicalRecordNumber = viewState.medicalRecordNumber
                )
            } else {
                throw IllegalStateException("MemberInformationViewModel.toMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }
    data class ViewState(val membershipNumber: String,
                         val age: Int? = null,
                         val ageUnit: AgeUnit = AgeUnit.years,
                         val gender: Member.Gender? = null,
                         val medicalRecordNumber: String? = null)
}