package org.watsi.uhp.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import org.watsi.domain.relations.MemberWithIdEventAndThumbnailPhoto
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoader

class MemberAdapter(context: Context,
                    memberList: List<MemberWithIdEventAndThumbnailPhoto>,
                    private val showClinicNumber: Boolean
) : ArrayAdapter<MemberWithIdEventAndThumbnailPhoto>(context, R.layout.item_member_list, memberList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: (context as Activity).layoutInflater.inflate(
                R.layout.item_member_list, parent, false)
        val viewHolder = if (convertView == null) {
            val holder = ViewHolder()
            holder.name = view!!.findViewById(R.id.member_name)
            holder.age_and_gender = view.findViewById(R.id.member_age_and_gender)
            holder.card_id = view.findViewById(R.id.member_card_id)
            holder.phone_number = view.findViewById(R.id.member_phone_number)
            holder.photo = view.findViewById(R.id.member_photo)
            if (showClinicNumber) {
                holder.clinic_number = view.findViewById(R.id.member_clinic_number)
            }

            view.tag = holder
            holder
        } else {
            convertView.tag as ViewHolder
        }

        getItem(position)?.let { memberWithIdEventAndPhoto ->
            val member = memberWithIdEventAndPhoto.member
            viewHolder.name?.text = member.name
            viewHolder.age_and_gender?.text = member.gender.toString()
            viewHolder.card_id?.text = member.cardId
            viewHolder.phone_number?.text = member.formattedPhoneNumber()
            if (showClinicNumber) {
                val idEvent = memberWithIdEventAndPhoto.identificationEvent

                viewHolder.phone_number?.visibility = View.GONE
                viewHolder.clinic_number?.visibility = View.VISIBLE
                viewHolder.clinic_number?.text = idEvent?.clinicNumber.toString()
            }

            viewHolder.photo?.let { imageView ->
                memberWithIdEventAndPhoto.thumbnailPhoto?.let {
                    PhotoLoader.loadMemberPhoto(it.bytes, imageView, context)
                }
            }
        }

        return view
    }

    private class ViewHolder {
        internal var name: TextView? = null
        internal var age_and_gender: TextView? = null
        internal var card_id: TextView? = null
        internal var phone_number: TextView? = null
        internal var clinic_number: TextView? = null
        internal var photo: ImageView? = null
    }
}
