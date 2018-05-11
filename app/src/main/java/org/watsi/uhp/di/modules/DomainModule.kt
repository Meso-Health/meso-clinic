package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.DismissIdentificationEventUseCase
import org.watsi.domain.usecases.FetchStatusUseCase
import org.watsi.domain.usecases.SyncStatusUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase

@Module
class DomainModule {

    @Provides
    fun provideCreateMemberUseCase(memberRepository: MemberRepository): CreateMemberUseCase {
        return CreateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideUpdateMemberUseCase(memberRepository: MemberRepository): UpdateMemberUseCase {
        return UpdateMemberUseCase(memberRepository)
    }

    @Provides
    fun provideCreateIdentificationEventUseCase(identificationEventRepository: IdentificationEventRepository): CreateIdentificationEventUseCase {
        return CreateIdentificationEventUseCase(identificationEventRepository)
    }

    @Provides
    fun provideDismissIdentificationEventUseCase(identificationEventRepository: IdentificationEventRepository): DismissIdentificationEventUseCase {
        return DismissIdentificationEventUseCase(identificationEventRepository)
    }

    @Provides
    fun provideCreateEncounterUseCase(encounterRepository: EncounterRepository): CreateEncounterUseCase {
        return CreateEncounterUseCase(encounterRepository)
    }

    @Provides
    fun provideSyncStatusUseCase(deltaRepository: DeltaRepository): SyncStatusUseCase {
        return SyncStatusUseCase(deltaRepository)
    }

    @Provides
    fun provideFetchStatusUseCase(memberRepository: MemberRepository): FetchStatusUseCase {
        return FetchStatusUseCase(memberRepository)
    }
}
