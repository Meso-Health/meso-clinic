package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SearchMemberViewModel
import org.watsi.uhp.views.ToolbarSearch
import javax.inject.Inject

class SearchMemberFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var clock: Clock

    lateinit var viewModel: SearchMemberViewModel
    lateinit var memberAdapter: MemberAdapter
    lateinit var toolbarSearchView: ToolbarSearch

    companion object {
        const val SCAN_CARD_INTENT = 1
    }

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
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        memberAdapter = MemberAdapter(
                onItemSelect = { memberRelation: MemberWithIdEventAndThumbnailPhoto ->
                    val searchMethod = viewModel.searchMethod() ?: run {
                        logger.error("Search method not set")
                        IdentificationEvent.SearchMethod.SEARCH_ID
                    }

                    toolbarSearchView.clear()
                    navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchMethod(
                            memberRelation.member,
                            searchMethod))
                },
                clock = clock)

        val layoutManager = LinearLayoutManager(activity)
        member_search_results.layoutManager = layoutManager
        member_search_results.adapter = memberAdapter
        member_search_results.isNestedScrollingEnabled = false
        val listItemDivider = DividerItemDecoration(context, layoutManager.orientation)
        listItemDivider.setDrawable(resources.getDrawable(R.drawable.list_divider, null))
        member_search_results.addItemDecoration(listItemDivider)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        val searchMenuItem = menu!!.findItem(R.id.menu_member_search)
        searchMenuItem.isVisible = true

        toolbarSearchView = searchMenuItem.actionView as ToolbarSearch
        toolbarSearchView.keyboardManager = keyboardManager
        toolbarSearchView.onSearch { query ->
            viewModel.updateQuery(query)
        }

        toolbarSearchView.onBack {
            toolbarSearchView.clear()
            navigationManager.goBack()
        }

        toolbarSearchView.onScan {
            startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java), SCAN_CARD_INTENT)
        }

        keyboardManager.showKeyboard(toolbarSearchView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (member, error) = SearchByMemberCardActivity.parseResult(resultCode, data, logger)
        member?.let {
            navigationManager.goTo(CheckInMemberDetailFragment.forMemberWithSearchMethod(
                    it,
                    IdentificationEvent.SearchMethod.SCAN_BARCODE))
        }
        error?.let {
            // TODO: display error?
        }
    }
}
