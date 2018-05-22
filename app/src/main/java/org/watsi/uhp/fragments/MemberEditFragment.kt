package org.watsi.uhp.fragments

import android.content.Intent
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
import org.watsi.device.managers.Logger

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.usecases.UpdateMemberUseCase
import org.watsi.uhp.R
import org.watsi.uhp.activities.ScanNewCardActivity
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class MemberEditFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var updateMemberUseCase: UpdateMemberUseCase
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository
    @Inject lateinit var logger: Logger

    lateinit var member: Member

    companion object {
        const val PARAM_MEMBER = "member"
        const val SCAN_CARD_INTENT = 1

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
        member_name.setText(member.name)
        phone_number.setText(member.phoneNumber)
        card_id.setText(member.cardId)

        scan_card.setOnClickListener {
            startActivityForResult(Intent(activity, ScanNewCardActivity::class.java), SCAN_CARD_INTENT)
        }

        save_button.setOnClickListener {
            val updatedName = member_name.text.toString()
            val updatedPhoneNumber = phone_number.text.toString()
            val updatedCardId = card_id.text.toString()

            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.member_edit_confirmation)
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                val toastMessage = "$updatedName's information has been updated."
                val updatedMember = member.copy(name = updatedName,
                                                phoneNumber = updatedPhoneNumber,
                                                cardId = updatedCardId)
                updateMemberUseCase.execute(updatedMember).subscribe({
                    identificationEventRepository.openCheckIn(member.id).subscribe({idEvent ->
                        navigationManager.popTo(CurrentMemberDetailFragment.forMemberAndIdEvent(updatedMember, idEvent))
                    }, {
                        // TODO: handle error
                    }, {
                        navigationManager.popTo(CheckInMemberDetailFragment.forMember(updatedMember))
                    })
                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                }, {
                    // TODO: handle error
                })
            }
            builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (cardId, error) = ScanNewCardActivity.parseResult(resultCode, data, logger)
        cardId?.let {
            card_id.setText(it)
        }
        error?.let {
            // TODO: display error?
        }
    }
}
