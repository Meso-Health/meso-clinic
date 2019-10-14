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

class SearchMemberViewModel @Inject constructor (
        private val memberRepository: MemberRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {
        observable.value = SearchMemberViewModel.ViewState()
        preloadUniqueMemberNames()
        preloadUniqueMemberIds()
        return observable
    }

    private fun preloadUniqueMemberNames() {
        memberRepository.allDistinctNames().subscribe({ memberNames ->
            observable.postValue(observable.value?.copy(
                uniqueMemberNames = memberNames,
                loading = false
            ))
        }, {
            logger.error(it)
        })
    }

    private fun preloadUniqueMemberIds() {
        memberRepository.allDistinctIds().subscribe({ memberCardIds ->
            observable.postValue(observable.value?.copy(
                uniqueMemberCardIds = memberCardIds,
                loading = false
            ))
        }, {
            logger.error(it)
        })
    }

    fun updateQuery(query: String) {
        Completable.fromAction {
            observable.postValue(observable.value?.copy(loading = true))
            if (observable.value?.searchMethod == IdentificationEvent.SearchMethod.SEARCH_CARD_ID) {
                val members = observable.value?.uniqueMemberCardIds
                val topMatchingCardsIds = FuzzySearch.extractTop(query, members, 20, 60).map { it.string }
                memberRepository.byCardIds(topMatchingCardsIds).blockingGet()
            } else {
                val namesStartingWithSameCharacter = observable.value?.uniqueMemberNames
                val topMatchingNames = FuzzySearch.extractTop(query, namesStartingWithSameCharacter, 20, 60).map { it.string }
                val matchingMembers = memberRepository.byNames(topMatchingNames).blockingGet()
                observable.postValue(observable.value?.copy(
                    matchingMembers = matchingMembers,
                    loading = false
                ))
            }/*.doOnError {
                logger.error(it)
            }.subscribeOn(Schedulers.computation()).subscribe {}*/
        }
    }

    fun searchMethod() = observable.value?.searchMethod

    data class ViewState(
        val matchingMembers: List<MemberWithIdEventAndThumbnailPhoto> = emptyList(),
        var uniqueMemberNames: List<String> = emptyList(),
        var uniqueMemberCardIds: List<String> = emptyList(),
        var searchMethod: IdentificationEvent.SearchMethod? = null,
        val loading: Boolean = true
    )
}
