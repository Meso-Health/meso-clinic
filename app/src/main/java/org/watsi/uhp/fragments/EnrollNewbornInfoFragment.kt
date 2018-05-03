package org.watsi.uhp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_enroll_newborn.card_id
import kotlinx.android.synthetic.main.fragment_enroll_newborn.name
import kotlinx.android.synthetic.main.fragment_enroll_newborn.save_button
import kotlinx.android.synthetic.main.fragment_enroll_newborn.scan_card
import org.threeten.bp.LocalDate
import org.watsi.device.managers.Logger

import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.activities.ScanNewCardActivity
import org.watsi.uhp.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class EnrollNewbornInfoFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var logger: Logger

    companion object {
        const val PARAM_MEMBER = "member"
        const val SCAN_CARD_INTENT = 1

        fun forParent(member: Member): EnrollNewbornInfoFragment {
            val fragment = EnrollNewbornInfoFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enroll_newborn_info_label)
        return inflater?.inflate(R.layout.fragment_enroll_newborn, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        scan_card.setOnClickListener {
            startActivityForResult(Intent(activity, ScanNewCardActivity::class.java), SCAN_CARD_INTENT)
        }

        save_button.setOnClickListener {
            val parent = arguments.getSerializable(PARAM_MEMBER) as Member
            val gender = Member.Gender.F
            val birthdate = LocalDate.now()
            val cardId = card_id.text.toString()
            val newborn = Member(id = UUID.randomUUID(),
                                 photoId = null,
                                 householdId = parent.householdId,
                                 thumbnailPhotoId = null,
                                 cardId = cardId,
                                 name = name.text.toString(),
                                 gender = gender,
                                 birthdate = birthdate,
                                 birthdateAccuracy = Member.DateAccuracy.D,
                                 fingerprintsGuid = null,
                                 phoneNumber = null,
                                 photoUrl = null)
            navigationManager.goTo(EnrollNewbornPhotoFragment.forNewborn(newborn))
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
