package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.R.layout.item_claim_list
import org.watsi.uhp.views.ClaimListItem

class ClaimListItemAdapter(
    private val claims: MutableList<EncounterWithExtras> = mutableListOf(),
    private val onClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit
) : RecyclerView.Adapter<ClaimListItemAdapter.ViewHolder>() {

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
        view.setClaim(claim, onClaimSelected)
    }

    fun setClaims(updatedClaims: List<EncounterWithExtras>) {
        if (updatedClaims != claims) {
            claims.clear()
            claims.addAll(updatedClaims)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
