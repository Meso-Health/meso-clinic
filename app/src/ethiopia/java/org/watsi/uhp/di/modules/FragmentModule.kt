package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.fragments.DiagnosisFragment
import org.watsi.uhp.fragments.DrugAndSupplyFragment
import org.watsi.uhp.fragments.NewClaimFragment
import org.watsi.uhp.fragments.MemberInformationFragment
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.fragments.SpinnerLineItemFragment
import org.watsi.uhp.fragments.StatusFragment
import org.watsi.uhp.fragments.VisitTypeFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindDiagnosisFragment(): DiagnosisFragment
    @ContributesAndroidInjector abstract fun bindDrugAndSupplyFragment(): DrugAndSupplyFragment
    @ContributesAndroidInjector abstract fun bindMemberInformationFragment(): MemberInformationFragment
    @ContributesAndroidInjector abstract fun bindNewClaimFragment(): NewClaimFragment
    @ContributesAndroidInjector abstract fun bindReceiptFragment(): ReceiptFragment
    @ContributesAndroidInjector abstract fun bindSpinnerLineItemFragment(): SpinnerLineItemFragment
    @ContributesAndroidInjector abstract fun bindStatusFragment(): StatusFragment
    @ContributesAndroidInjector abstract fun bindVisitTypeFragment(): VisitTypeFragment
}
