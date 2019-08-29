package org.watsi.uhp.viewmodels

import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.usecases.FetchHouseholdIdByCardIdUseCase
import org.watsi.domain.usecases.FetchHouseholdIdByMembershipNumberUseCase
import java.util.UUID
import javax.inject.Inject

class DownloadHouseholdViewModel @Inject constructor(
    private val fetchHouseholdIdByCardIdUseCase: FetchHouseholdIdByCardIdUseCase,
    private val fetchHouseholdIdByMembershipNumberUseCase: FetchHouseholdIdByMembershipNumberUseCase
) : ViewModel() {

    fun downloadHouseholdByCardId(cardId: String): Single<UUID> {
        return fetchHouseholdIdByCardIdUseCase.execute(cardId)
    }

    fun downloadHouseholdByMembershipNumber(membershipNumber: String): Single<UUID> {
        return fetchHouseholdIdByMembershipNumberUseCase.execute(membershipNumber)
    }
}
