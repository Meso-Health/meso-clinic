package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.member_no_search_results_text
import kotlinx.android.synthetic.main.fragment_member_search.member_search
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.domain.repositories.IdentificationEventRepository

import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchMemberViewModel

import javax.inject.Inject

class SearchMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    @Inject lateinit var photoRepository: PhotoRepository

    lateinit var viewModel: SearchMemberViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchMemberViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { searchResults ->
                val adapter = MemberAdapter(activity, searchResults, false)

                member_search_results.adapter = adapter
                member_search_results.requestFocus()
            }
        })
    }

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
                viewModel.updateQuery(query)
                KeyboardManager.hideKeyboard(member_search, activity)
                return true
            }
        })

        member_search_results.emptyView = member_no_search_results_text

        member_search_results.setOnItemClickListener { parent, _, position, _ ->
            val memberRelation = parent.getItemAtPosition(position) as MemberWithIdEventAndThumbnailPhoto
            val member = memberRelation.member
            identificationEventRepository.openCheckIn(member.id).subscribe({
                navigationManager.goTo(
                        CurrentMemberDetailFragment.forIdentificationEvent(it))
            }, {
                // TODO: handle error
            }, {
                navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
            })
        }
    }

    override fun onResume() {
        super.onResume()

        member_search.requestFocus()
    }
}
