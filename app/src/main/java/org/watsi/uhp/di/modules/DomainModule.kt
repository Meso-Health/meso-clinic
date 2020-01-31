package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.EncounterFormRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.EnrollmentPeriodRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MainRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.domain.repositories.PriceScheduleRepository
import org.watsi.domain.repositories.ReferralRepository
import org.watsi.domain.usecases.CheckForSameDayEncountersUseCase
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.DeletePendingClaimAndMemberUseCase
import org.watsi.domain.usecases.DeleteUserDataUseCase
import org.watsi.domain.usecases.DismissMemberUseCase
import org.watsi.domain.usecases.ExportUnsyncedClaimsAsJsonUseCase
import org.watsi.domain.usecases.ExportUnsyncedClaimsAsTextUseCase
import org.watsi.domain.usecases.FetchBillablesUseCase
import org.watsi.domain.usecases.FetchDiagnosesUseCase
import org.watsi.domain.usecases.FetchEnrollmentPeriodUseCase
import org.watsi.domain.usecases.FetchHouseholdIdByCardIdUseCase
import org.watsi.domain.usecases.FetchHouseholdIdByMembershipNumberUseCase
import org.watsi.domain.usecases.FetchMembersPhotosUseCase
import org.watsi.domain.usecases.FetchMembersUseCase
import org.watsi.domain.usecases.FetchOpenIdentificationEventsUseCase
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import org.watsi.domain.usecases.FetchStatusUseCase
import org.watsi.domain.usecases.FindHouseholdIdByCardIdUseCase
import org.watsi.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadAllBillablesTypesUseCase
import org.watsi.domain.usecases.LoadAllBillablesUseCase
import org.watsi.domain.usecases.LoadBillablesOfTypeUseCase
import org.watsi.domain.usecases.LoadCheckedInMembersUseCase
import org.watsi.domain.usecases.LoadDefaultBillablesUseCase
import org.watsi.domain.usecases.LoadEncounterWithExtrasUseCase
import org.watsi.domain.usecases.LoadHouseholdMembersUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.LoadOnePendingClaimUseCase
import org.watsi.domain.usecases.LoadOneReturnedClaimUseCase
import org.watsi.domain.usecases.LoadPendingClaimsCountUseCase
import org.watsi.domain.usecases.LoadPendingClaimsUseCase
import org.watsi.domain.usecases.LoadPhotoUseCase
import org.watsi.domain.usecases.LoadReturnedClaimsCountUseCase
import org.watsi.domain.usecases.LoadReturnedClaimsUseCase
import org.watsi.domain.usecases.MarkReturnedEncountersAsRevisedUseCase
import org.watsi.domain.usecases.PersistReturnedEncountersUseCase
import org.watsi.domain.usecases.ReviseClaimUseCase
import org.watsi.domain.usecases.ShouldEnrollUseCase
import org.watsi.domain.usecases.SubmitClaimUseCase
import org.watsi.domain.usecases.SyncBillableUseCase
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import org.watsi.domain.usecases.SyncPriceScheduleUseCase
import org.watsi.domain.usecases.SyncStatusUseCase
import org.watsi.domain.usecases.UpdateEncounterUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import org.watsi.domain.usecases.ValidateDiagnosesAndBillablesExistenceUseCase

@Module
class DomainModule {

