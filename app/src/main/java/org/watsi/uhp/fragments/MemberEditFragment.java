package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.UUID;

public class MemberEditFragment extends Fragment {

    private Member mMember;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_edit_label);

        View view = inflater.inflate(R.layout.fragment_member_edit, container, false);

        UUID memberId = UUID.fromString(getArguments().getString("memberId"));

        try {
            mMember = MemberDao.findById(memberId);
        } catch (SQLException e) {
            Rollbar.reportException(e);
            new NavigationManager(getActivity()).setCurrentPatientsFragment();
            Toast.makeText(getContext(), R.string.invalid_member_id, Toast.LENGTH_LONG).show();
        }

        final EditText nameView = (EditText) view.findViewById(R.id.member_name);
        nameView.getText().append(mMember.getFullName());
        final EditText cardIdView = (EditText) view.findViewById(R.id.card_id);
        if (mMember.getCardId() != null) {
            cardIdView.getText().append(mMember.getCardId());
        }
        final EditText phoneNumView = (EditText) view.findViewById(R.id.phone_number);
        if (mMember.getPhoneNumber() != null) {
            phoneNumView.getText().append(mMember.getPhoneNumber());
        }

        Button saveBtn = (Button) view.findViewById(R.id.save_button);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(nameView, cardIdView, phoneNumView);
            }
        });

        return view;
    }

    public void saveChanges(EditText nameView, EditText cardIdView, EditText phoneNumView) {
        if (valid(nameView, cardIdView, phoneNumView)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.member_edit_confirmation);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String toastMessage = mMember.getFullName() + "'s information has been updated.";
                    try {
                        mMember.setSynced(false);
                        MemberDao.update(mMember);
                    } catch (SQLException e) {
                        Rollbar.reportException(e);
                        toastMessage = "Failed to update the member information.";
                    }

                    new NavigationManager(getActivity()).setCurrentPatientsFragment();
                    Toast.makeText(
                            getActivity().getApplicationContext(),
                            toastMessage,
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    private boolean valid(EditText nameView, EditText cardIdView, EditText phoneNumView) {
        boolean valid = true;

        try {
            mMember.setFullName(nameView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            nameView.setError(getString(R.string.name_validation_error));
            valid = false;
        }

        try {
            mMember.setCardId(cardIdView.getText().toString());
        } catch (AbstractModel.ValidationException e) {
            cardIdView.setError(getString(R.string.card_id_validation_error));
            valid = false;
        }

        try {
            String updatedPhoneNumber = phoneNumView.getText().toString();
            if (updatedPhoneNumber.isEmpty()) updatedPhoneNumber = null;
            mMember.setPhoneNumber(updatedPhoneNumber);
        } catch (AbstractModel.ValidationException e) {
            phoneNumView.setError(getString(R.string.phone_number_validation_error));
            valid = false;
        }

        return valid;
    }
}
