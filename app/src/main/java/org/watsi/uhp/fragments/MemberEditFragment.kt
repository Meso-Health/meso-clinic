package org.watsi.uhp.fragments

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_member_edit.card_id
import kotlinx.android.synthetic.main.fragment_member_edit.member_name
import kotlinx.android.synthetic.main.fragment_member_edit.phone_number
import kotlinx.android.synthetic.main.fragment_member_edit.save_button
import kotlinx.android.synthetic.main.fragment_member_edit.scan_card

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class MemberEditFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    lateinit var member: Member

    companion object {
        const val PARAM_MEMBER = "member"

        fun forMember(member: Member): MemberEditFragment {
            val fragment = MemberEditFragment()
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
        activity.setTitle(R.string.member_edit_label)
        return inflater?.inflate(R.layout.fragment_member_edit, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        scan_card.setOnClickListener {
            navigationManager.goTo(BarcodeFragment.forPurpose(
                    BarcodeFragment.ScanPurpose.MEMBER_EDIT, member))
        }

        save_button.setOnClickListener {
            val updatedName = member_name.text.toString()
            val updatedPhoneNumber = phone_number.text.toString()
            val updatedCardId = card_id.text.toString()

            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.member_edit_confirmation)
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                val toastMessage = member.name + "'s information has been updated."
                memberRepository.save(member.copy(name = updatedName,
                                                  phoneNumber = updatedPhoneNumber,
                                                  cardId = updatedCardId))
                // navigate to correct member detail fragment
                Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }
}
