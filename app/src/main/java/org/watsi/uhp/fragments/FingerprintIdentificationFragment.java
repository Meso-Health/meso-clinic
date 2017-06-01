package org.watsi.uhp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.SimHelper;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class FingerprintIdentificationFragment extends BaseFragment {
    private static int SIMPRINTS_IDENTIFICATION_INTENT = 1;

    private Member mMember;
    private TextView mResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.identification_fingerprint_label);

        View view = inflater.inflate(R.layout.fragment_fingerprint_identification, container, false);
        mResults = (TextView) view.findViewById(R.id.fingerprint_identification_results);
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);

        SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, getSessionManager().getCurrentLoggedInUsername());
        Intent fingerprintIdentificationIntent = simHelper.identify(BuildConfig.PROVIDER_ID.toString());

        startActivityForResult(
                fingerprintIdentificationIntent,
                SIMPRINTS_IDENTIFICATION_INTENT
        );


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(
                    getContext(),
                    "result not OK",
                    Toast.LENGTH_LONG).show();
        } else if (requestCode == SIMPRINTS_IDENTIFICATION_INTENT) {
            // Get rid of progress bar
            ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.INVISIBLE);

            ArrayList<Identification> identifications =
                    data.getParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS);
            String result = "";
            for (Identification id : identifications) {
                String memberName = "unknown";
                try {
                    Member member = MemberDao.findByFingerprintsGuid(UUID.fromString(id.getGuid()));
                    memberName = member.getFullName();
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                }
                result = result + " " + memberName +  " confidence:" + id.getConfidence() +  " tier: " + id.getTier();
                result = result + "\n";
                Log.i("UHP", "Guid is: " + id.getGuid());
            }
            Toast.makeText(getContext(), result, Toast.LENGTH_LONG).show();

            DialogFragment clinicNumberDialog = new ClinicNumberDialogFragment();
            clinicNumberDialog.show(getActivity().getSupportFragmentManager(),
                    "ClinicNumberDialogFragment");

            getNavigationManager().set


        }
    }
}
