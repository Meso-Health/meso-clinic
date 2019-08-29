package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.fragments.CheckedInPatientsFragment
import org.watsi.uhp.fragments.DiagnosisFragment
import org.watsi.uhp.fragments.DownloadHouseholdFragment
import org.watsi.uhp.fragments.DrugAndSupplyFragment
import org.watsi.uhp.fragments.EditMemberFragment
import org.watsi.uhp.fragments.EditPriceFragment
import org.watsi.uhp.fragments.HomeFragment
import org.watsi.uhp.fragments.HouseholdFragment
import org.watsi.uhp.fragments.MemberInformationFragment
import org.watsi.uhp.fragments.MemberNotFoundFragment
import org.watsi.uhp.fragments.MemberSearchFragment
import org.watsi.uhp.fragments.PendingClaimsFragment
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.fragments.ReturnedClaimsFragment
import org.watsi.uhp.fragments.SearchFragment
import org.watsi.uhp.fragments.SpinnerLineItemFragment
import org.watsi.uhp.fragments.StatusFragment
import org.watsi.uhp.fragments.VisitTypeFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindHomeFragment(): HomeFragment
    @ContributesAndroidInjector abstract fun bindCheckedInPatientsFragment(): CheckedInPatientsFragment
    @ContributesAndroidInjector abstract fun bindSearchFragment(): SearchFragment
    @ContributesAndroidInjector abstract fun bindHouseholdFragment(): HouseholdFragment
    @ContributesAndroidInjector abstract fun bindDiagnosisFragment(): DiagnosisFragment
    @ContributesAndroidInjector abstract fun bindDownloadHouseholdFragment(): DownloadHouseholdFragment
    @ContributesAndroidInjector abstract fun bindDrugAndSupplyFragment(): DrugAndSupplyFragment
    @ContributesAndroidInjector abstract fun bindEditMemberFragment(): EditMemberFragment
    @ContributesAndroidInjector abstract fun bindEditPriceFragment(): EditPriceFragment
    @ContributesAndroidInjector abstract fun bindMemberInformationFragment(): MemberInformationFragment
    @ContributesAndroidInjector abstract fun bindMemberNotFoundFragment(): MemberNotFoundFragment
    @ContributesAndroidInjector abstract fun bindMemberSearchFragment(): MemberSearchFragment
    @ContributesAndroidInjector abstract fun bindReceiptFragment(): ReceiptFragment
    @ContributesAndroidInjector abstract fun bindPendingClaimsFragment(): PendingClaimsFragment
    @ContributesAndroidInjector abstract fun bindReturnedClaimsFragment(): ReturnedClaimsFragment
    @ContributesAndroidInjector abstract fun bindSpinnerLineItemFragment(): SpinnerLineItemFragment
    @ContributesAndroidInjector abstract fun bindStatusFragment(): StatusFragment
    @ContributesAndroidInjector abstract fun bindVisitTypeFragment(): VisitTypeFragment
}
