package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.managers.Logger
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.usecases.LoadPendingClaimsUseCase
import org.watsi.domain.usecases.SubmitClaimUseCase
import org.watsi.uhp.testutils.AACBaseTest

class PendingClaimsViewModelTest : AACBaseTest() {
    private lateinit var viewModel: PendingClaimsViewModel
    private lateinit var observable: LiveData<PendingClaimsViewModel.ViewState>
    @Mock
    lateinit var mockLogger: Logger
    @Mock
    lateinit var mockEncounterRepository: EncounterRepository
    @Mock
    lateinit var mockLoadPendingClaimsUseCase: LoadPendingClaimsUseCase
    @Mock
    lateinit var mockSubmitClaimUseCase: SubmitClaimUseCase

    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val member1 = MemberFactory.build(membershipNumber = "01/11/11/P-11111/22", medicalRecordNumber = "89463")
    private val pendingClaim1 = EncounterWithExtrasFactory.build(member = member1)
    private val member2 = MemberFactory.build(membershipNumber = "01/11/11/P-110234/26", medicalRecordNumber = "89400")
    private val pendingClaim2 = EncounterWithExtrasFactory.build(member = member2)
    private val member3 = MemberFactory.build(membershipNumber = "01/11/11/P-212310/22", medicalRecordNumber = "234008")
    private val pendingClaim3 = EncounterWithExtrasFactory.build(member = member3)

    private val initialViewState = PendingClaimsViewModel.ViewState(
        claims = listOf(
            pendingClaim1,
            pendingClaim2,
            pendingClaim3),
        visibleClaims = listOf(
            pendingClaim1,
            pendingClaim2,
            pendingClaim3
        )
    )

    @Before
    fun setup() {
        whenever(mockEncounterRepository.loadPendingClaims()).thenReturn(Flowable.just(listOf(
            pendingClaim1,
            pendingClaim2,
            pendingClaim3
        )))

        mockLoadPendingClaimsUseCase = LoadPendingClaimsUseCase(mockEncounterRepository)
        viewModel = PendingClaimsViewModel(
            mockLoadPendingClaimsUseCase,
            mockSubmitClaimUseCase,
            mockLogger,
            clock)
        observable = viewModel.getObservable()
        observable.observeForever{}
    }

    @Test
    fun init() {
        Assert.assertEquals(initialViewState, observable.value)
    }

    @Test
    fun filterClaimsBySearchText_lessThan3Characters_allClaims() {
        viewModel.filterClaimsBySearchText("0")
        Assert.assertEquals(observable.value, initialViewState.copy())
        viewModel.filterClaimsBySearchText("08")
        Assert.assertEquals(observable.value, initialViewState.copy())
    }

    @Test
    fun filterClaimsBySearchText_atLeast3Characters_matchMRN() {
        viewModel.filterClaimsBySearchText("894")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(pendingClaim1, pendingClaim2)
        ))
        viewModel.filterClaimsBySearchText("8946")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(pendingClaim1)
        ))
    }


    @Test
    fun filterClaimsBySearchText_atLeast3Characters_matchCBHID() {
        viewModel.filterClaimsBySearchText("1111")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(pendingClaim1)
        ))
    }

    @Test
    fun filterClaimsBySearchText_atLeast3Characters_matchMRNandCBHID() {
        viewModel.filterClaimsBySearchText("234")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(pendingClaim2, pendingClaim3)
        ))
        viewModel.filterClaimsBySearchText("2340")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(pendingClaim3)
        ))
    }
}
