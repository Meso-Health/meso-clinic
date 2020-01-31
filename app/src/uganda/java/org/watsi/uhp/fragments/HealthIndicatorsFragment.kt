package org.watsi.uhp.fragments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.uganda.fragment_health_indicators.fever_checkbox
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.HealthIndicatorsViewModel
import kotlinx.android.synthetic.uganda.fragment_health_indicators.save_button
import javax.inject.Inject

class HealthIndicatorsFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: HealthIndicatorsViewModel
    lateinit var observable: LiveData<HealthIndicatorsViewModel.ViewState>
    lateinit var encounterFlowState: EncounterFlowState

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterFlowState): HealthIndicatorsFragment {
            val fragment = HealthIndicatorsFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HealthIndicatorsViewModel::class.java)
        observable = viewModel.getObservable()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.health_indicators_title), R.drawable.ic_clear_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_health_indicators, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        viewModel.setHasFever(false)
        fever_checkbox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHasFever(isChecked)
        }
        save_button.setOnClickListener {
            viewModel.updateEncounterWithHealthIndicators(encounterFlowState)
            navigationManager.goTo(EncounterFragment.forEncounter(encounterFlowState))
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
