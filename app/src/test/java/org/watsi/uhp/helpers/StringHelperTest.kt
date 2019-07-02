package org.watsi.uhp.helpers

import android.content.Context
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.domain.entities.Member
import org.watsi.domain.factories.MemberFactory
import org.watsi.uhp.R

@RunWith(MockitoJUnitRunner::class)
class StringHelperTest {

    @Mock private lateinit var mockContext: Context

    val today = LocalDate.now()
    val now = Instant.now()
    val fixedClock = Clock.fixed(now, ZoneId.systemDefault())
    val fiveYearsAgo = today.minusYears(5)
    val tenYearsAgo = today.minusYears(10)
    val fiveMonthsAgo = today.minusMonths(5)
    val fiveDaysAgo = today.minusDays(5)

    @Before
    fun setup() {
        whenever(mockContext.getString(R.string.phone_number_prefix)).thenReturn("(0)")
        whenever(mockContext.getString(R.string.male)).thenReturn("Male")
        whenever(mockContext.getString(R.string.female)).thenReturn("Female")
        whenever(mockContext.getString(R.string.years)).thenReturn("years")
        whenever(mockContext.getString(R.string.months)).thenReturn("months")
        whenever(mockContext.getString(R.string.days)).thenReturn("days")
        whenever(mockContext.getString(R.string.middle_dot)).thenReturn("\u00B7")
    }

    @Test
    fun formatAgeAndGender() {
        val m1 = MemberFactory.build(birthdate = tenYearsAgo, gender = Member.Gender.M)
        val m2 = MemberFactory.build(birthdate = fiveYearsAgo, gender = Member.Gender.F)
        val m3 = MemberFactory.build(birthdate = fiveMonthsAgo, gender = Member.Gender.F)
        val m4 = MemberFactory.build(birthdate = fiveDaysAgo, gender = Member.Gender.F)

        assertEquals(StringHelper.formatAgeAndGender(m1, mockContext, fixedClock), "Male · 10 years")
        assertEquals(StringHelper.formatAgeAndGender(m2, mockContext, fixedClock), "Female · 5 years")
        assertEquals(StringHelper.formatAgeAndGender(m3, mockContext, fixedClock), "Female · 5 months")
        assertEquals(StringHelper.formatAgeAndGender(m4, mockContext, fixedClock), "Female · 5 days")
    }

    @Test
    fun formatMembershipInfo() {
        val m1 = MemberFactory.build(membershipNumber = null, medicalRecordNumber = null)
        val m2 = MemberFactory.build(membershipNumber = "01/01/01/P-000001/01", medicalRecordNumber = null)
        val m3 = MemberFactory.build(membershipNumber = null, medicalRecordNumber = "123456")
        val m4 = MemberFactory.build(membershipNumber = "01/01/01/P-000001/01", medicalRecordNumber = "123456")

        assertEquals(StringHelper.formatMembershipInfo(m1, mockContext), "")
        assertEquals(StringHelper.formatMembershipInfo(m2, mockContext), "01/01/01/P-000001/01")
        assertEquals(StringHelper.formatMembershipInfo(m3, mockContext), "123456")
        assertEquals(StringHelper.formatMembershipInfo(m4, mockContext), "01/01/01/P-000001/01 · 123456")
    }

    @Test
    fun formatPhoneNumber() {
        val memberNullPhone = MemberFactory.build(phoneNumber = null)
        val member9Digit = MemberFactory.build(phoneNumber = "775555555")
        val member10Digit = MemberFactory.build(phoneNumber = "0775555555")

        val expectedPhoneNumberString = "(0) 775 555 555"

        assertNull(StringHelper.formatPhoneNumber(memberNullPhone, mockContext))
        assertEquals(
            expectedPhoneNumberString,
            StringHelper.formatPhoneNumber(member9Digit, mockContext)
        )
        assertEquals(
            expectedPhoneNumberString,
            StringHelper.formatPhoneNumber(member10Digit, mockContext)
        )
    }
}
