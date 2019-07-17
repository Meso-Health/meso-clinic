package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.User
import org.watsi.domain.factories.UserFactory
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.R
import org.watsi.uhp.testutils.AACBaseTest

class MemberInformationViewModelTest : AACBaseTest() {
    private lateinit var viewModel: MemberInformationViewModel
    private lateinit var observable: LiveData<MemberInformationViewModel.ViewState>
    @Mock lateinit var mockCreateMemberUseCase: CreateMemberUseCase
    @Mock lateinit var mockCreateIdentificationEventUseCase: CreateIdentificationEventUseCase
    @Mock lateinit var mockCreateEncounterUseCase: CreateEncounterUseCase

    private val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    private val membershipNumber = "01/01/06/P-692/3"
    private val initialViewState = MemberInformationViewModel.ViewState()
    private val validViewState = MemberInformationViewModel.ViewState(
        gender = Member.Gender.F,
        name = "Three Name Here",
        age = 10,
        ageUnit = AgeUnit.years,
        medicalRecordNumber = "123456",
        visitReason = Encounter.VisitReason.EMERGENCY
    )
    private val user = UserFactory.build()
    private val hospitalUser = UserFactory.build(providerType = User.ProviderType.GENERAL_HOSPITAL)

    @Before
    fun setup() {
        viewModel = MemberInformationViewModel(
            mockCreateMemberUseCase,
            mockCreateIdentificationEventUseCase,
            mockCreateEncounterUseCase,
            clock
        )
        observable = viewModel.getObservable()
        observable.observeForever{}

        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun getObservable_initialState() {
        assertEquals(initialViewState, observable.value)
    }

    @Test
    fun onAgeChange() {
        viewModel.onAgeChange(10)
        assertEquals(initialViewState.copy(age = 10), observable.value)
    }

    @Test
    fun onGenderChange() {
        viewModel.onGenderChange(Member.Gender.F)
        assertEquals(initialViewState.copy(gender = Member.Gender.F), observable.value)
    }

    @Test
    fun onMedicalRecordNumberChange() {
        viewModel.onMedicalRecordNumberChange("012926")
        assertEquals(initialViewState.copy(medicalRecordNumber = "012926"), observable.value)
    }

    @Test
    fun onAgeUnitChange() {
        viewModel.onAgeUnitChange(AgeUnit.months)
        assertEquals(initialViewState.copy(ageUnit = AgeUnit.months), observable.value)
    }

    @Test
    fun createAndCheckInMember_allFieldsMissing_returnsError() {
        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(
            initialViewState.copy(errors = hashMapOf(
                MemberInformationViewModel.MEMBER_GENDER_ERROR to R.string.gender_validation_error,
                MemberInformationViewModel.MEMBER_NAME_ERROR to R.string.name_validation_error,
                MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_validation_error,
                MemberInformationViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_validation_error
            )),
            observable.value
        )
    }

    @Test
    fun createAndCheckInMember_missingOrBlankName_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onNameChange(null)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_NAME_ERROR to R.string.name_validation_error
        ))

        viewModel.onNameChange("")

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_NAME_ERROR to R.string.name_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_invalidName_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onNameChange("Two Names")

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_NAME_ERROR to R.string.three_names_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_missingAge_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onAgeChange(null)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_invalidAge_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onAgeChange(Member.MAX_AGE + 1)
        viewModel.onAgeUnitChange(AgeUnit.years)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_limit_validation_error
        ))

        viewModel.onAgeUnitChange(AgeUnit.months)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_limit_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_missingGender_returnsError() {
        viewModel.onAgeChange(validViewState.age)
        viewModel.onNameChange(validViewState.name)
        viewModel.onAgeUnitChange(validViewState.ageUnit)
        viewModel.onMedicalRecordNumberChange(validViewState.medicalRecordNumber)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_GENDER_ERROR to R.string.gender_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_missingMedicalRecordNumber_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onMedicalRecordNumberChange(null)

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_invalidMedicalRecordNumber_returnsError() {
        setValidViewStateOnViewModel()
        viewModel.onMedicalRecordNumberChange("1234")

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_length_validation_error
        ))
    }

    @Test
    fun createAndCheckInMember_validViewState_completes() {
        setValidViewStateOnViewModel()
        whenever(mockCreateMemberUseCase.execute(any(), any())).thenReturn(Completable.complete())
        whenever(mockCreateIdentificationEventUseCase.execute(any())).thenReturn(Completable.complete())

        viewModel.createAndCheckInMember(membershipNumber, user).test().assertComplete()
    }

    @Test
    fun createAndCheckInMember_isHospitalUser_missingVisitReason_returnsError() {
        setValidViewStateOnViewModel(hospitalUser = true)
        viewModel.onVisitReasonChange(null)

        viewModel.createAndCheckInMember(membershipNumber, hospitalUser).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(observable.value?.errors, hashMapOf(
            MemberInformationViewModel.VISIT_REASON_ERROR to R.string.missing_visit_reason
        ))
    }

    @Test
    fun createAndCheckInMember_isHospitalUser_validViewState_completes() {
        setValidViewStateOnViewModel(hospitalUser = true)
        whenever(mockCreateMemberUseCase.execute(any(), any())).thenReturn(Completable.complete())
        whenever(mockCreateIdentificationEventUseCase.execute(any())).thenReturn(Completable.complete())
        whenever(mockCreateEncounterUseCase.execute(any(), eq(true), eq(true), eq(clock))).thenReturn(Completable.complete())

        viewModel.createAndCheckInMember(membershipNumber, hospitalUser).test().assertComplete()
    }

    private fun setValidViewStateOnViewModel(hospitalUser: Boolean = false) {
        viewModel.onAgeChange(validViewState.age)
        viewModel.onNameChange(validViewState.name)
        viewModel.onGenderChange(validViewState.gender!!)
        viewModel.onAgeUnitChange(validViewState.ageUnit)
        viewModel.onMedicalRecordNumberChange(validViewState.medicalRecordNumber)
        if (hospitalUser) {
            viewModel.onVisitReasonChange(validViewState.visitReason)
        }
    }
}
