package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.fragments.CheckInMemberDetailFragment
import org.watsi.uhp.fragments.CurrentMemberDetailFragment
import org.watsi.uhp.fragments.CurrentPatientsFragment
import org.watsi.uhp.fragments.DiagnosisFragment
import org.watsi.uhp.fragments.EditMemberFragment
import org.watsi.uhp.fragments.EncounterFragment
import org.watsi.uhp.fragments.EnrollNewbornFragment
import org.watsi.uhp.fragments.NewClaimFragment
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.fragments.SearchMemberFragment
import org.watsi.uhp.fragments.StatusFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindCheckInMemberDetailFragment(): CheckInMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentMemberDetailFragment(): CurrentMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentPatientsFragment(): CurrentPatientsFragment
    @ContributesAndroidInjector abstract fun bindNewClaimFragment(): NewClaimFragment
    @ContributesAndroidInjector abstract fun bindDiagnosisFragment(): DiagnosisFragment
    @ContributesAndroidInjector abstract fun bindEncounterFragment(): EncounterFragment
    @ContributesAndroidInjector abstract fun bindEnrollNewbornFragment(): EnrollNewbornFragment
    @ContributesAndroidInjector abstract fun bindEditMemberFragment(): EditMemberFragment
    @ContributesAndroidInjector abstract fun bindReceiptFragment(): ReceiptFragment
    @ContributesAndroidInjector abstract fun bindSearchMemberFragment(): SearchMemberFragment
    @ContributesAndroidInjector abstract fun bindStatusFragment(): StatusFragment
}
