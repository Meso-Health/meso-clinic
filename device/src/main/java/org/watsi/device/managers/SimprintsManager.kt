package org.watsi.device.managers

import android.content.Intent
import android.support.v4.app.Fragment
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.SimHelper
import java.util.UUID

class SimprintsManager (private val simHelper: SimHelper) : FingerprintManager {
    /**
     * Sends an intent to Simprints to register a member's fingerprints.
     *
     * @return true if the intent is sent and there is an activity to handle it, false otherwise.
     */
    override fun captureFingerprint(memberId: String, fragment: Fragment, requestCode: Int): Boolean {
        val metadata: Metadata = createMetadataWithMemberId(memberId)
        // TODO: decide where to get providerId from (e.g. "enrollment period" model)
        val captureFingerprintIntent: Intent = simHelper.register("1", metadata)
        return if (captureFingerprintIntent.resolveActivity(fragment.activity.packageManager) != null) {
            fragment.startActivityForResult(captureFingerprintIntent, requestCode)
            true
        } else {
            false
        }
    }

    /**
     * Parses the response from Simprints after a register call is sent.
     *
     * @return FingerprintResponse with the parsed response status, fingerprint UUID if the status
     * is success.
     */
    override fun parseResponse(resultCode: Int, data: Intent?): FingerprintManager.FingerprintResponse {
        return when (resultCode) {
            Constants.SIMPRINTS_OK -> {
                val registration = data?.getParcelableExtra<Registration>(Constants.SIMPRINTS_REGISTRATION)
                if (registration?.guid != null) {
                    FingerprintManager.FingerprintResponse(
                            FingerprintManager.FingerprintStatus.SUCCESS,
                            UUID.fromString(registration.guid)
                    )
                } else {
                    // TODO: report to Rollbar
                    FingerprintManager.FingerprintResponse(
                            FingerprintManager.FingerprintStatus.FAILURE,
                            null
                    )
                }
            }
            Constants.SIMPRINTS_CANCELLED -> {
                FingerprintManager.FingerprintResponse(
                        FingerprintManager.FingerprintStatus.CANCELLED,
                        null
                )
            }
            else -> {
                // TODO: report to Rollbar
                FingerprintManager.FingerprintResponse(
                        FingerprintManager.FingerprintStatus.FAILURE,
                        null
                )
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