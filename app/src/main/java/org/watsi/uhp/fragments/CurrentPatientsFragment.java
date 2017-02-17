package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.rollbar.android.Rollbar;
import com.rollbar.android.RollbarExceptionHandler;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class CurrentPatientsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.current_patients_fragment_label);

        View view = inflater.inflate(R.layout.fragment_current_patients, container, false);
        ListView listView = (ListView) view.findViewById(R.id.current_patients);

        //TODO: change to show only members who have been identified but not had an encounter
        try {
            List<Member> currentPatients = MemberDao.recentMembers();
            ListAdapter adapter = new MemberAdapter(getContext(), currentPatients);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    MainActivity activity = (MainActivity) getActivity();
                    activity.setEncounterFragment(String.valueOf(member.getId()));
                }
            });
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        return view;
    }
}
