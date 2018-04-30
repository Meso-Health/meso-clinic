package org.watsi.uhp.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_enrollment_fingerprint.enrollment_fingerprint_capture_btn
import kotlinx.android.synthetic.main.fragment_enrollment_fingerprint.enrollment_fingerprint_failed_message
import kotlinx.android.synthetic.main.fragment_enrollment_fingerprint.enrollment_fingerprint_success_message
import kotlinx.android.synthetic.main.fragment_enrollment_fingerprint.save_button
import org.watsi.device.managers.SessionManager

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.helpers.SimprintsHelper
import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.managers.NavigationManager

import java.util.UUID

import javax.inject.Inject

class EnrollmentFingerprintFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository

    lateinit var member: Member
    lateinit var simprintsHelper: SimprintsHelper
    private var fingerprintsGuid: UUID? = null

    companion object {
        const val PARAM_MEMBER = "member"

        fun forMember(member: Member): EnrollmentFingerprintFragment {
            val fragment = EnrollmentFingerprintFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        simprintsHelper = SimprintsHelper(sessionManager.currentToken()?.user?.username, this)
        member = arguments.getSerializable(PARAM_MEMBER) as Member
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enrollment_fingerprint_label)
        return inflater?.inflate(R.layout.fragment_enrollment_fingerprint, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        enrollment_fingerprint_capture_btn.setOnClickListener {
            enrollment_fingerprint_success_message.visibility = View.GONE
            enrollment_fingerprint_failed_message.visibility = View.GONE

            try {
                simprintsHelper.enroll(BuildConfig.PROVIDER_ID.toString(), member.id.toString())
            } catch (e: SimprintsHelper.SimprintsInvalidIntentException) {
                ExceptionManager.reportException(e)
                Toast.makeText(activity, R.string.simprints_not_installed, Toast.LENGTH_LONG).show()
            }
        }

        save_button.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.enrollment_fingerprint_confirm_completion)
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                memberRepository.save(member.copy(fingerprintsGuid = fingerprintsGuid))

                identificationEventRepository.openCheckIn(member.id).subscribe({
                    navigationManager.goTo(CurrentMemberDetailFragment.forIdentificationEvent(it))
                }, {
                    // TODO: handle error
                }, {
                    navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
                })

                Toast.makeText(activity, "Enrollment completed", Toast.LENGTH_LONG).show()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            fingerprintsGuid = simprintsHelper.onActivityResultFromEnroll(requestCode, resultCode, data)
        } catch (e: SimprintsHelper.SimprintsHelperException) {
            ExceptionManager.reportException(e)
        }

        if (fingerprintsGuid != null) {
            enrollment_fingerprint_success_message.visibility = View.VISIBLE
        } else {
            enrollment_fingerprint_failed_message.visibility = View.VISIBLE
        }
    }
}
