package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.empty_member_search_results
import kotlinx.android.synthetic.main.fragment_member_search.loading_indicator
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.MemberSearchViewModel
import javax.inject.Inject

class MemberSearchFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    private val searchResults = mutableListOf<MemberWithIdEventAndThumbnailPhoto>()
    private lateinit var memberAdapter: MemberAdapter
    lateinit var viewModel: MemberSearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memberAdapter = MemberAdapter(
            members = searchResults,
            onItemSelect = {
                val searchMethod = viewModel.searchMethod()
                if (searchMethod != null) {
                    navigationManager.goTo(HouseholdFragment.forParams(
                        it.member.householdId!!, searchMethod)
                    )
                } else {
                    logger.error("View model does not have a searchMethod when MemberAdapter.onItemSelect was called. ViewModel: ${viewModel.getObservable().value.toString()}")
                }
            }
        )
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MemberSearchViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                val sortedMembers =
                    MemberWithIdEventAndThumbnailPhoto.asSortedListWithHeadOfHouseholdsFirst(
                        viewState.matchingMembers
                    )
                searchResults.clear()
                searchResults.addAll(sortedMembers)
                member_search_results.adapter.notifyDataSetChanged()

                if (viewState.loading) {
                    member_search_results.visibility = View.GONE
                    loading_indicator.visibility = View.VISIBLE
                    empty_member_search_results.visibility = View.GONE
                } else {
                    // If search has finished and there are no matching members...
                    if (viewState.matchingMembers.isEmpty() && viewState.searchMethod != null) {
                        member_search_results.visibility = View.GONE
                        empty_member_search_results.visibility = View.VISIBLE
                    } else {
                        // If search is finished and there are matching members...
                        member_search_results.visibility = View.VISIBLE
                        empty_member_search_results.visibility = View.GONE
                    }

                    // Regardless of results, we should hide the loading indicator.
                    loading_indicator.visibility = View.GONE
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.blank), 0)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(
            recyclerView = member_search_results,
            adapter = memberAdapter,
            context = activity,
            nestedScrollingEnabled = false
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.member_search, menu)

        // auto-expand the SearchView in the Toolbar
        val searchItem = menu.findItem(R.id.search_member_name)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(query: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // need to include clearFocus() or else back button will only unfocus SearchView
                searchView.clearFocus()
                viewModel.updateQuery(query.toLowerCase())
                return false
            }
        })

        // auto-focus the SearchView
        searchView.isIconified = false
        searchView.requestFocus()
    }
}
