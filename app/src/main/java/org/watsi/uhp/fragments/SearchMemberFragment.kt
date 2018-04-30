package org.watsi.uhp.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.member_no_search_results_text
import kotlinx.android.synthetic.main.fragment_member_search.member_search
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository

import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.PhotoLoaderHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class SearchMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    @Inject lateinit var photoRepository: PhotoRepository

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.member_search_fragment_label)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        member_search.setOnQueryTextFocusChangeListener { searchView, hasFocus ->
            if (hasFocus) {
                // for SearchViews, in order to properly show the search keyboard, we need to
                // use findFocus() to grab and pass a view *inside* of the SearchView
                KeyboardManager.showKeyboard(searchView.findFocus(), activity)
            }
        }

        member_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String) = true

            override fun onQueryTextSubmit(query: String): Boolean {
                SearchMembersTask().execute(query)
                KeyboardManager.hideKeyboard(member_search, activity)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()

        member_search.requestFocus()
    }

    inner class SearchMembersTask : AsyncTask<String, Void, Pair<IdentificationEvent.SearchMethod, List<Member>>>() {

        override fun onPreExecute() {
            // TODO: show progress dialog
        }

        override fun doInBackground(vararg params: String): Pair<IdentificationEvent.SearchMethod, List<Member>> {
            val query = params[0]
            return if (query.matches(Regex(".*\\d+.*"))) {
                Pair(IdentificationEvent.SearchMethod.SEARCH_ID, memberRepository.fuzzySearchByCardId(query))
            } else {
                Pair(IdentificationEvent.SearchMethod.SEARCH_NAME, memberRepository.fuzzySearchByName(query))
            }
        }

        override fun onPostExecute(pair: Pair<IdentificationEvent.SearchMethod, List<Member>>) {
            val photoLoaderHelper = PhotoLoaderHelper(activity, photoRepository)
            val adapter = MemberAdapter(activity, pair.second, photoLoaderHelper, false)

            member_search_results.adapter = adapter
            member_search_results.emptyView = member_no_search_results_text
            member_search_results.setOnItemClickListener { parent, _, position, _ ->
                val member = parent.getItemAtPosition(position) as Member
                identificationEventRepository.openCheckIn(member.id).subscribe({
                    if (it != null) {
                        navigationManager.goTo(
                                CurrentMemberDetailFragment.forIdentificationEvent(it))
                    } else {
                        navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
                    }
                }, {
                    // TODO: handle error
                })
            }
            // TODO: dismiss ProgressDialog
            member_search_results.requestFocus()
        }
    }
}
