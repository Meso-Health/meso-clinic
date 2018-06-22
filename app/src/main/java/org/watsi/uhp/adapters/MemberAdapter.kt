package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.threeten.bp.Clock
import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.views.MemberListItem

class MemberAdapter(
        private val members: MutableList<MemberWithIdEventAndThumbnailPhoto> = mutableListOf(),
        private val onItemSelect: (memberRelation: MemberWithIdEventAndThumbnailPhoto) -> Unit,
        private val clock: Clock
) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    lateinit var memberListItemView: MemberListItem

    override fun getItemCount(): Int = members.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.view_member_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memberRelation = members[position]
        memberListItemView = holder.itemView as MemberListItem
        memberListItemView.setMember(memberRelation, clock)
        memberListItemView.setOnClickListener{
            onItemSelect(memberRelation)
        }
    }

    fun setMembers(updatedMembers: List<MemberWithIdEventAndThumbnailPhoto>) {
        if (updatedMembers != members) {
            members.clear()
            members.addAll(updatedMembers)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
