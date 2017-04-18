package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class CurrentPatientsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.current_patients_fragment_label);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        View view = inflater.inflate(R.layout.fragment_current_patients, container, false);
        Button mNewPatientButton = (Button) view.findViewById(R.id.identification_button);
        TextView currentPatientsLabel = (TextView) view.findViewById(R.id.current_patients_label);
        ListView listView = (ListView) view.findViewById(R.id.current_patients);
        listView.setEmptyView(view.findViewById(R.id.current_patients_empty_text));

        mNewPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setBarcodeFragment(
                        BarcodeFragment.ScanPurposeEnum.ID, null, null);
                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setDisplayHomeAsUpEnabled(true);
            }
        });

        try {
            List<Member> currentPatients = MemberDao.getCheckedInMembers();
            ListAdapter adapter = new MemberAdapter(getContext(), currentPatients, true);
            int currentPatientsCount = currentPatients.size();

            if (currentPatientsCount == 0) {
                currentPatientsLabel.setVisibility(View.GONE);
            } else {
                currentPatientsLabel.setText(getActivity().getResources().getQuantityString(
                        R.plurals.current_patients_label, currentPatientsCount, currentPatientsCount));
            }
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    MainActivity activity = (MainActivity) getActivity();
                    new NavigationManager(activity).setDetailFragment(member, null, null);

                    ((AppCompatActivity) getActivity()).getSupportActionBar()
                            .setDisplayHomeAsUpEnabled(true);
                }
            });
        } catch (SQLException e) {
            ExceptionManager.handleException(e);
            Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }

        return view;
    }
}
