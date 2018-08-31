package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Member
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.utils.Age
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.R
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.testutils.AACBaseTest
import java.util.UUID

class MemberInformationViewModelTest : AACBaseTest() {
    private lateinit var viewModel: MemberInformationViewModel
    private lateinit var observable: LiveData<MemberInformationViewModel.ViewState>

    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val memberId = UUID.randomUUID()
    private val encounter = Encounter(UUID.randomUUID(), memberId, null, Instant.now(clock))
    private val encounterFlowState = EncounterFlowState(encounter, emptyList(), emptyList(), emptyList())
    private val membershipNumber = "01/01/06/P-692/3"
    private val initialViewState = MemberInformationViewModel.ViewState(membershipNumber)

    @Before
    fun setup() {
        viewModel = MemberInformationViewModel(clock)
        observable = viewModel.getObservable(null, membershipNumber)
        observable.observeForever{}
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
        viewModel.onMedicalRecordNumberChange("01292")
        assertEquals(initialViewState.copy(medicalRecordNumber = "01292"), observable.value)
    }

    @Test
    fun onAgeUnitChange() {
        viewModel.onAgeUnitChange(AgeUnit.months)
        assertEquals(initialViewState.copy(ageUnit = AgeUnit.months), observable.value)
    }

    @Test
    fun updateEncounterWithMember_allFieldsMissing() {
        viewModel.updateEncounterWithMember(encounterFlowState).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(
            initialViewState.copy(errors = hashMapOf(
                MemberInformationViewModel.MEMBER_GENDER_ERROR to R.string.gender_validation_error,
                MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_validation_error,
                MemberInformationViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_validation_error
            )),
            observable.value
        )
    }

    @Test
    fun updateEncounterWithMember_blankAge() {
        viewModel.onGenderChange(Member.Gender.F)
        viewModel.onMedicalRecordNumberChange("09900")
        viewModel.updateEncounterWithMember(encounterFlowState).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(
            initialViewState.copy(
                age = null,
                ageUnit = AgeUnit.years,
                gender = Member.Gender.F,
                medicalRecordNumber = "09900",
                errors = hashMapOf(
                    MemberInformationViewModel.MEMBER_AGE_ERROR to R.string.age_validation_error
                )
            ),
            observable.value
        )
    }

    @Test
    fun updateEncounterWithMember_genderMissing() {
        viewModel.onAgeChange(10)
        viewModel.onAgeUnitChange(AgeUnit.months)
        viewModel.onMedicalRecordNumberChange("09900")
        viewModel.updateEncounterWithMember(encounterFlowState).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(
            initialViewState.copy(
                age = 10,
                ageUnit = AgeUnit.months,
                gender = null,
                medicalRecordNumber = "09900",
                errors = hashMapOf(
                    MemberInformationViewModel.MEMBER_GENDER_ERROR to R.string.gender_validation_error
                )
            ),
            observable.value
        )
    }

    @Test
    fun updateEncounterWithMember_blankMedicalRecordNumber() {
        setValidViewStateOnViewModel()
        viewModel.onMedicalRecordNumberChange("")
        viewModel.updateEncounterWithMember(encounterFlowState).test().assertError(
            MemberInformationViewModel.ValidationException::class.java
        )
        assertEquals(
            initialViewState.copy(
                age = 10,
                ageUnit = AgeUnit.months,
                gender = Member.Gender.F,
                medicalRecordNumber = "",
                errors = hashMapOf(
                    MemberInformationViewModel.MEMBER_MEDICAL_RECORD_NUMBER_ERROR to R.string.medical_record_number_validation_error
                )
            ),
            observable.value
        )
    }

    @Test
    fun updateEncounterWithMember_validViewState() {
        setValidViewStateOnViewModel()
        val birthdateWithAccuracy = Age(10, AgeUnit.months).toBirthdateWithAccuracy(clock)
        viewModel.updateEncounterWithMember(encounterFlowState).test().assertValue(
            encounterFlowState.copy(
                member = MemberFactory.build(
                    id = memberId,
                    name = "Member Name",
                    enrolledAt = clock.instant(),
                    gender = Member.Gender.F,
                    membershipNumber = membershipNumber,
                    medicalRecordNumber = "09900",
                    birthdate = birthdateWithAccuracy.first,
                    birthdateAccuracy = birthdateWithAccuracy.second,
                    householdId = null
                )
            )
        )
    }

    private fun setValidViewStateOnViewModel() {
        viewModel.onAgeChange(10)
        viewModel.onGenderChange(Member.Gender.F)
        viewModel.onAgeUnitChange(AgeUnit.months)
        viewModel.onMedicalRecordNumberChange("09900")
    }
}
