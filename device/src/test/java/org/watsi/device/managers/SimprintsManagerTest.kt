package org.watsi.device.managers

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.SimHelper
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.factories.AuthenticationTokenFactory
import java.util.UUID


@RunWith(MockitoJUnitRunner::class)
class SimprintsManagerTest {
    @Mock lateinit var mockSimHelper: SimHelper
    @Mock lateinit var mockFragment: Fragment
    @Mock lateinit var mockActivity: FragmentActivity
    @Mock lateinit var mockIntent: Intent
    @Mock lateinit var mockMetadata: Metadata
    @Mock lateinit var mockRegistration: Registration
    @Mock lateinit var mockVerfication: Verification
    @Mock lateinit var mockPackageManager: PackageManager
    @Mock lateinit var mockComponentName: ComponentName
    @Mock lateinit var mockSessionManager: SessionManager

    lateinit var simprintsManager: SimprintsManager

    @Before
    fun setup() {
        simprintsManager = SimprintsManager(mockSimHelper, mockSessionManager)

        whenever(mockFragment.activity).thenReturn(mockActivity)
    }

    @Test
    fun captureFingerprint_intentIsValid_sendsIntentToSimprintsAndReturnsTrue() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"
        val requestCode = 0
        val authToken = AuthenticationTokenFactory.build()

        doReturn(mockMetadata).whenever(spySimprintsManager).createMetadataWithMemberId(any())
        whenever(mockSimHelper.register(any(), eq(mockMetadata))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(mockComponentName)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)

