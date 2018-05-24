package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.views.MemberListItem

class MemberAdapter(
        private val showClinicNumber: Boolean,
        private val showPhoneNumber: Boolean,
        private val onItemSelect: (memberRelation: MemberWithIdEventAndThumbnailPhoto) -> Unit,
        private val members: MutableList<MemberWithIdEventAndThumbnailPhoto> = mutableListOf()
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {
    override fun getItemCount(): Int = members.size

    lateinit var memberListItemView: MemberListItem

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val memberRelation = members[position]
        memberListItemView = holder.itemView as MemberListItem
        memberListItemView.setMemberRelation(memberRelation, showClinicNumber, showPhoneNumber)
        memberListItemView.setOnClickListener{
            onItemSelect(memberRelation)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val memberView = LayoutInflater.from(parent.context).inflate(
                R.layout.item_member_list, parent, false)
        return MemberViewHolder(memberView)
    }

    fun setMembers(updatedMembers: List<MemberWithIdEventAndThumbnailPhoto>) {
        members.clear()
        members.addAll(updatedMembers)
        notifyDataSetChanged()
    }

    class MemberViewHolder(memberView: View) : RecyclerView.ViewHolder(memberView)
}
