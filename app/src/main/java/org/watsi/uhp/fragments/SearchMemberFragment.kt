package org.watsi.uhp.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_search.member_search_results
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
                clock = clock
        )
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbarMinimal(null)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_search, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        RecyclerViewHelper.setRecyclerView(member_search_results, memberAdapter, context)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu?.let {
            val searchMenuItem = menu.findItem(R.id.menu_member_search)
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
