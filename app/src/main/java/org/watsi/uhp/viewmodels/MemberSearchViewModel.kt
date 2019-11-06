package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class MemberSearchViewModel @Inject constructor (
        private val memberRepository: MemberRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {
        observable.value = MemberSearchViewModel.ViewState()
        preloadUniqueNamesAndCardIds()
        return observable
    }

    private fun preloadUniqueNamesAndCardIds() {
        Completable.fromAction {
            val distinctNames = memberRepository.allDistinctNames().blockingGet()
            val distinctCardIds = memberRepository.allDistinctCardIds().blockingGet()
            observable.postValue(observable.value?.copy(
                uniqueMemberNames = distinctNames,
                uniqueMemberCardIds = distinctCardIds,
                loading = false
            ))
        }.subscribeOn(Schedulers.io()).subscribe({}, {
            logger.error(it)
        })
    }

    fun updateQuery(query: String) {
        Completable.fromAction {
            observable.postValue(observable.value?.copy(loading = true))

            if (query.contains(Regex("[0-9]"))) {
                val uniqueMemberCardIds = observable.value?.uniqueMemberCardIds
                val topMatchingCardsIds = FuzzySearch.extractTop(query, uniqueMemberCardIds, 20, 60).map { it.string }
                val matchingMembers = memberRepository.byCardIds(topMatchingCardsIds).blockingGet()
                observable.postValue(observable.value?.copy(
                    matchingMembers = matchingMembers,
                    loading = false,
                    searchMethod = IdentificationEvent.SearchMethod.SEARCH_CARD_ID
                ))
            } else {
                val uniqueMemberNames = observable.value?.uniqueMemberNames
                val topMatchingNames = FuzzySearch.extractTop(query, uniqueMemberNames, 20, 60).map { it.string }
                val matchingMembers = memberRepository.byNames(topMatchingNames).blockingGet()
                observable.postValue(observable.value?.copy(
                    matchingMembers = matchingMembers,
                    loading = false,
                    searchMethod = IdentificationEvent.SearchMethod.SEARCH_NAME
                ))
            }
        }.doOnError {
            logger.error(it)
        }.subscribeOn(Schedulers.computation()).subscribe {}
    }

    fun searchMethod() = observable.value!!.searchMethod

    data class ViewState(
        val matchingMembers: List<MemberWithIdEventAndThumbnailPhoto> = emptyList(),
        var uniqueMemberNames: List<String> = emptyList(),
        var uniqueMemberCardIds: List<String> = emptyList(),
        var searchMethod: IdentificationEvent.SearchMethod? = null,
        val loading: Boolean = true
    )
}
