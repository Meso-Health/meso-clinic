package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_download_household.cancel
import kotlinx.android.synthetic.ethiopia.fragment_download_household.error_view
import kotlinx.android.synthetic.ethiopia.fragment_download_household.loading_view
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.DownloadHouseholdViewModel
import java.util.UUID
import javax.inject.Inject

class DownloadHouseholdFragment : DaggerFragment() {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger
    private lateinit var viewModel: DownloadHouseholdViewModel
    private lateinit var searchMethod: IdentificationEvent.SearchMethod
    private var cardId: String? = null
    private var membershipNumber: String? = null

    companion object {
        const val PARAM_CARD_ID = "card_id"
        const val PARAM_MEMBERSHIP_NUMBER = "membership_number"
        const val PARAM_SEARCH_METHOD = "search_method"

        fun forCardId(cardId: String): DownloadHouseholdFragment {
            val downloadHouseholdFragment = DownloadHouseholdFragment()
            downloadHouseholdFragment.arguments = Bundle().apply {
                putString(PARAM_CARD_ID, cardId)
                putSerializable(PARAM_SEARCH_METHOD, IdentificationEvent.SearchMethod.SCAN_BARCODE)
            }
            return downloadHouseholdFragment
        }

        fun forMembershipNumber(membershipNumber: String): DownloadHouseholdFragment {
            val downloadHouseholdFragment = DownloadHouseholdFragment()
            downloadHouseholdFragment.arguments = Bundle().apply {
                putString(PARAM_MEMBERSHIP_NUMBER, membershipNumber)
                putSerializable(PARAM_SEARCH_METHOD, IdentificationEvent.SearchMethod.SEARCH_MEMBERSHIP_NUMBER)
            }
            return downloadHouseholdFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardId = arguments.getString(PARAM_CARD_ID)
        membershipNumber = arguments.getString(PARAM_MEMBERSHIP_NUMBER)
        searchMethod = arguments.getSerializable(HouseholdFragment.PARAM_SEARCH_METHOD) as IdentificationEvent.SearchMethod
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(
            DownloadHouseholdViewModel::class.java)

        val observable: LiveData<DownloadHouseholdViewModel.ViewState>
        val onFailure: () -> Unit
        when {
            cardId != null -> {
                observable = viewModel.getObservableByCardId(cardId!!)
                // TODO: Ideally this would also be able to launch into manually creating a member like below
                onFailure = this::showError

            }
            membershipNumber != null -> {
                observable = viewModel.getObservableByMembershipNumber(membershipNumber!!)
                onFailure = this::goToMemberNotFound
            }
            else -> {
                logger.error("Cannot download household without card id or membership number")
                return
            }
        }
        observable.observe(this, Observer {
            it?.let { viewState ->
                if (viewState.householdId != null) {
                    completeDownload(viewState.householdId, searchMethod)
                } else {
                    onFailure()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.household_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_download_household, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        showLoading()

        cancel.setOnClickListener {
            // TODO: Ideally both of these would go to member not found and manually create a member
            when {
                cardId != null -> navigationManager.goBack()
                membershipNumber != null -> goToMemberNotFound()
                else -> navigationManager.goBack()
            }
        }
    }

    private fun completeDownload(householdId: UUID, searchMethod: IdentificationEvent.SearchMethod) {
        navigationManager.popTo(SearchFragment())
        navigationManager.goTo(HouseholdFragment.forParams(householdId, searchMethod))
    }

    private fun showLoading() {
        view?.post {
            loading_view.visibility = View.VISIBLE
            error_view.visibility = View.GONE
        }
    }

    private fun showError() {
        view?.post {
            error_view.visibility = View.VISIBLE
            loading_view.visibility = View.GONE
        }
    }

    private fun goToMemberNotFound() {
        membershipNumber?.let { it
            navigationManager.popTo(SearchFragment())
            navigationManager.goTo(MemberNotFoundFragment.forMembershipNumber(it))
        }
    }
}
