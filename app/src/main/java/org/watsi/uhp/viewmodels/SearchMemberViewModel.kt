package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.helpers.QueryHelper
import javax.inject.Inject

class SearchMemberViewModel @Inject constructor (
        private val memberRepository: MemberRepository,
        private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<List<MemberWithIdEventAndThumbnailPhoto>>()
    private var members: List<Member> = emptyList()
    private var memberNames: List<String> = emptyList()

    init {
        observable.value = emptyList()
        // TODO: check performance consequence of storing all members
        memberRepository.all().subscribe({
            members = it
            memberNames = it.map { it.name }.distinct()
        }, {
            logger.error(it)
        })
    }

    fun getObservable(): LiveData<List<MemberWithIdEventAndThumbnailPhoto>> = observable

    fun updateQuery(query: String) {
        if (QueryHelper.isSearchById(query)) {
            members.filter { it.cardId?.contains(query) == true }.sortedBy { it.cardId }.let {
                memberRepository.byIds(it.map { it.id }).subscribe({
                    observable.postValue(it)
                }, {
                    logger.error(it)
                })
            }
        } else {
            val topMatchingNames = FuzzySearch.extractTop(query, memberNames, 20, 60)
                    .map { it.string }
            members.filter { topMatchingNames.contains(it.name) }.sortedBy { it.name }.let {
                memberRepository.byIds(it.map { it.id }).subscribe({
                    observable.postValue(it)
                }, {
                    logger.error(it)
                })
            }
        }
    }
}
