package org.watsi.device.managers

import android.content.Intent
import android.support.v4.app.Fragment
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.SimHelper
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import java.util.UUID

class SimprintsManager (private val simHelper: SimHelper, private val sessionManager: SessionManager) : FingerprintManager {

    companion object {
        private val BAD_SCAN_THRESHOLD = Tier.TIER_5
    }

    /**
     * Sends an intent to Simprints to register a member's fingerprints.
     *
     * @return true if the intent is sent and there is an activity to handle it, false otherwise.
     */
    override fun captureFingerprint(memberId: String, fragment: Fragment, requestCode: Int): Boolean {
        val metadata: Metadata = createMetadataWithMemberId(memberId)
        val providerId = sessionManager.currentToken()?.user?.providerId.toString()
        val captureFingerprintIntent: Intent = simHelper.register(providerId, metadata)
        return if (captureFingerprintIntent.resolveActivity(fragment.activity.packageManager) != null) {
            fragment.startActivityForResult(captureFingerprintIntent, requestCode)
            true
        } else {
            false
        }
    }

    /**
     * Sends an intent to Simprints to verify a member's fingerprints.
     *
     * @return true if the intent is sent and there is an activity to handle it, false otherwise
     */
    override fun verifyFingerprint(guid: String, fragment: Fragment, requestCode: Int): Boolean {
        val providerId = sessionManager.currentToken()?.user?.providerId.toString()
        val verifyFingerprintIntent: Intent = simHelper.verify(providerId, guid)
        return if (verifyFingerprintIntent.resolveActivity(fragment.activity.packageManager) != null) {
            fragment.startActivityForResult(verifyFingerprintIntent, requestCode)
            true
        } else {
            false
        }
    }

    /**
     * Parses the response from Simprints after a register call is sent.
     *
     * @return FingerprintRequestResponse with the parsed response status, fingerprint UUID if the status
     * is success.
     */
    override fun parseResponseForRegistration(resultCode: Int, data: Intent?): FingerprintManager.FingerprintRequestResponse {
        return when (resultCode) {
            Constants.SIMPRINTS_OK -> {
                val registration = data?.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
                if (registration?.guid != null) {
                    FingerprintManager.FingerprintRequestResponse(
                            FingerprintManager.FingerprintStatus.SUCCESS,
                            UUID.fromString(registration.guid)
                    )
                } else {
                    FingerprintManager.FingerprintRequestResponse(
                            FingerprintManager.FingerprintStatus.FAILURE)
                }
            }
            Constants.SIMPRINTS_CANCELLED -> {
                FingerprintManager.FingerprintRequestResponse(
                        FingerprintManager.FingerprintStatus.CANCELLED)
            }
            else -> {
                FingerprintManager.FingerprintRequestResponse(
                        FingerprintManager.FingerprintStatus.FAILURE)
            }
        }
    }

    /**
     * Parses the response from Simprints after a verification call is sent.
     *
     * @return FingerprintVerificationResponse with parsed response status, resultCode, confidence, and tier
     */
    override fun parseResponseForVerification(resultCode: Int, data: Intent?): FingerprintManager.FingerprintVerificationResponse {
        return when (resultCode) {
            Constants.SIMPRINTS_OK -> {
                val verification = data?.getParcelableExtra<Verification>(Constants.SIMPRINTS_VERIFICATION)
                if (verification?.guid != null) {
                    FingerprintManager.FingerprintVerificationResponse(
                            FingerprintManager.FingerprintStatus.SUCCESS,
                            verification.confidence,
                            verification.tier.toString(),
                            verification.tier == BAD_SCAN_THRESHOLD
                    )
                } else {
                    FingerprintManager.FingerprintVerificationResponse(
                            FingerprintManager.FingerprintStatus.FAILURE)
                }
            }
            Constants.SIMPRINTS_CANCELLED -> {
                FingerprintManager.FingerprintVerificationResponse(
                        FingerprintManager.FingerprintStatus.CANCELLED)
            }
            else -> {
                FingerprintManager.FingerprintVerificationResponse(
                        FingerprintManager.FingerprintStatus.FAILURE)
            }
        }
    }

    /**
     * Creates metadata with the member's id to send to Simprints, thus linking the fingerprints
     * with the members on their end. This is a safety precaution in case we fail to link the
     * fingerprints and members correctly on our end.
     */
    fun createMetadataWithMemberId(memberId: String): Metadata {
        return Metadata().put("memberId", memberId)
    }
}
