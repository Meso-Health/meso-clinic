package org.watsi.uhp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.listeners.CapturePhotoClickListener;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;

import java.io.IOException;

public class EncounterFormFragment extends Fragment {

    static final int ENCOUNTER_FORM_PHOTO_INTENT = 6;

    private ImageView mEncounterFormImageView;
    private Uri mUri;
    private Encounter mEncounter;
    private EncounterForm mEncounterForm = new EncounterForm();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.encounter_form_fragment_label);

        mEncounter = (Encounter) getArguments().getSerializable(NavigationManager.ENCOUNTER_BUNDLE_FIELD);

        View view = inflater.inflate(R.layout.fragment_capture_photo, container, false);

        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.encounter_form_fragment_label);
        try {
            String filename = "encounter_form_" + Clock.getCurrentTime().getTime() + ".jpg";
            mUri = FileManager.getUriFromProvider(filename, "encounter", getContext());
        } catch (IOException e) {
            Rollbar.reportException(e);
            new NavigationManager(getActivity()).setReceiptFragment(mEncounter);
            Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }

        view.findViewById(R.id.photo_btn).setOnClickListener(
                new CapturePhotoClickListener(ENCOUNTER_FORM_PHOTO_INTENT, this, mUri));

        mEncounterFormImageView = (ImageView) view.findViewById(R.id.photo);

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEncounterForm.getUrl() != null) {
                    mEncounter.addEncounterForm(mEncounterForm);
                }
                new NavigationManager(getActivity()).setReceiptFragment(mEncounter);
            }
        });

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENCOUNTER_FORM_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                mEncounterFormImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Rollbar.reportException(e);
            }

            mEncounterForm.setUrl(mUri.toString());
        } else {
            Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
        }
    }
}
