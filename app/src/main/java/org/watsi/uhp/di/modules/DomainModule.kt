package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.domain.repositories.PriceScheduleRepository
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.DeletePendingClaimAndMemberUseCase
import org.watsi.domain.usecases.FetchReturnedClaimsUseCase
import org.watsi.domain.usecases.FetchStatusUseCase
import org.watsi.domain.usecases.FindHouseholdIdByCardIdUseCase
import org.watsi.domain.usecases.FindHouseholdIdByMembershipNumberUseCase
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
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
        priceScheduleRepository: PriceScheduleRepository
    ): UpdateEncounterUseCase {
        return UpdateEncounterUseCase(encounterRepository, priceScheduleRepository)
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
}
