package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.fragments.AddNewBillableFragment
import org.watsi.uhp.fragments.CheckInMemberDetailFragment
import org.watsi.uhp.fragments.CurrentMemberDetailFragment
import org.watsi.uhp.fragments.CurrentPatientsFragment
import org.watsi.uhp.fragments.DiagnosisFragment
import org.watsi.uhp.fragments.EncounterFormFragment
import org.watsi.uhp.fragments.EncounterFragment
import org.watsi.uhp.fragments.EnrollNewbornInfoFragment
import org.watsi.uhp.fragments.EnrollNewbornPhotoFragment
import org.watsi.uhp.fragments.EnrollmentFingerprintFragment
import org.watsi.uhp.fragments.EnrollmentMemberPhotoFragment
import org.watsi.uhp.fragments.MemberEditFragment
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.fragments.SearchMemberFragment
import org.watsi.uhp.fragments.VersionAndSyncFragment

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector abstract fun bindAddNewBillableFragment(): AddNewBillableFragment
    @ContributesAndroidInjector abstract fun bindCheckInMemberDetailFragment(): CheckInMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentMemberDetailFragment(): CurrentMemberDetailFragment
    @ContributesAndroidInjector abstract fun bindCurrentPatientsFragment(): CurrentPatientsFragment
    @ContributesAndroidInjector abstract fun bindDiagnosisFragment(): DiagnosisFragment
    @ContributesAndroidInjector abstract fun bindEncounterFormFragment(): EncounterFormFragment
    @ContributesAndroidInjector abstract fun bindEncounterFragment(): EncounterFragment
    @ContributesAndroidInjector abstract fun bindEnrollmentFingerprintFragment(): EnrollmentFingerprintFragment
    @ContributesAndroidInjector abstract fun bindEnrollmentMemberPhotoFragment(): EnrollmentMemberPhotoFragment
    @ContributesAndroidInjector abstract fun bindEnrollNewbornInfoFragment(): EnrollNewbornInfoFragment
    @ContributesAndroidInjector abstract fun bindEnrollNewbornPhotoFragment(): EnrollNewbornPhotoFragment
    @ContributesAndroidInjector abstract fun bindMemberEditFragment(): MemberEditFragment
    @ContributesAndroidInjector abstract fun bindReceiptFragment(): ReceiptFragment
    @ContributesAndroidInjector abstract fun bindSearchMemberFragment(): SearchMemberFragment
    @ContributesAndroidInjector abstract fun bindVersionAndSyncFragment(): VersionAndSyncFragment
}
