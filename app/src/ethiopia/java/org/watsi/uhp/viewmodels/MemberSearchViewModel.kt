package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

class MemberSearchViewModel @Inject constructor (
    private val memberRepository: MemberRepository,
    private val logger: Logger
) : ViewModel() {

    private val observable = MutableLiveData<List<MemberWithIdEventAndThumbnailPhoto>>()
    private var members: List<Member> = emptyList()
    private var memberNames: List<String> = emptyList()

    init {
        observable.value = emptyList()
        // TODO: monitor performance consequence of storing all members
        // TODO: make sure we only return Members with households
        memberRepository.all().subscribe({
            members = it
            memberNames = it.map { it.name }.distinct()
        }, {
            logger.error(it)
        })
    }

    fun getObservable(): LiveData<List<MemberWithIdEventAndThumbnailPhoto>> = observable

    fun updateQuery(query: String) {
        val topMatchingNames = FuzzySearch.extractTop(query, memberNames, 20, 60).map { it.string }

        // TODO: it is overkill to keep all the members in memory, we should just use the names
        // as the input into the second query
        members.filter { topMatchingNames.contains(it.name) }.sortedBy { it.name }.let {
            memberRepository.byIds(it.map { it.id }).subscribe({
                observable.postValue(it)
            }, {
                logger.error(it)
            })
        }
    }
}
