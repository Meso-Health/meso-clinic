package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.CbhiId
import javax.inject.Inject

class SearchViewModel @Inject constructor() : ViewModel() {

    private val formStateObservable = MutableLiveData<FormState>()

    init {
        formStateObservable.value = FormState()
    }

    companion object {
        val memberStatusList: List<String> = listOf("P", "I")
    }

    fun onRegionNumberChange(regionNumber: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(regionNumber = regionNumber, error = "")
        }
    }

    fun onWoredaNumberChange(woredaNumber: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(woredaNumber = woredaNumber, error = "")
        }
    }

    fun onKebeleNumberChange(kebeleNumber: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(kebeleNumber = kebeleNumber, error = "")
        }
    }

    fun onMemberStatusChange(memberStatus: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(memberStatus = memberStatus, error = "")
        }
    }

    fun onHouseholdNumberChange(householdNumber: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(householdNumber = householdNumber, error = "")
        }
    }

    fun onHouseholdMemberNumberChange(householdMemberNumber: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(householdMemberNumber = householdMemberNumber, error = "")
        }
    }

    private fun parseCbhiId(formState: FormState): CbhiId {
        return CbhiId(
            formState.regionNumber.toIntOrNull(),
            formState.woredaNumber.toIntOrNull(),
            formState.kebeleNumber.toIntOrNull(),
            CbhiId.Paying.valueOf(formState.memberStatus),
            formState.householdNumber.toIntOrNull(),
            formState.householdMemberNumber.toIntOrNull()
        )
    }

    /**
     * This function is not null-safe by itself because it assumes validation has passed
     */
    fun getMembershipNumber(formState: FormState): String = parseCbhiId(formState).formatted()!!

    fun membershipNumberHasError(formState: FormState): Boolean = !parseCbhiId(formState).valid()

    fun setMembershipNumberError(error: String) {
        formStateObservable.value?.let {
            formStateObservable.value = it.copy(error = error)
        }
    }

    fun getFormStateObservable(): LiveData<FormState> {
        return formStateObservable
    }

    data class FormState(
        val regionNumber: String = "",
        val woredaNumber: String = "",
        val kebeleNumber: String = "",
        val memberStatus: String = "",
        val householdNumber: String = "",
        val householdMemberNumber: String = "",
        val error: String = ""
    )
}
