package org.watsi.uhp.fragments

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Tier
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_detail.absentee_notification
import kotlinx.android.synthetic.main.fragment_member_detail.member_action_button
import kotlinx.android.synthetic.main.fragment_member_detail.replace_card_notification
import kotlinx.android.synthetic.main.fragment_member_detail.scan_fingerprints_btn
import kotlinx.android.synthetic.main.fragment_member_detail.scan_result

import org.watsi.domain.entities.Member
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.helpers.SimprintsHelper
import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class CheckInMemberDetailFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var simprintsHelper: SimprintsHelper

    lateinit var member: Member

    companion object {
        const val PARAM_MEMBER = "member"

        fun forMember(member: Member): CheckInMemberDetailFragment {
            val fragment = CheckInMemberDetailFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        member = arguments.getSerializable(PARAM_MEMBER) as Member
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.detail_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_member_detail, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (member.isAbsentee()) {
            absentee_notification.visibility = View.VISIBLE
            absentee_notification.setOnActionClickListener {
                // TODO: navigate to complete enrollment fragment
            }
        }
        if (member.cardId == null) {
            replace_card_notification.visibility = View.VISIBLE
            replace_card_notification.setOnClickListener {
                navigationManager.goTo(MemberEditFragment.forMember(member))
            }
        }

        // TODO: need to set fragment views with member information

        member_action_button.text = getString(R.string.check_in)
        member_action_button.setOnClickListener {
            // TODO: open clinic number dialog
            // completing clinic number dialog should persist IdentificationEvent and navigate
            // back to the CurrentPatientsFragment
        }

        if (member.fingerprintsGuid != null) {
            scan_fingerprints_btn.visibility = View.VISIBLE
            scan_fingerprints_btn.setOnClickListener {
                try {
                    simprintsHelper.verify(BuildConfig.PROVIDER_ID.toString(), member.fingerprintsGuid)
                } catch (e: SimprintsHelper.SimprintsInvalidIntentException) {
                    Toast.makeText(context, R.string.simprints_not_installed, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            val verification = simprintsHelper.onActivityResultFromVerify(requestCode, resultCode, data)
            if (verification != null) {
                val fingerprintTier = verification.tier
                val fingerprintConfidence = verification.confidence

                // TODO: should store the verification details so they can be persisted in the IdentificationEvent
                if (fingerprintTier == Tier.TIER_5) {
                    setScanResultProperties(ContextCompat.getColor(context, R.color.indicatorRed), R.string.bad_scan_indicator)
                } else {
                    setScanResultProperties(ContextCompat.getColor(context, R.color.indicatorGreen), R.string.good_scan_indicator)
                }
                Toast.makeText(context, R.string.fingerprint_scan_successful, Toast.LENGTH_LONG).show()
                return
            } else {
                Toast.makeText(context, R.string.fingerprint_scan_failed, Toast.LENGTH_LONG).show()
            }
        } catch (e: SimprintsHelper.SimprintsHelperException) {
            ExceptionManager.reportException(e)
            Toast.makeText(context, R.string.fingerprint_scan_failed, Toast.LENGTH_LONG).show()
        }

        if (resultCode != Constants.SIMPRINTS_CANCELLED) {
            setScanResultProperties(ContextCompat.getColor(context, R.color.indicatorNeutral), R.string.no_scan_indicator)
        }
    }

    private fun setScanResultProperties(color: Int, textId: Int) {
        scan_result.invalidate()
        scan_result.setText(textId)
        scan_result.setTextColor(color)
        val border = scan_result.background as GradientDrawable
        border.setStroke(2, color)
        val fingerprintIcon = scan_result.compoundDrawables[0] as VectorDrawable
        //mutate() allows us to modify only this instance of the drawable without affecting others
        fingerprintIcon.mutate().setTint(color)

        scan_fingerprints_btn.visibility = View.GONE
        scan_result.visibility = View.VISIBLE
    }
}
