package org.watsi.uhp.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.demo.fragment_search_with_membership_number.membership_number
import kotlinx.android.synthetic.demo.fragment_search_with_membership_number.membership_number_button
import kotlinx.android.synthetic.demo.fragment_search_with_membership_number.scan_card_button
import kotlinx.android.synthetic.demo.fragment_search_with_membership_number.search_button
import kotlinx.android.synthetic.demo.fragment_search_with_membership_number.search_by_name_button
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.QrCodeActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class MemberSearchWithMembershipNumberFragment : DaggerFragment() {
    @Inject lateinit var findHouseholdByMembershipNumberUseCase: FindHouseholdIdByMembershipNumberUseCase
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger

    companion object {
        const val SEARCH_HOUSEHOLD_BY_CARD_INTENT = 1
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.search), 0)
        return inflater?.inflate(R.layout.fragment_search_with_membership_number, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        search_button.setOnClickListener {
            val membershipNumber = membership_number.text.toString()
            // Only execute use case if membership number is not blank.
            if (!membershipNumber.isBlank()) {
                findHouseholdByMembershipNumberUseCase.execute(membershipNumber).subscribe({ householdId ->
                    navigationManager.goTo(HouseholdFragment.forParams(householdId, IdentificationEvent.SearchMethod.SEARCH_MEMBERSHIP_NUMBER))
                }, { err ->
                    logger.error(err)
                    view?.let {
                        SnackbarHelper.showError(it, context, err.localizedMessage)
                    }
                }, {
                    navigationManager.goTo(MemberNotFoundFragment.forMembershipNumber(membershipNumber))
                })
            }
        }

        membership_number_button.setTextColor(context.getColor(R.color.blue4))
        membership_number_button.compoundDrawableTintList = context.getColorStateList(R.color.blue4)

        scan_card_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java),
                SEARCH_HOUSEHOLD_BY_CARD_INTENT)
        }

        search_by_name_button.setOnClickListener {
            navigationManager.goTo(MemberSearchFragment())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SEARCH_HOUSEHOLD_BY_CARD_INTENT) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val householdId = it.getSerializableExtra(SearchByMemberCardActivity.MEMBER_RESULT_KEY) as UUID
                        navigationManager.goTo(HouseholdFragment.forParams(householdId, IdentificationEvent.SearchMethod.SCAN_BARCODE))
                    }
                }

                Activity.RESULT_CANCELED -> { } // Do nothing, but don't trigger an error

                QrCodeActivity.RESULT_BARCODE_DETECTOR_NOT_OPERATIONAL -> {
                    logger.error("Barcode detector not operational")
                }
                else -> {
                    logger.error("Unknown result code ($resultCode) from SearchHouseholdByCardActivity")
                }
            }
        }
    }
}

