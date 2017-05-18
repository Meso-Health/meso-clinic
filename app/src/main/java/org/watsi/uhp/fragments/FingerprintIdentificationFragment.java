package org.watsi.uhp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Identification;
import com.simprints.libsimprints.SimHelper;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class FingerprintIdentificationFragment extends BaseFragment {
    private static int SIMPRINTS_IDENTIFICATION_INTENT = 1;

    private TextView mResults;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.identification_fingerprint_label);

        View view = inflater.inflate(R.layout.fragment_fingerprint_identification, container, false);
        mResults = (TextView) view.findViewById(R.id.fingerprint_identification_results);

        view.findViewById(R.id.scan_fingerprints).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, getSessionManager().getCurrentLoggedInUsername());
                Intent fingerprintIdentificationIntent = simHelper.identify(BuildConfig.PROVIDER_ID.toString());
                startActivityForResult(
                        fingerprintIdentificationIntent,
                        SIMPRINTS_IDENTIFICATION_INTENT
                );
            }
        });
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(
                    getContext(),
                    "result not OK",
                    Toast.LENGTH_LONG).show();
        } else {
            ArrayList<Identification> identifications =
                    data.getParcelableArrayListExtra(Constants.SIMPRINTS_IDENTIFICATIONS);
            for (Identification id : identifications) {
                id.getGuid();
                id.getConfidence();
                id.getTier();
            }
            mResults.setText(identifications.toString());
        }
    }
}
