package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.device.managers.Logger
import org.watsi.domain.usecases.LoadReturnedClaimsCountUseCase
import javax.inject.Inject

class NewClaimViewModel @Inject constructor(
    private val loadReturnedClaimsCountUseCase: LoadReturnedClaimsCountUseCase,
    private val logger: Logger
) : ViewModel() {

    private val viewStateObservable = MutableLiveData<ViewState>()

    companion object {
        val memberStatusList: List<String> = listOf("P", "I")
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

    fun getMembershipNumber(viewState: ViewState): String {
        return viewState.regionNumber + "/" + viewState.woredaNumber + "/" + viewState.kebeleNumber + "/" +
                    viewState.memberStatus + "-" + viewState.householdNumber + "/" + viewState.householdMemberNumber
    }

    fun membershipNumberHasError(viewState: ViewState): Boolean {
        val hasError = viewState.regionNumber.isBlank() || viewState.woredaNumber.isBlank() || viewState.kebeleNumber.isBlank() ||
                viewState.memberStatus.isBlank() || viewState.householdNumber.isBlank()

        return hasError
    }

    fun setMembershipNumberError(error: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(error = error)
        }
    }

    fun getViewStateObservable(): LiveData<ViewState> {
        viewStateObservable.value = ViewState()
        loadReturnedClaimsCountUseCase.execute().subscribe({
            viewStateObservable.postValue(viewStateObservable.value?.copy(returnedClaimsCount = it))
        }, {
            logger.error(it)
        })

        return viewStateObservable
    }

    data class ViewState(val regionNumber: String = "",
                         val woredaNumber: String = "",
                         val kebeleNumber: String = "",
                         val memberStatus: String = "",
                         val householdNumber: String = "",
                         val householdMemberNumber: String = "",
                         val returnedClaimsCount: Int = 0,
                         val error: String = "")
}
