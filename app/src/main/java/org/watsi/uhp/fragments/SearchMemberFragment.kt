package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.member_search
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchMemberViewModel
import javax.inject.Inject

class SearchMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var keyboardManager: KeyboardManager

    lateinit var viewModel: SearchMemberViewModel
    lateinit var memberAdapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchMemberViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { matchingMembers ->
                memberAdapter.setMembers(matchingMembers)
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
                keyboardManager.showKeyboard(searchView.findFocus())
            }
        }

        member_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String) = true

            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.updateQuery(query)
                keyboardManager.hideKeyboard(member_search)
                return true
            }
        })

        memberAdapter = MemberAdapter(
                showClinicNumber = false,
                showPhoneNumber = true,
                onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                        navigationManager.goTo(CheckInMemberDetailFragment.forMember(memberRelation.member))
                })
        member_search_results.adapter = memberAdapter
        member_search_results.layoutManager = LinearLayoutManager(activity)
        member_search_results.isNestedScrollingEnabled = false
    }

    override fun onResume() {
        super.onResume()

        member_search.requestFocus()
    }
}
