package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.DismissIdentificationEventUseCase
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import org.watsi.domain.usecases.SyncPhotoUseCase
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
    fun provideSyncMemberUseCase(memberRepository: MemberRepository, deltaRepository: DeltaRepository): SyncMemberUseCase {
        return SyncMemberUseCase(memberRepository, deltaRepository)
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
    fun provideSyncIdentificationEventUseCase(identificationEventRepository: IdentificationEventRepository, deltaRepository: DeltaRepository): SyncIdentificationEventUseCase {
        return SyncIdentificationEventUseCase(identificationEventRepository, deltaRepository)
    }

    @Provides
    fun provideCreateEncounterUseCase(encounterRepository: EncounterRepository): CreateEncounterUseCase {
        return CreateEncounterUseCase(encounterRepository)
    }

    @Provides
    fun provideSyncEncounterUseCase(encounterRepository: EncounterRepository, deltaRepository: DeltaRepository): SyncEncounterUseCase {
        return SyncEncounterUseCase(encounterRepository, deltaRepository)
    }

    @Provides
    fun provideSyncEncounterFormUseCase(encounterFormRepository: EncounterFormRepository, deltaRepository: DeltaRepository): SyncEncounterFormUseCase {
        return SyncEncounterFormUseCase(encounterFormRepository, deltaRepository)
    }

    @Provides
    fun provideSyncPhotoUseCase(photoRepository: PhotoRepository, deltaRepository: DeltaRepository): SyncPhotoUseCase {
        return SyncPhotoUseCase(photoRepository, deltaRepository)
    }
}
