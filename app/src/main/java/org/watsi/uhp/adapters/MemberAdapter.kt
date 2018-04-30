package org.watsi.uhp.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.helpers.PhotoLoaderHelper

class MemberAdapter(context: Context,
                    memberList: List<Member>,
                    private val photoLoaderHelper: PhotoLoaderHelper,
                    private val showClinicNumber: Boolean
) : ArrayAdapter<Member>(context, R.layout.item_member_list, memberList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            val layoutInflater = (context as Activity).layoutInflater
            convertView = layoutInflater.inflate(R.layout.item_member_list, parent, false)

            viewHolder = ViewHolder()
            viewHolder.name = convertView!!.findViewById(R.id.member_name)
            viewHolder.age_and_gender = convertView.findViewById(R.id.member_age_and_gender)
            viewHolder.card_id = convertView.findViewById(R.id.member_card_id)
            viewHolder.phone_number = convertView.findViewById(R.id.member_phone_number)
            viewHolder.photo = convertView.findViewById(R.id.member_photo)
            if (showClinicNumber) {
                viewHolder.clinic_number = convertView.findViewById(R.id.member_clinic_number)
            }

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        val member = getItem(position)

        if (member != null) {
            // TODO: clean up formatting
            viewHolder.name?.text = member.name
            viewHolder.age_and_gender?.text = member.gender.toString()
            viewHolder.card_id?.text = member.cardId
            viewHolder.phone_number?.text = member.formattedPhoneNumber()
            if (showClinicNumber) {
                viewHolder.phone_number?.visibility = View.GONE
                viewHolder.clinic_number?.visibility = View.VISIBLE

                // TODO: figure out efficient way to get clinic number
//                viewHolder.clinic_number?.text = currentCheckIn.clinicNumber.toString()
            }

            // TODO: un-comment when we fix photo handling
//            photoLoaderHelper.loadMemberPhoto(member, viewHolder.photo!!, R.dimen.item_member_list_photo_width, R.dimen.item_member_list_photo_height)
        }

        return convertView
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
