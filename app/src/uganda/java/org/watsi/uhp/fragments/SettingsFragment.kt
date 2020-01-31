package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.uganda.fragment_settings.copayment_default_field
import kotlinx.android.synthetic.uganda.fragment_settings.save_button
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.uhp.R
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.SettingsViewModel
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var logger: Logger
    lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)
        val currentDefaultCopayment = preferencesManager.getDefaultCopaymentAmount()
        viewModel.getObservable(currentDefaultCopayment).observe(this, Observer {
            it?.let { viewState ->
                save_button.isEnabled = viewState.isValid
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.setting_fragment_label)
        return inflater?.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val currentDefaultCopayment = preferencesManager.getDefaultCopaymentAmount()
        copayment_default_field.setText(currentDefaultCopayment.toString())
        save_button.setOnClickListener {
            updateDefaultCopaymentAmount()
            navigationManager.popTo(CurrentPatientsFragment.withSnackbarMessage(getString(R.string.copayment_update_message)))
        }
        copayment_default_field.addTextChangedListener(LayoutHelper.OnChangedListener { text ->
            viewModel.updateCopaymentAmount( text.toIntOrNull())
        })
    }

    private fun updateDefaultCopaymentAmount() {
        val newDefaultCopayment = viewModel.copaymentAmount()
        if (newDefaultCopayment == null) {
            return
        }
        preferencesManager.setDefaultCopaymentAmount(newDefaultCopayment)
    }
}
