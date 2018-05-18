package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class SearchMemberViewModel @Inject constructor (
        private val memberRepository: MemberRepository
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()
    private var members: List<Member> = emptyList()
    private var memberNames: List<String> = emptyList()

    init {
        observable.value = ViewState()
        // TODO: check performance consequence of storing all members
        memberRepository.all().subscribe({
            members = it
            memberNames = it.map { it.name }.distinct()
        }, {
            // TODO: handle error
        })
    }

    fun getObservable(): LiveData<ViewState> = observable

    fun updateQuery(query: String) {
        if (query.matches(Regex(".*\\d+.*"))) {
            members.filter { it.cardId?.contains(query) == true }.sortedBy { it.cardId }.let {
                memberRepository.byIds(it.map { it.id }).subscribe({
                    observable.value = observable.value?.copy(searchResults = it)
                }, {
                    // TODO: handle error
                })
            }
        } else {
            val topMatchingNames = FuzzySearch.extractTop(query, memberNames, 20, 60)
                    .map { it.string }
            members.filter { topMatchingNames.contains(it.name) }.sortedBy { it.name }.let {
                memberRepository.byIds(it.map { it.id }).subscribe({
                    observable.value = observable.value?.copy(searchResults = it)
                }, {
                    // TODO: handle error
                })
            }
        }
    }

    data class ViewState(val searchResults: List<MemberWithIdEventAndThumbnailPhoto> = emptyList())
}
