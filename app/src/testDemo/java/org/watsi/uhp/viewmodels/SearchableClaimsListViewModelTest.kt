package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.managers.Logger
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.usecases.LoadClaimsUseCase
import org.watsi.domain.usecases.SubmitClaimUseCase
import org.watsi.uhp.testutils.AACBaseTest

class SearchableClaimsListViewModelTest : AACBaseTest() {
    private lateinit var viewModel: SearchableClaimsListViewModel
    private lateinit var observable: LiveData<SearchableClaimsListViewModel.ViewState>
    @Mock lateinit var mockLogger: Logger
    @Mock lateinit var mockSubmitClaimUseCase: SubmitClaimUseCase
    @Mock lateinit var mockLoadClaimUseCase: LoadClaimsUseCase

    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val member1 = MemberFactory.build(membershipNumber = "01/11/11/P-11111/22", medicalRecordNumber = "89463")
    private val claim1 = EncounterWithExtrasFactory.build(member = member1)
    private val member2 = MemberFactory.build(membershipNumber = "01/11/11/P-110234/26", medicalRecordNumber = "89400")
    private val claim2 = EncounterWithExtrasFactory.build(member = member2)
    private val member3 = MemberFactory.build(membershipNumber = "01/11/11/P-212310/22", medicalRecordNumber = "234008")
    private val claim3 = EncounterWithExtrasFactory.build(member = member3)

    private val initialViewState = SearchableClaimsListViewModel.ViewState(
        claims = listOf(
            claim1,
            claim2,
            claim3),
        visibleClaims = listOf(
            claim1,
            claim2,
            claim3
        )
    )

    @Before
    fun setup() {
        whenever(mockLoadClaimUseCase.execute()).thenReturn(Flowable.just(listOf(
            claim1,
            claim2,
            claim3
        )))

        viewModel = SearchableClaimsListViewModel(
            mockSubmitClaimUseCase,
            mockLogger,
            clock)
        observable = viewModel.getObservable(mockLoadClaimUseCase)
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
            visibleClaims = listOf(claim1, claim2)
        ))
        viewModel.filterClaimsBySearchText("8946")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(claim1)
        ))
    }


    @Test
    fun filterClaimsBySearchText_atLeast3Characters_matchMembershipNumber() {
        viewModel.filterClaimsBySearchText("1111")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(claim1)
        ))
    }

    @Test
    fun filterClaimsBySearchText_atLeast3Characters_matchMRNandMembershipNumber() {
        viewModel.filterClaimsBySearchText("234")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(claim2, claim3)
        ))
        viewModel.filterClaimsBySearchText("2340")
        Assert.assertEquals(observable.value, initialViewState.copy(
            visibleClaims = listOf(claim3)
        ))
    }
}
