package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients
import kotlinx.android.synthetic.main.fragment_current_patients.current_patients_label
import kotlinx.android.synthetic.main.fragment_current_patients.identification_button
import org.watsi.device.managers.Logger
import org.watsi.device.managers.SessionManager

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.adapters.MemberAdapter
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.CurrentPatientsViewModel

import javax.inject.Inject

class CurrentPatientsFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var photoRepository: PhotoRepository
    @Inject lateinit var logger: Logger

    lateinit var viewModel: CurrentPatientsViewModel

    companion object {
        const val SCAN_CARD_INTENT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentPatientsViewModel::class.java)
        viewModel.getObservable().observe(this, Observer {
            it?.let { viewState ->
                val checkedInMembers = viewState.checkedInMembers
                if (checkedInMembers.isEmpty()) {
                    current_patients_label.visibility = View.GONE
                } else {
                    current_patients_label.text = activity.resources.getQuantityString(
                            R.plurals.current_patients_label, checkedInMembers.size, checkedInMembers.size)

                    current_patients.adapter = MemberAdapter(context,
                            checkedInMembers,
                            photoRepository,
                            true)
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.current_patients_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_current_patients, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        current_patients.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val member = parent.getItemAtPosition(position) as Member
            viewModel.getIdentificationEvent(member).subscribe({idEvent ->
                navigationManager.goTo(CurrentMemberDetailFragment.forMemberAndIdEvent(member, idEvent))
            }, {
                // TODO: handle error
            }, {
                // TODO: this code path technically should not happen...
                navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
            })
        }

        identification_button.setOnClickListener {
            startActivityForResult(Intent(activity, SearchByMemberCardActivity::class.java), SCAN_CARD_INTENT)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        menu!!.findItem(R.id.menu_logout).isVisible = true
        menu.findItem(R.id.menu_version).isVisible = true
        menu.findItem(R.id.menu_search_by_name_or_id).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_version -> {
                navigationManager.goTo(StatusFragment())
            }
            R.id.menu_logout -> {
                AlertDialog.Builder(activity)
                        .setTitle(R.string.log_out_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            sessionManager.logout()
                            (activity as ClinicActivity).navigateToAuthenticationActivity()
                        }.create().show()
            }
            R.id.menu_search_by_name_or_id -> {
                navigationManager.goTo(SearchMemberFragment())
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val (member, error) = SearchByMemberCardActivity.parseResult(resultCode, data, logger)
        member?.let {member ->
            viewModel.getIdentificationEvent(member).subscribe({idEvent ->
                navigationManager.goTo(CurrentMemberDetailFragment.forMemberAndIdEvent(member, idEvent))
            }, {
                // TODO: handle error
            }, {
                // TODO: this code path technically should not happen...
                navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
            })
        }
        error?.let {
            // TODO: display error?
        }
    }
}