    @Provides
    fun providerCreateMemberUseCase(memberRepository: MemberRepository): CreateMemberUseCase {
        return CreateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideUpdateMemberUseCase(memberRepository: MemberRepository): UpdateMemberUseCase {
        return UpdateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideSyncMemberUseCase(
            memberRepository: MemberRepository,
            deltaRepository: DeltaRepository
    ): SyncMemberUseCase {
        return SyncMemberUseCase(memberRepository, deltaRepository)
    }

    @Provides
    fun provideCreateIdentificationEventUseCase(
            identificationEventRepository: IdentificationEventRepository
    ): CreateIdentificationEventUseCase {
        return CreateIdentificationEventUseCase(identificationEventRepository)
    }

    @Provides
    fun provideSyncBillableUseCase(
            billableRepository: BillableRepository,
            deltaRepository: DeltaRepository
    ): SyncBillableUseCase {
        return SyncBillableUseCase(billableRepository, deltaRepository)
    }

    @Provides
    fun provideSyncPriceScheduleUseCase(
            priceScheduleRepository: PriceScheduleRepository,
            deltaRepository: DeltaRepository
    ): SyncPriceScheduleUseCase {
        return SyncPriceScheduleUseCase(priceScheduleRepository, deltaRepository)
    }
    
    @Provides
    fun provideSyncIdentificationEventUseCase(
            identificationEventRepository: IdentificationEventRepository,
            deltaRepository: DeltaRepository
    ): SyncIdentificationEventUseCase {
        return SyncIdentificationEventUseCase(identificationEventRepository, deltaRepository)
    }

    @Provides
    fun provideCreateEncounterUseCase(
            encounterRepository: EncounterRepository,
            billableRepository: BillableRepository,
            priceScheduleRepository: PriceScheduleRepository
    ): CreateEncounterUseCase {
        return CreateEncounterUseCase(encounterRepository, billableRepository, priceScheduleRepository)
    }

    @Provides
    fun provideUpdateEncounterUseCase(
        encounterRepository: EncounterRepository,
        referralRepository: ReferralRepository,
        priceScheduleRepository: PriceScheduleRepository
    ): UpdateEncounterUseCase {
        return UpdateEncounterUseCase(encounterRepository, referralRepository, priceScheduleRepository)
    }

    @Provides
    fun provideSyncEncounterUseCase(
            encounterRepository: EncounterRepository,
            deltaRepository: DeltaRepository
    ): SyncEncounterUseCase {
        return SyncEncounterUseCase(encounterRepository, deltaRepository)
    }

    @Provides
    fun provideSyncEncounterFormUseCase(
            encounterFormRepository: EncounterFormRepository,
            deltaRepository: DeltaRepository
    ): SyncEncounterFormUseCase {
        return SyncEncounterFormUseCase(encounterFormRepository, deltaRepository)
    }

    @Provides
    fun provideSyncMemberPhotoUseCase(
            memberRepository: MemberRepository,
            deltaRepository: DeltaRepository
    ): SyncMemberPhotoUseCase {
        return SyncMemberPhotoUseCase(memberRepository, deltaRepository)
    }
  
    @Provides
    fun provideSyncStatusUseCase(deltaRepository: DeltaRepository): SyncStatusUseCase {
        return SyncStatusUseCase(deltaRepository)
    }

    @Provides
    fun provideFetchStatusUseCase(memberRepository: MemberRepository): FetchStatusUseCase {
        return FetchStatusUseCase(memberRepository)
    }

    @Provides
    fun provideFetchMembersUseCase(memberRepository: MemberRepository): FetchMembersUseCase {
        return FetchMembersUseCase(memberRepository)
    }

    @Provides
    fun provideFetchOpenIdentificationEventsUseCase(identificationEventRepository: IdentificationEventRepository): FetchOpenIdentificationEventsUseCase {
        return FetchOpenIdentificationEventsUseCase(identificationEventRepository)
    }

    @Provides
    fun provideFetchBillablesUseCase(billableRepository: BillableRepository): FetchBillablesUseCase {
        return FetchBillablesUseCase(billableRepository)
    }

    @Provides
    fun provideFetchDiagnosesUseCase(diagnosisRepository: DiagnosisRepository): FetchDiagnosesUseCase {
        return FetchDiagnosesUseCase(diagnosisRepository)
    }

    @Provides
    fun provideFetchHouseholdIdByCardIdUseCase(memberRepository: MemberRepository): FetchHouseholdIdByCardIdUseCase {
        return FetchHouseholdIdByCardIdUseCase(memberRepository)
    }

    @Provides
    fun provideFetchHouseholdIdByMembershipNumberUseCase(memberRepository: MemberRepository): FetchHouseholdIdByMembershipNumberUseCase {
        return FetchHouseholdIdByMembershipNumberUseCase(memberRepository)
    }

    @Provides
    fun provideFetchMemberPhotosUseCase(memberRepository: MemberRepository): FetchMembersPhotosUseCase {
        return FetchMembersPhotosUseCase(memberRepository)
    }

    @Provides
    fun provideLoadMemberUseCase(memberRepository: MemberRepository): LoadMemberUseCase {
        return LoadMemberUseCase(memberRepository)
    }

    @Provides
    fun provideLoadPhotoUseCase(photoRepository: PhotoRepository): LoadPhotoUseCase {
        return LoadPhotoUseCase(photoRepository)
    }

    @Provides
    fun provideLoadDefaultOpdBillablesUseCase(
            billableRepository: BillableRepository
    ): LoadDefaultBillablesUseCase {
        return LoadDefaultBillablesUseCase(billableRepository)
    }

    @Provides
    fun provideLoadBillablesofTypeUseCase(
        billableRepository: BillableRepository
    ): LoadBillablesOfTypeUseCase {
        return LoadBillablesOfTypeUseCase(billableRepository)
    }

    @Provides
    fun provideLoadAllBillablesUseCase(
        billableRepository: BillableRepository
    ): LoadAllBillablesUseCase {
        return LoadAllBillablesUseCase(billableRepository)
    }

    @Provides
    fun provideLoadHouseholdMembersUseCase(
            memberRepository: MemberRepository
    ): LoadHouseholdMembersUseCase {
        return LoadHouseholdMembersUseCase(memberRepository)
    }

    @Provides
    fun provideIsMemberCheckedInUseCase(memberRepository: MemberRepository): IsMemberCheckedInUseCase {
        return IsMemberCheckedInUseCase(memberRepository)
    }

    @Provides
    fun providePersistReturnedEncountersUseCase(encounterRepository: EncounterRepository): PersistReturnedEncountersUseCase {
        return PersistReturnedEncountersUseCase(encounterRepository)
    }

    @Provides
    fun provideMarkReturnedEncounterAsRevisedUseCase(encounterRepository: EncounterRepository): MarkReturnedEncountersAsRevisedUseCase {
        return MarkReturnedEncountersAsRevisedUseCase(encounterRepository)
    }

    @Provides
    fun provideFetchReturnedEncounterUseCase(
            persistReturnedEncountersUseCase: PersistReturnedEncountersUseCase,
            markReturnedEncountersAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase,
            encounterRepository: EncounterRepository
    ): FetchReturnedClaimsUseCase {
        return FetchReturnedClaimsUseCase(persistReturnedEncountersUseCase, markReturnedEncountersAsRevisedUseCase, encounterRepository)
    }

    @Provides
    fun provideLoadPendingClaimsCountUseCase(
        encounterRepository: EncounterRepository
    ): LoadPendingClaimsCountUseCase {
        return LoadPendingClaimsCountUseCase(encounterRepository)
    }

    @Provides
    fun provideLoadPendingClaimsUseCase(
        encounterRepository: EncounterRepository
    ): LoadPendingClaimsUseCase {
        return LoadPendingClaimsUseCase(encounterRepository)
    }
    
    @Provides
    fun provideLoadReturnedClaimsCountUseCase(
        encounterRepository: EncounterRepository
    ): LoadReturnedClaimsCountUseCase {
        return LoadReturnedClaimsCountUseCase(encounterRepository)
    }

    @Provides
    fun provideLoadReturnedClaimsUseCase(
        encounterRepository: EncounterRepository
    ): LoadReturnedClaimsUseCase {
        return LoadReturnedClaimsUseCase(encounterRepository)
    }

    @Provides
    fun provideReviseClaimUseCase(
        createEncounterUseCase: CreateEncounterUseCase,
        markReturnedEncounterAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
    ): ReviseClaimUseCase {
        return ReviseClaimUseCase(createEncounterUseCase, markReturnedEncounterAsRevisedUseCase)
    }

    @Provides
    fun provideSubmitClaimUseCase(
        deltaRepository: DeltaRepository,
        encounterRepository: EncounterRepository
    ): SubmitClaimUseCase {
        return SubmitClaimUseCase(deltaRepository, encounterRepository)
    }

    @Provides
    fun provideDeletePendingClaimAndMemberUseCase(
        encounterRepository: EncounterRepository
    ): DeletePendingClaimAndMemberUseCase {
        return DeletePendingClaimAndMemberUseCase(encounterRepository)
    }

    @Provides
    fun provideLoadEncounterWithExtrasUseCase(
        encounterRepository: EncounterRepository
    ): LoadEncounterWithExtrasUseCase {
        return LoadEncounterWithExtrasUseCase(encounterRepository)
    }

    @Provides
    fun provideLoadCheckedInMembersUseCase(
        memberRepository: MemberRepository
    ): LoadCheckedInMembersUseCase {
        return LoadCheckedInMembersUseCase(memberRepository)
    }

    @Provides
    fun provideFindHouseholdIdByMembershipNumberUseCase(
        memberRepository: MemberRepository
    ): FindHouseholdIdByMembershipNumberUseCase {
        return FindHouseholdIdByMembershipNumberUseCase(memberRepository)
    }

    @Provides
    fun provideFindHouseholdIdByCardIdUseCase(
        memberRepository: MemberRepository
    ): FindHouseholdIdByCardIdUseCase {
        return FindHouseholdIdByCardIdUseCase(memberRepository)
    }

    @Provides
    fun provideLoadOnePendingClaimUseCase(
        encounterRepository: EncounterRepository
    ): LoadOnePendingClaimUseCase {
        return LoadOnePendingClaimUseCase(encounterRepository)
    }

    @Provides
    fun provideLoadOneReturnedClaimUseCase(
        encounterRepository: EncounterRepository
    ): LoadOneReturnedClaimUseCase {
        return LoadOneReturnedClaimUseCase(encounterRepository)
    }

    @Provides
    fun provideDismissMemberUseCase(
        identificationEventRepository: IdentificationEventRepository
    ): DismissMemberUseCase {
        return DismissMemberUseCase(identificationEventRepository)
    }

    @Provides
    fun provideValidateDiagnosesAndBillablesExistenceUseCase(
        billableRepository: BillableRepository,
        diagnosisRepository: DiagnosisRepository
    ): ValidateDiagnosesAndBillablesExistenceUseCase {
        return ValidateDiagnosesAndBillablesExistenceUseCase(billableRepository, diagnosisRepository)
    }

    @Provides
    fun provideDeleteUserDataUseCase(
        mainRepository: MainRepository
    ): DeleteUserDataUseCase {
        return DeleteUserDataUseCase(mainRepository)
    }

    @Provides
    fun provideExportUnsyncedClaimsUseCase(
        deltaRepository: DeltaRepository,
        encounterRepository: EncounterRepository
    ): ExportUnsyncedClaimsAsJsonUseCase {
        return ExportUnsyncedClaimsAsJsonUseCase(deltaRepository, encounterRepository)
    }

    @Provides
    fun provideExportUnsyncedClaimsAsTextUseCase(
        deltaRepository: DeltaRepository,
        encounterRepository: EncounterRepository,
        clock: Clock
        ): ExportUnsyncedClaimsAsTextUseCase {
        return ExportUnsyncedClaimsAsTextUseCase(deltaRepository, encounterRepository, clock)
    }

    @Provides
    fun provideCheckForSameDayEncountersUseCase(
        encounterRepository: EncounterRepository
    ): CheckForSameDayEncountersUseCase {
        return CheckForSameDayEncountersUseCase(encounterRepository)
    }

    @Provides
    fun provideFetchEnrollmentPeriodUseCase(
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): FetchEnrollmentPeriodUseCase {
        return FetchEnrollmentPeriodUseCase(enrollmentPeriodRepository)
    }

    @Provides
    fun provideShouldEnrollUseCase(
        enrollmentPeriodRepository: EnrollmentPeriodRepository
    ): ShouldEnrollUseCase {
        return ShouldEnrollUseCase(enrollmentPeriodRepository)
    }

    @Provides
    fun provideLoadAllBillablesTypesUseCase(
        billableRepository: BillableRepository
    ): LoadAllBillablesTypesUseCase {
        return LoadAllBillablesTypesUseCase(billableRepository)
    }
}
