package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_enrollment_contact_info.phone_number
import kotlinx.android.synthetic.main.fragment_enrollment_contact_info.save_button

import org.watsi.domain.entities.Member
import org.watsi.uhp.R

class EnrollmentContactInfoFragment : DaggerFragment() {

    lateinit var member: Member

    companion object {
        const val PARAM_MEMBER = "member"

        fun forMember(member: Member): EnrollmentContactInfoFragment {
            val fragment = EnrollmentContactInfoFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        member = arguments.getSerializable(PARAM_MEMBER) as Member
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enrollment_contact_info_label)
        return inflater?.inflate(R.layout.fragment_enrollment_contact_info, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        save_button.setOnClickListener {
            val phoneNumber = phone_number.text.toString()

            if (phoneNumber.isNotBlank() && !Member.validPhoneNumber(phoneNumber)) {
                phone_number.error = getString(R.string.phone_number_validation_error)
            } else {
                if (phoneNumber.isBlank()) {
                    // TODO: navigate to enroll fingerprint fragment with null phone number
                } else {
                    // TODO: navigate to enroll fingerprint fragment with phone number
                }
            }
        }
    }
}
