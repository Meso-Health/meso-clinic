package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.fragments.AddNewBillableFragment
import org.watsi.uhp.fragments.CheckInMemberDetailFragment
import org.watsi.uhp.fragments.CurrentMemberDetailFragment
import org.watsi.uhp.fragments.CurrentPatientsFragment
import org.watsi.uhp.fragments.DiagnosisFragment
import org.watsi.uhp.fragments.EditMemberFragment
import org.watsi.uhp.fragments.EditPriceFragment
import org.watsi.uhp.fragments.EncounterFormFragment
import org.watsi.uhp.fragments.EncounterFragment
import org.watsi.uhp.fragments.EnrollNewbornFragment
import org.watsi.uhp.fragments.HealthIndicatorsFragment
import org.watsi.uhp.fragments.MemberSearchFragment
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.fragments.SettingsFragment
import org.watsi.uhp.fragments.StatusFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindAddNewBillableFragment(): AddNewBillableFragment
    @ContributesAndroidInjector abstract fun bindCheckInMemberDetailFragment(): CheckInMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentMemberDetailFragment(): CurrentMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentPatientsFragment(): CurrentPatientsFragment
    @ContributesAndroidInjector abstract fun bindDiagnosisFragment(): DiagnosisFragment
    @ContributesAndroidInjector abstract fun bindHealthIndicatorsFragment(): HealthIndicatorsFragment
    @ContributesAndroidInjector abstract fun bindEditMemberFragment(): EditMemberFragment
    @ContributesAndroidInjector abstract fun bindEditPriceFragment(): EditPriceFragment
    @ContributesAndroidInjector abstract fun bindEncounterFormFragment(): EncounterFormFragment
    @ContributesAndroidInjector abstract fun bindEncounterFragment(): EncounterFragment
    @ContributesAndroidInjector abstract fun bindEnrollNewbornFragment(): EnrollNewbornFragment
    @ContributesAndroidInjector abstract fun bindReceiptFragment(): ReceiptFragment
    @ContributesAndroidInjector abstract fun bindMemberSearchFragment(): MemberSearchFragment
    @ContributesAndroidInjector abstract fun bindStatusFragment(): StatusFragment
    @ContributesAndroidInjector abstract fun bindSettingsFragment(): SettingsFragment
}
