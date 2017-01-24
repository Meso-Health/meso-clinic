package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Member;

public class RecentEncountersFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_encounters, container, false);
        ListView listView = (ListView) view.findViewById(R.id.recent_members);
        ListAdapter adapter = new MemberAdapter(getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getItemAtPosition(position);
                MainActivity activity = (MainActivity) getActivity();
                activity.setDetailFragment(String.valueOf(member.getId()), Encounter.IdMethodEnum.SEARCH);
            }
        });

        return view;
    }
}
