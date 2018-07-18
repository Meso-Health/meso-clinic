package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import javax.inject.Inject

class NewClaimViewModel @Inject constructor() : ViewModel() {
    private val viewStateObservable = MutableLiveData<ViewState>()

    companion object {
        const val INVALID_MEMBERSHIP_NUMBER_ERROR = "Invalid membership number"
    }

    fun onRegionNumberChange(regionNumber: String) {

        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(regionNumber = regionNumber, error = "")
        }
    }

    fun onWoredaNumberChange(woredaNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(woredaNumber = woredaNumber, error = "")
        }
    }

    fun onKebeleNumberChange(kebeleNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(kebeleNumber = kebeleNumber, error = "")
        }
    }

    fun onMemberStatusChange(memberStatus: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(memberStatus = memberStatus, error = "")
        }
    }

    fun onHouseholdNumberChange(householdNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(householdNumber = householdNumber, error = "")
        }
    }

    fun onHouseholdMemberNumberChange(householdMemberNumber: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(householdMemberNumber = householdMemberNumber, error = "")
        }
    }


//    fun getMembershipNumberError(): String? {
//        viewStateObservable.value?.let {
//            val hasError = it.regionNumber.isBlank() || it.woredaNumber.isBlank() || it.kebeleNumber.isBlank() ||
//                    it.memberStatus.isBlank() || it.householdNumber.isBlank()
//
//            // TODO: check this syntax
//            if (hasError) return INVALID_MEMBERSHIP_NUMBER_ERROR else return ""
//        }
//    }
//
//    fun setMembershipNumberError(error: String?) {
//        viewStateObservable.value?.let {
//            val currentViewState = viewStateObservable.value
//
//            viewStateObservable.value = currentViewState?.copy(error = error)
//        }
//    }
//
//    fun getMembershipNumber(): String? {
//        viewStateObservable.value?.let {
//
//        }
//        val regionNumber = viewStateObservable.value.regionNumber
//    }
    fun getViewStateObservable(): LiveData<ViewState> {
        viewStateObservable.value = ViewState()
        return viewStateObservable
    }

    data class ViewState(val regionNumber: String = "",
                         val woredaNumber: String = "",
                         val kebeleNumber: String = "",
                         val memberStatus: String = "",
                         val householdNumber: String = "",
                         val householdMemberNumber: String = "",
                         val error: String = "")
}
