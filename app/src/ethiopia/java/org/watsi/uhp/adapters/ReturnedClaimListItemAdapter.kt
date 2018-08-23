package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.uhp.R.layout.item_returned_claim_list
import org.watsi.uhp.views.ReturnedClaimListItem

class ReturnedClaimListItemAdapter(
    private val returnedClaims: MutableList<EncounterWithExtras> = mutableListOf(),
    private val onReturnedClaimSelected: (encounterRelation: EncounterWithExtras) -> Unit
) : RecyclerView.Adapter<ReturnedClaimListItemAdapter.ViewHolder>() {

    override fun getItemCount(): Int = returnedClaims.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            item_returned_claim_list, parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val returnedClaim = returnedClaims[position]
        val view = holder.itemView as ReturnedClaimListItem
        view.setReturnedClaim(returnedClaim, onReturnedClaimSelected)
    }

    fun setReturnedClaimsItems(updatedReturnedClaims: List<EncounterWithExtras>) {
        if (updatedReturnedClaims != returnedClaims) {
            returnedClaims.clear()
            returnedClaims.addAll(updatedReturnedClaims)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
