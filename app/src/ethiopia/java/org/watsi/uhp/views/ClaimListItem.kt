package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.ethiopia.item_claim_list.view.claim_cbhi
import kotlinx.android.synthetic.ethiopia.item_claim_list.view.claim_id
import kotlinx.android.synthetic.ethiopia.item_claim_list.view.claim_price
import kotlinx.android.synthetic.ethiopia.item_claim_list.view.medical_record_number
import kotlinx.android.synthetic.ethiopia.item_claim_list.view.member_name
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.utils.CurrencyUtil

class ClaimListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setClaim(
        encounterRelation: EncounterWithExtras,
        onClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit
    ) {
        medical_record_number.text = encounterRelation.member.medicalRecordNumber
        member_name.text = encounterRelation.member.name
        claim_cbhi.text = encounterRelation.member.membershipNumber
        claim_id.text = encounterRelation.encounter.shortenedClaimId()
        claim_price.text = CurrencyUtil.formatMoney(encounterRelation.price())

        setOnClickListener {
            onClaimSelected(encounterRelation)
        }
    }
}
