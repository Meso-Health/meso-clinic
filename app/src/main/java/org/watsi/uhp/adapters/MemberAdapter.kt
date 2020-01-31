package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.views.MemberListItem

class MemberAdapter(
    private val members: MutableList<MemberWithIdEventAndThumbnailPhoto> = mutableListOf(),
    private val onItemSelect: ((member: MemberWithIdEventAndThumbnailPhoto) -> Unit)? = null
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    override fun getItemCount(): Int = members.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val memberView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_member_list_item, parent, false)
        return MemberViewHolder(memberView)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val memberWithPhoto = members[position]
        val householdMemberListItemView = holder.itemView as MemberListItem
        householdMemberListItemView.setMember(memberWithPhoto)
        onItemSelect?.let { handler ->
            householdMemberListItemView.setOnClickListener {
                handler(memberWithPhoto)
            }
        }
    }

    fun setMembers(updatedMembers: List<MemberWithIdEventAndThumbnailPhoto>) {
        if (updatedMembers != members) {
            members.clear()
            members.addAll(updatedMembers)
            notifyDataSetChanged()
        }
    }

    class MemberViewHolder(memberView: View) : RecyclerView.ViewHolder(memberView)
}
