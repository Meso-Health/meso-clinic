package org.watsi.uhp.helpers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.view.View;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Metadata;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.SimHelper;
import com.simprints.libsimprints.Verification;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.fragments.EnrollmentFingerprintFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Member;

import java.util.UUID;

import static android.app.Activity.RESULT_OK;

/**
 * Created by michaelliang on 6/27/17.
 */

public class SimprintsHelper {
    public static int SIMPRINTS_ENROLLMENT_INTENT = 3;
    public static int SIMPRINTS_VERIFICATION_INTENT = 1;

    SimHelper mSimHelper;
    Fragment mFragment;

    public SimprintsHelper(String currentUserName, Fragment fragment) {
        mSimHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, currentUserName);
        mFragment = fragment;
    }

    public void enrollMember(String providerId, String memberId) throws SimprintsHelperException {
        Metadata metadata = new Metadata().put("memberId", memberId);
        Intent captureFingerprintIntent = mSimHelper.register(providerId, metadata);

        if (validIntent(captureFingerprintIntent)) {
            mFragment.startActivityForResult(
                    captureFingerprintIntent,
                    SIMPRINTS_VERIFICATION_INTENT
            );
        } else {
            throw new SimprintsInvalidIntentException("Invalid enrollment intent. Check if simprints is installed.");
        }
    }

    public void verify(String providerId, UUID fingerprintsGuid) throws SimprintsHelperException {
        Intent fingerprintVerificationIntent = mSimHelper.verify(providerId, fingerprintsGuid.toString());
        if (validIntent(fingerprintVerificationIntent)) {
            mFragment.startActivityForResult(
                    fingerprintVerificationIntent,
                    SIMPRINTS_VERIFICATION_INTENT
            );
        } else {
            throw new SimprintsInvalidIntentException("Invalid verify intent. Check if simprints is installed.");
        }
    }

    protected boolean validIntent(Intent intent) {
        PackageManager packageManager = mFragment.getActivity().getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            return true;
        } else {
            return false;
        }
    }


    public UUID onActivityResultFromEnroll(int requestCode, int resultCode, Intent data) throws SimprintsHelperException {
        if (requestCode != SIMPRINTS_VERIFICATION_INTENT) {
            throw new SimprintsInvalidIntentException("RequestCode in simprints verification call was from a different intent. Actual request code was: " + requestCode);
        } else {
            if (resultCode == Constants.SIMPRINTS_OK) {
                Registration registration = data.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION);
                if (registration == null || registration.getGuid() == null) {
                    throw new SimprintsRegistrationError("Simprints registration is null after call to register.");
                } else {
                    return UUID.fromString(registration.getGuid());
                }
            } else if (resultCode == Constants.SIMPRINTS_CANCELLED) {
                return null;
            } else {
                throw new SimprintsErrorResultCodeException("Call to Simprints enrollment returned a resultCode of " + resultCode);
            }
        }
    }

    public Verification onActivityResultFromVerify(int requestCode, int resultCode, Intent data) throws SimprintsHelperException {
        if (requestCode != SIMPRINTS_VERIFICATION_INTENT) {
            throw new SimprintsInvalidIntentException("RequestCode in simprints verification call was from a different intent. Actual request code was: " + requestCode);
        } else {
            if (resultCode == Constants.SIMPRINTS_OK) {
                Verification verification =
                        data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
                return verification;
            } else if (resultCode == Constants.SIMPRINTS_CANCELLED) {
                return null;
            } else {
                throw new SimprintsErrorResultCodeException("Call to Simprints verification returned a resultCode of " + resultCode);
            }
        }
    }

    public class SimprintsHelperException extends Throwable {
        public SimprintsHelperException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class SimprintsInvalidIntentException extends SimprintsHelperException {
        public SimprintsInvalidIntentException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class SimprintsErrorResultCodeException extends SimprintsHelperException {
        public SimprintsErrorResultCodeException(String errorMessage) {
            super(errorMessage);
        }
    }

    public class SimprintsRegistrationError extends SimprintsHelperException {
        public SimprintsRegistrationError(String errorMessage) {
            super(errorMessage);
        }
    }
}
