package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.usecases.CreateMemberUseCase
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

    fun onAgeUnitChange(ageUnit: Member.AgeUnit) {
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
        return Completable.complete()
    }

    data class ViewState(val membershipNumber: String,
                         val name: String = "",
                         val age: Int? = null,
                         val ageUnit: Member.AgeUnit = Member.AgeUnit.YEARS,
                         val gender: Member.Gender? = null,
                         val medicalRecordNumber: String? = null)
}