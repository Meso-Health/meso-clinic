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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID


@RunWith(MockitoJUnitRunner::class)
class SimprintsManagerTest {
    @Mock
    lateinit var mockSimHelper: SimHelper
    @Mock
    lateinit var mockFragment: Fragment
    @Mock
    lateinit var mockActivity: FragmentActivity
    @Mock
    lateinit var mockIntent: Intent
    @Mock
    lateinit var mockMetadata: Metadata
    @Mock
    lateinit var mockRegistration: Registration
    @Mock
    lateinit var mockPackageManager: PackageManager
    @Mock
    lateinit var mockComponentName: ComponentName

    lateinit var simprintsManager: SimprintsManager

    @Before
    fun setup() {
        simprintsManager = SimprintsManager(mockSimHelper)
        whenever(mockFragment.activity).thenReturn(mockActivity)
    }

    @Test
    fun captureFingerprint_intentIsValid_sendsIntentToSimprintsAndReturnsTrue() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"
        val requestCode = 0

        doReturn(mockMetadata).whenever(spySimprintsManager).createMetadataWithMemberId(any())
        whenever(mockSimHelper.register(any(), eq(mockMetadata))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(mockComponentName)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)

        Assert.assertEquals(spySimprintsManager.captureFingerprint(memberId, mockFragment, requestCode), true)
        verify(mockFragment).startActivityForResult(mockIntent, requestCode)
    }

    @Test
    fun captureFingerprint_intentIsInvalid_returnsFalse() {
        val spySimprintsManager = spy(simprintsManager)
        val memberId = "1234"

        doReturn(mockMetadata).whenever(spySimprintsManager).createMetadataWithMemberId(any())
        whenever(mockSimHelper.register(any(), eq(mockMetadata))).thenReturn(mockIntent)
        whenever(mockIntent.resolveActivity(mockPackageManager)).thenReturn(null)
        whenever(mockActivity.packageManager).thenReturn(mockPackageManager)

        Assert.assertEquals(spySimprintsManager.captureFingerprint(memberId, mockFragment, 0), false)
    }

    @Test
    fun parseResponse_resultCodeIsOkay_responseContainsId_returnsSuccessResponseAndId() {
        val fingerprintId = UUID.randomUUID().toString()
        val successResponse = FingerprintManager.FingerprintResponse(
                FingerprintManager.FingerprintStatus.SUCCESS,
                UUID.fromString(fingerprintId))

        whenever(mockIntent.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION))
                .thenReturn(mockRegistration)
        whenever(mockRegistration.guid).thenReturn(fingerprintId)

        Assert.assertEquals(simprintsManager.parseResponse(Constants.SIMPRINTS_OK, mockIntent), successResponse)
    }

    @Test
    fun parseResponse_resultCodeIsOkay_responseDoesNotContainId_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintResponse(
                FingerprintManager.FingerprintStatus.FAILURE,null)

        whenever(mockIntent.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION))
                .thenReturn(mockRegistration)
        whenever(mockRegistration.guid).thenReturn(null)

        Assert.assertEquals(simprintsManager.parseResponse(Constants.SIMPRINTS_OK, mockIntent), failureResponse)

    }

    @Test
    fun parseResponse_resultCodeIsCanceled_returnsCancelledResponse() {
        val cancelledResponse = FingerprintManager.FingerprintResponse(
                FingerprintManager.FingerprintStatus.CANCELLED,null)

        Assert.assertEquals(simprintsManager.parseResponse(Constants.SIMPRINTS_CANCELLED, mockIntent), cancelledResponse)
    }

    @Test
    fun parseResponse_resultCodeIsNotOkayOrCanceled_returnsFailedResponse() {
        val failureResponse = FingerprintManager.FingerprintResponse(
                FingerprintManager.FingerprintStatus.FAILURE,null)

        Assert.assertEquals(simprintsManager.parseResponse(Constants.SIMPRINTS_MISSING_API_KEY, mockIntent), failureResponse)
    }
}
