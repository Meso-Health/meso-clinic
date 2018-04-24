package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_enroll_newborn.name
import kotlinx.android.synthetic.main.fragment_enroll_newborn.save_button
import kotlinx.android.synthetic.main.fragment_enroll_newborn.scan_card
import org.threeten.bp.LocalDate

import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import java.util.UUID
import javax.inject.Inject

class EnrollNewbornInfoFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager

    lateinit var parent: Member
    private var cardId: String? = null

    companion object {
        const val PARAM_MEMBER = "member"

        fun forParent(member: Member): EnrollNewbornInfoFragment {
            val fragment = EnrollNewbornInfoFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parent = arguments.getSerializable(PARAM_MEMBER) as Member
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.enroll_newborn_info_label)
        return inflater?.inflate(R.layout.fragment_enroll_newborn, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        scan_card.setOnClickListener {
            navigationManager.goTo(BarcodeFragment())
        }

        save_button.setOnClickListener {
            val gender = Member.Gender.F
            val birthdate = LocalDate.now()
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
                                 phoneNumber = null)
            // TODO: navigate to photo fragment
        }
    }
}
