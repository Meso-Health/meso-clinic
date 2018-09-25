package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class CreateMemberUseCaseTest {

    @Mock lateinit var mockMemberRepository: MemberRepository
    lateinit var useCase: CreateMemberUseCase

    @Before
    fun setup() {
        useCase = CreateMemberUseCase(mockMemberRepository)
    }

    @Test
    fun execute_memberDoesNotHavePhoto_createsMemberAndMemberDelta() {
        val member = MemberFactory.build(photoId = null)
        val memberDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.MEMBER,
                modelId = member.id)

        useCase.execute(member, true)

        verify(mockMemberRepository).upsert(member, listOf(memberDelta))
    }

    @Test
    fun execute_memberDoesNotHavePhoto_createsMemberAndMemberWithoutDelta() {
        val member = MemberFactory.build(photoId = null)

        useCase.execute(member, false)

        verify(mockMemberRepository).upsert(member, emptyList())
    }

    @Test
    fun execute_memberHasPhoto_createsMemberAndMemberAndPhotoDeltas() {
        val member = MemberFactory.build(photoId = UUID.randomUUID())
        val memberDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.MEMBER,
                modelId = member.id
        )
        val photoDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PHOTO,
                modelId = member.id
        )

        useCase.execute(member, true)

        verify(mockMemberRepository).upsert(member, listOf(memberDelta, photoDelta))
    }

    @Test
    fun execute_memberHasPhoto_createsMemberAndMemberAndPhotoWithoutDeltas() {
        val member = MemberFactory.build(photoId = UUID.randomUUID())

        useCase.execute(member, false)

        verify(mockMemberRepository).upsert(member, emptyList())
    }
}