        assertEquals(spySimprintsManager.captureFingerprint(memberId, mockFragment, requestCode), true)
        verify(mockFragment).startActivityForResult(mockIntent, requestCode)
    }

    @Test
    fun captureFingerprint_intentIsInvalid_returnsFalse() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"
        val authToken = AuthenticationTokenFactory.build()

        doReturn(mockMetadata).whenever(spySimprintsManager).createMetadataWithMemberId(any())
        whenever(mockSimHelper.register(any(), eq(mockMetadata))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(null)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)

        assertEquals(spySimprintsManager.captureFingerprint(memberId, mockFragment, 0), false)
    }

    @Test
    fun verifyFingerprint_intentIsValid_sendsIntentToSimprintsAndReturnsTrue() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"
        val authToken = AuthenticationTokenFactory.build()
        val requestCode = 0

        whenever(mockSimHelper.verify(any(), eq(memberId))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(mockComponentName)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)

        assertEquals(spySimprintsManager.verifyFingerprint(memberId, mockFragment, requestCode), true)
        verify(mockFragment).startActivityForResult(mockIntent, requestCode)
    }

    @Test
    fun verifyFingerprint_intentIsInvalid_returnsFalse() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"
        val authToken = AuthenticationTokenFactory.build()

        whenever(mockSimHelper.verify(any(), eq(memberId))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(null)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)

        assertEquals(spySimprintsManager.verifyFingerprint(memberId, mockFragment, 0), false)
    }

    @Test
    fun parseResponseForRegistration_statusIsOkay_responseContainsId_returnsSuccessResponseAndId() {
        val fingerprintId = UUID.randomUUID().toString()
        val successResponse = FingerprintManager.FingerprintRequestResponse(
                FingerprintManager.FingerprintStatus.SUCCESS,
                UUID.fromString(fingerprintId))

        whenever(mockIntent.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION))
                .thenReturn(mockRegistration)
        whenever(mockRegistration.guid).thenReturn(fingerprintId)

        assertEquals(simprintsManager.parseResponseForRegistration(Constants.SIMPRINTS_OK, mockIntent), successResponse)
    }

    @Test
    fun parseResponseForRegistration_statusIsOkay_responseDoesNotContainId_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintRequestResponse(
                FingerprintManager.FingerprintStatus.FAILURE)

        whenever(mockIntent.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION))
                .thenReturn(mockRegistration)
        whenever(mockRegistration.guid).thenReturn(null)

        assertEquals(simprintsManager.parseResponseForRegistration(Constants.SIMPRINTS_OK, mockIntent), failureResponse)

    }

    @Test
    fun parseResponseForRegistration_statusIsCanceled_returnsCancelledResponse() {
        val cancelledResponse = FingerprintManager.FingerprintRequestResponse(
                FingerprintManager.FingerprintStatus.CANCELLED)

        assertEquals(simprintsManager.parseResponseForRegistration(Constants.SIMPRINTS_CANCELLED, mockIntent), cancelledResponse)
    }

    @Test
    fun parseResponseForRegistration_statusIsNotOkayOrCanceled_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintRequestResponse(
                FingerprintManager.FingerprintStatus.FAILURE)

        assertEquals(simprintsManager.parseResponseForRegistration(Constants.SIMPRINTS_MISSING_API_KEY, mockIntent), failureResponse)
    }

    @Test
    fun parseResponseForVerification_statusIsOkay_responseContainsResults_returnsSuccessResponseAndResults() {
        val guid = "1234"
        val confidence = 133F
        val tier = Tier.TIER_1
        val badScan = false
        val successResponse = FingerprintManager.FingerprintVerificationResponse(
                FingerprintManager.FingerprintStatus.SUCCESS,
                confidence,
                tier.toString(),
                badScan
        )

        whenever(mockIntent.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION))
                .thenReturn(mockVerfication)
        whenever(mockVerfication.guid).thenReturn(guid)
        whenever(mockVerfication.confidence).thenReturn(confidence)
        whenever(mockVerfication.tier).thenReturn(tier)

        assertEquals(simprintsManager.parseResponseForVerification(Constants.SIMPRINTS_OK, mockIntent), successResponse)
    }

    @Test
    fun parseResponseForVerification_statusIsOkay_responseContainsBadScan_returnsSuccessResponseAndBadScan() {
        val guid = "1234"
        val confidence = 2F
        val tier = Tier.TIER_5
        val badScan = true
        val successResponse = FingerprintManager.FingerprintVerificationResponse(
                FingerprintManager.FingerprintStatus.SUCCESS,
                confidence,
                tier.toString(),
                badScan)

        whenever(mockIntent.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION))
                .thenReturn(mockVerfication)
        whenever(mockVerfication.guid).thenReturn(guid)
        whenever(mockVerfication.confidence).thenReturn(confidence)
        whenever(mockVerfication.tier).thenReturn(tier)

        assertEquals(simprintsManager.parseResponseForVerification(Constants.SIMPRINTS_OK, mockIntent), successResponse)
    }

    @Test
    fun parseResponseForVerification__statusIsOkay_responseDoesNotContainResultCode_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintVerificationResponse(
                FingerprintManager.FingerprintStatus.FAILURE)

        whenever(mockIntent.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION))
                .thenReturn(mockVerfication)
        whenever(mockVerfication.guid).thenReturn(null)

        assertEquals(simprintsManager.parseResponseForVerification(Constants.SIMPRINTS_OK, mockIntent), failureResponse)

    }

    @Test
    fun parseResponseForVerification__statusIsCanceled_returnsCancelledResponse() {
        val cancelledResponse = FingerprintManager.FingerprintVerificationResponse(
                FingerprintManager.FingerprintStatus.CANCELLED)

        assertEquals(simprintsManager.parseResponseForVerification(Constants.SIMPRINTS_CANCELLED, mockIntent), cancelledResponse)
    }

    @Test
    fun parseResponseForVerification__statusIsNotOkayOrCanceled_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintVerificationResponse(
                FingerprintManager.FingerprintStatus.FAILURE)

        assertEquals(simprintsManager.parseResponseForVerification(Constants.SIMPRINTS_MISSING_API_KEY, mockIntent), failureResponse)
    }
}
