package org.watsi.uhp.viewmodels

import android.arch.lifecycle.ViewModel
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class StatusViewModel @Inject constructor (
        private val memberRepository: MemberRepository,
        private val deltaRepository: DeltaRepository
) : ViewModel() {
    init {
        // Do we even need a use case for this?
        // Create empty state Observable for this. MediatorLiveData. (Zipped, refer to HomeFragment in coverage-enrollment)
    }

    data class ViewState(
            val newMemberCount: Int,
            val editedMemberCount: Int,
            val unsyncedIdEventCount: Int,
            val unsyncedEncounterCount: Int,
            val unsyncedEncounterFormCount: Int,
            val unfetchedPhotos: Int
    )
}