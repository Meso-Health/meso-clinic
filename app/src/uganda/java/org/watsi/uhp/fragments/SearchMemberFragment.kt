package org.watsi.uhp.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.uganda.fragment_member_search.loading_indicator
import kotlinx.android.synthetic.uganda.fragment_member_search.member_search_results
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchMemberViewModel
import javax.inject.Inject

class SearchMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock

    lateinit var viewModel: SearchMemberViewModel
    lateinit var memberAdapter: MemberAdapter
    private val searchResults = mutableListOf<MemberWithIdEventAndThumbnailPhoto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        memberAdapter = MemberAdapter(
            members = searchResults,
            onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                val searchMethod = viewModel.searchMethod()

                navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchMethod(
                    memberRelation.member,
                    searchMethod))
            }
        )

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchMemberViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                searchResults.clear()
                searchResults.addAll(viewState.matchingMembers)
                member_search_results.adapter.notifyDataSetChanged()

                if (viewState.loading) {
                    member_search_results.visibility = View.GONE
                    loading_indicator.visibility = View.VISIBLE
                } else {
                    member_search_results.visibility = View.VISIBLE
                    loading_indicator.visibility = View.GONE
                }
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.search_fragment_label), null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(member_search_results, memberAdapter, context, false)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                val member = data?.getSerializableExtra(SearchByMemberCardActivity.MEMBER_RESULT_KEY) as Member?
                if (member != null) {
                    navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchMethod(
                            member, IdentificationEvent.SearchMethod.SCAN_BARCODE))
                } else {
                    logger.error("QRCodeActivity returned null member with resultCode: Activity.RESULT_OK")
                }
            }
            SearchByMemberCardActivity.RESULT_REDIRECT_TO_SEARCH_FRAGMENT -> { }
            Activity.RESULT_CANCELED -> { }
            else -> {
                logger.error("QrCodeActivity.parseResult called with resultCode: $resultCode")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as ClinicActivity).resetToolbarMinimal()
    }
}
