package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.threeten.bp.Clock
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.R.layout.item_claim_list
import org.watsi.uhp.views.ClaimListItem

class ClaimListItemAdapter(
    private val clock: Clock,
    private val claims: MutableList<EncounterWithExtras> = mutableListOf(),
    private val onClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit,
    private val onCheck: ((encounterRelation: EncounterWithExtras) -> Unit)? = null
) : RecyclerView.Adapter<ClaimListItemAdapter.ViewHolder>() {

    private val selectedClaims: MutableList<EncounterWithExtras> = mutableListOf()

    override fun getItemCount(): Int = claims.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            item_claim_list, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val claim = claims[position]
        val view = holder.itemView as ClaimListItem
        val isSelected = selectedClaims.contains(claim)
        view.setClaim(claim, onClaimSelected, isSelected, onCheck, clock)
    }

    fun setClaims(
        visibleClaims: List<EncounterWithExtras>,
        updatedSelectedClaims: List<EncounterWithExtras> = selectedClaims
    ) {
        var shouldUpdate = false
        if (selectedClaims != updatedSelectedClaims) {
            selectedClaims.clear()
            selectedClaims.addAll(updatedSelectedClaims)
            shouldUpdate = true
        }
        if (visibleClaims != claims) {
            claims.clear()
            claims.addAll(visibleClaims)
            shouldUpdate = true
        }
        if (shouldUpdate) {
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
