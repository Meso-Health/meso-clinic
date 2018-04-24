package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients_label
import kotlinx.android.synthetic.main.fragment_current_patients.identification_button

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.NavigationManager

import javax.inject.Inject

class CurrentPatientsFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.current_patients_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_current_patients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        val checkedInMembers = memberRepository.checkedInMembers()

        if (checkedInMembers.isEmpty()) {
            current_patients_label.visibility = View.GONE
        } else {
            current_patients_label.text = activity.resources.getQuantityString(
                    R.plurals.current_patients_label, checkedInMembers.size, checkedInMembers.size)

            current_patients.adapter = MemberAdapter(context,
                                                     checkedInMembers,
                                                     true,
                                                     identificationEventRepository)

            current_patients.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                val member = parent.getItemAtPosition(position) as Member
                val openCheckIn = identificationEventRepository.openCheckIn(member.id)
                if (openCheckIn == null) {
                    // TODO: this code path technically should not happen...
                    navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
                } else {
                    navigationManager.goTo(
                            CurrentMemberDetailFragment.forIdentificationEvent(openCheckIn))
                }
            }
        }

        identification_button.setOnClickListener {
            navigationManager.goTo(BarcodeFragment.forPurpose(BarcodeFragment.ScanPurpose.ID))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_logout).isVisible = true
        menu.findItem(R.id.menu_version).isVisible = true
    }
}
