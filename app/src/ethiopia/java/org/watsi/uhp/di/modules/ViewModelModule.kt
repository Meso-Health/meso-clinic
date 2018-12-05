package org.watsi.uhp.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.watsi.uhp.di.ViewModelKey
import org.watsi.uhp.viewmodels.DaggerViewModelFactory
import org.watsi.uhp.viewmodels.DiagnosisViewModel
import org.watsi.uhp.viewmodels.DrugAndSupplyViewModel
import org.watsi.uhp.viewmodels.EditPriceViewModel
import org.watsi.uhp.viewmodels.EncounterViewModel
import org.watsi.uhp.viewmodels.HomeViewModel
import org.watsi.uhp.viewmodels.HouseholdViewModel
import org.watsi.uhp.viewmodels.MemberInformationViewModel
import org.watsi.uhp.viewmodels.PendingClaimsViewModel
import org.watsi.uhp.viewmodels.ReceiptViewModel
import org.watsi.uhp.viewmodels.ReturnedClaimsViewModel
import org.watsi.uhp.viewmodels.SearchViewModel
import org.watsi.uhp.viewmodels.SpinnerLineItemViewModel
import org.watsi.uhp.viewmodels.StatusViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HouseholdViewModel::class)
    abstract fun bindHouseholdViewModel(viewModel: HouseholdViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiagnosisViewModel::class)
    abstract fun bindDiagnosisViewModel(viewModel: DiagnosisViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DrugAndSupplyViewModel::class)
    abstract fun bindDrugAndSupplyViewModel(viewModel: DrugAndSupplyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditPriceViewModel::class)
    abstract fun bindEditPriceViewModel(viewModel: EditPriceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EncounterViewModel::class)
    abstract fun bindEncounterViewModel(viewModel: EncounterViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SpinnerLineItemViewModel::class)
    abstract fun bindLineItemViewModel(viewModel: SpinnerLineItemViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReceiptViewModel::class)
    abstract fun bindReceiptViewModel(viewModel: ReceiptViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PendingClaimsViewModel::class)
    abstract fun bindPendingClaimsViewModel(viewModel: PendingClaimsViewModel): ViewModel
    
    @Binds
    @IntoMap
    @ViewModelKey(ReturnedClaimsViewModel::class)
    abstract fun bindReturnedClaimsViewModel(viewModel: ReturnedClaimsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StatusViewModel::class)
    abstract fun bindStatusViewModel(viewModel: StatusViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MemberInformationViewModel::class)
    abstract fun bindMemberInformationViewModel(viewModel: MemberInformationViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory
}
