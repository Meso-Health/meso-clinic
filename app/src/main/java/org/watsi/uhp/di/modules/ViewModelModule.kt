package org.watsi.uhp.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.watsi.uhp.di.ViewModelKey
import org.watsi.uhp.viewmodels.AddNewBillableViewModel
import org.watsi.uhp.viewmodels.CompleteEnrollmentViewModel
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import org.watsi.uhp.viewmodels.CurrentPatientsViewModel
import org.watsi.uhp.viewmodels.DaggerViewModelFactory
import org.watsi.uhp.viewmodels.DiagnosisViewModel
import org.watsi.uhp.viewmodels.EncounterViewModel
import org.watsi.uhp.viewmodels.SearchMemberViewModel
import org.watsi.uhp.viewmodels.StatusViewModel
import org.watsi.uhp.viewmodels.CheckInMemberDetailViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddNewBillableViewModel::class)
    abstract fun bindAddNewBillableViewModel(viewModel: AddNewBillableViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrentMemberDetailViewModel::class)
    abstract fun bindCurrentMemberDetailViewModel(viewModel: CurrentMemberDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CheckInMemberDetailViewModel::class)
    abstract fun bindCheckInMemberDetailViewModel(viewModel: CheckInMemberDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CurrentPatientsViewModel::class)
    abstract fun bindCurrentPatientsViewModel(viewModel: CurrentPatientsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiagnosisViewModel::class)
    abstract fun bindDiagnosisViewModel(viewModel: DiagnosisViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EncounterViewModel::class)
    abstract fun bindEncounterViewModel(viewModel: EncounterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchMemberViewModel::class)
    abstract fun bindSearchMemberViewModel(viewModel: SearchMemberViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    abstract fun bindStatusViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CompleteEnrollmentViewModel::class)
    abstract fun bindCompleteEnrollmentViewModel(viewModel: CompleteEnrollmentViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}
