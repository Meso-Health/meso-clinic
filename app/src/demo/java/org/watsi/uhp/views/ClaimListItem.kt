package org.watsi.uhp.views

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.demo.item_claim_list.view.checkbox
import kotlinx.android.synthetic.demo.item_claim_list.view.claim_cbhi
import kotlinx.android.synthetic.demo.item_claim_list.view.claim_id
import kotlinx.android.synthetic.demo.item_claim_list.view.claim_price
import kotlinx.android.synthetic.demo.item_claim_list.view.medical_record_number
import kotlinx.android.synthetic.demo.item_claim_list.view.member_name
import org.threeten.bp.Clock
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.R
import org.watsi.uhp.utils.CurrencyUtil
import javax.inject.Inject

class ClaimListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setClaim(
        encounterRelation: EncounterWithExtras,
        onClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit,
        isSelected: Boolean,
        onCheck: ((encounterRelation: EncounterWithExtras) -> Unit)?,
        clock: Clock
    ) {
        medical_record_number.text = encounterRelation.member.medicalRecordNumber
        member_name.text = encounterRelation.member.name
        claim_cbhi.text = encounterRelation.member.membershipNumber
        claim_id.text = encounterRelation.encounter.shortenedClaimId()
        claim_price.text = CurrencyUtil.formatMoneyWithCurrency(context, encounterRelation.price())

        when (encounterRelation.member.memberStatus(clock)) {
            Member.MembershipStatus.ACTIVE -> { /* Do nothing */ }
            Member.MembershipStatus.EXPIRED, Member.MembershipStatus.DELETED -> {
                this.setBackgroundColor(resources.getColor(R.color.inactiveBackgroundRed))
                medical_record_number.setTextColor(resources.getColor(R.color.inactiveTextRed))
                member_name.setTextColor(resources.getColor(R.color.inactiveTextRed))
                claim_cbhi.setTextColor(resources.getColor(R.color.inactiveTextRed))
                claim_id.setTextColor(resources.getColor(R.color.inactiveTextRed))
                claim_price.setTextColor(resources.getColor(R.color.inactiveTextRed))
            }
            Member.MembershipStatus.UNKNOWN -> {
                this.setBackgroundColor(resources.getColor(R.color.unknownBackgroundGray))
                medical_record_number.setTextColor(resources.getColor(R.color.unknownTextGray))
                member_name.setTextColor(resources.getColor(R.color.unknownTextGray))
                claim_cbhi.setTextColor(resources.getColor(R.color.unknownTextGray))
                claim_id.setTextColor(resources.getColor(R.color.unknownTextGray))
                claim_price.setTextColor(resources.getColor(R.color.unknownTextGray))
            }
        }

        setOnClickListener {
            onClaimSelected(encounterRelation)
        }

        onCheck?.let {
            checkbox.visibility = View.VISIBLE
            checkbox.isChecked = isSelected
            checkbox.setOnTouchListener { _, event ->
                // intercept touch event so it does not trigger the normal check action
                // because we want to manage the checked state via the ViewState
                if (event.action == MotionEvent.ACTION_DOWN) {
                    onCheck(encounterRelation)
                    true
                } else {
                    false
                }
            }
        }
    }
}
