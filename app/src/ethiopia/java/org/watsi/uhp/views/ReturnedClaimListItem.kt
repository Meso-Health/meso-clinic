package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.ethiopia.item_returned_claim_list.view.claim_cbhi
import kotlinx.android.synthetic.ethiopia.item_returned_claim_list.view.claim_id
import kotlinx.android.synthetic.ethiopia.item_returned_claim_list.view.claim_price
import kotlinx.android.synthetic.ethiopia.item_returned_claim_list.view.medical_record_number
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.utils.CurrencyUtil

class ReturnedClaimListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setReturnedClaim(
        encounterRelation: EncounterWithExtras,
        onReturnedClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit
    ) {
        medical_record_number.text = encounterRelation.member.medicalRecordNumber
        claim_cbhi.text = encounterRelation.member.membershipNumber
        claim_id.text = encounterRelation.encounter.claimId
        claim_price.text = CurrencyUtil.formatMoney(encounterRelation.price())

        setOnClickListener {
            onReturnedClaimSelected(encounterRelation)
        }
    }
}
