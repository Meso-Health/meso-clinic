package org.watsi.uhp.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class NewClaimViewModel(
        private val viewStateObservable: MutableLiveData<ViewState>
) : ViewModel() {

    fun onMemberIdChange(memberId: String) {
        viewStateObservable.value?.let {
            viewStateObservable.value = it.copy(memberId = memberId)
        }
    }

    data class ViewState(val memberId: String? = null)
}
