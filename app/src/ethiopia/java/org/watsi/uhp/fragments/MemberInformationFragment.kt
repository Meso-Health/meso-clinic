package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_member_information.age_input
import kotlinx.android.synthetic.ethiopia.fragment_member_information.age_unit_spinner
import kotlinx.android.synthetic.ethiopia.fragment_member_information.gender_field
import kotlinx.android.synthetic.ethiopia.fragment_member_information.medical_record_number
import kotlinx.android.synthetic.ethiopia.fragment_member_information.next_button
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterBuilder
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.MemberInformationViewModel
import java.util.UUID
import javax.inject.Inject

class MemberInformationFragment : DaggerFragment() {
    @Inject lateinit var clock: Clock
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var keyboardManager: KeyboardManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: MemberInformationViewModel
    lateinit var observable: LiveData<MemberInformationViewModel.ViewState>
    lateinit var membershipNumber: String
    lateinit var encounterBuilder: EncounterBuilder
    internal val memberId = UUID.randomUUID()
    private val encounterId = UUID.randomUUID()

    companion object {
        const val PARAM_MEMBERSHIP_NUMBER = "membership_number"

        fun withMembershipNumber(membershipNumber: String): MemberInformationFragment {
            val fragment = MemberInformationFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_MEMBERSHIP_NUMBER, membershipNumber)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        membershipNumber = arguments.getString(PARAM_MEMBERSHIP_NUMBER)
        val encounter = Encounter(encounterId, memberId, null, Instant.now(clock))
        encounterBuilder = EncounterBuilder(encounter, emptyList(), emptyList(), emptyList())


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MemberInformationViewModel::class.java)
        observable = viewModel.getObservable(membershipNumber)
        observable.observe(this, Observer {
            it?.let { viewState ->
                gender_field.setGender(viewState.gender)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.member_information_fragment_label), R.drawable.ic_clear_white_24dp)
        return inflater?.inflate(R.layout.fragment_member_information, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        gender_field.setOnGenderChange { gender -> viewModel.onGenderChange(gender) }

        medical_record_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            text -> viewModel.onMedicalRecordNumberChange(text)
        })

        medical_record_number.onFocusChangeListener = View.OnFocusChangeListener{ view, hasFocus ->
            if (!hasFocus) { keyboardManager.hideKeyboard(view) }
        }

        val ageUnitAdapter = ArrayAdapter.createFromResource(
                context, R.array.age_units, android.R.layout.simple_spinner_dropdown_item)

        age_unit_spinner.adapter = ageUnitAdapter
        age_unit_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onAgeUnitChange(AgeUnit.valueOf(age_unit_spinner.selectedItem.toString()))
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { /* no-op */ }
        }

        age_input.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.onAgeChange(text.toIntOrNull())
        })

        age_input.onFocusChangeListener = View.OnFocusChangeListener{ view, hasFocus ->
            if (!hasFocus) { keyboardManager.hideKeyboard(view) }
        }

        next_button.setOnClickListener {
            viewModel.buildEncounterFlowRelation(memberId, encounterBuilder).subscribe({encounterBuilder ->
                navigationManager.goTo(EncounterFragment.forEncounter(encounterBuilder))
            }, { throwable ->
                // TODO when we implement validations
            })
        }
    }
}