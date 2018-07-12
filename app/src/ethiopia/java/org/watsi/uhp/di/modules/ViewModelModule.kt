package org.watsi.uhp.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.watsi.uhp.di.ViewModelKey
import org.watsi.uhp.viewmodels.CheckInMemberDetailViewModel
import org.watsi.uhp.viewmodels.CurrentMemberDetailViewModel
import org.watsi.uhp.viewmodels.CurrentPatientsViewModel
import org.watsi.uhp.viewmodels.DaggerViewModelFactory
import org.watsi.uhp.viewmodels.DiagnosisViewModel
import org.watsi.uhp.viewmodels.EditMemberViewModel
import org.watsi.uhp.viewmodels.EncounterViewModel
import org.watsi.uhp.viewmodels.EnrollNewbornViewModel
import org.watsi.uhp.viewmodels.NewClaimViewModel
import org.watsi.uhp.viewmodels.MemberInformationViewModel
import org.watsi.uhp.viewmodels.ReceiptViewModel
import org.watsi.uhp.viewmodels.SearchMemberViewModel
import org.watsi.uhp.viewmodels.StatusViewModel

@Module
abstract class ViewModelModule {

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
    @ViewModelKey(EditMemberViewModel::class)
    abstract fun bindEditMemberViewModel(viewModel: EditMemberViewModel): ViewModel

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
    @ViewModelKey(ReceiptViewModel::class)
    abstract fun bindReceiptViewModel(viewModel: ReceiptViewModel): ViewModel

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
    @ViewModelKey(EnrollNewbornViewModel::class)
    abstract fun bindEnrollNewbornViewModel(viewModel: EnrollNewbornViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MemberInformationViewModel::class)
    abstract fun bindMemberInformationViewModel(viewModel: MemberInformationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NewClaimViewModel::class)
    abstract fun bindNewClaimViewModel(viewModel: NewClaimViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}
