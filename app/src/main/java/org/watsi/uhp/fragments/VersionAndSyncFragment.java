package org.watsi.uhp.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VersionAndSyncFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.version_and_sync_label);

        View view = inflater.inflate(R.layout.fragment_version_and_sync, container, false);

        TextView versionNumber = (TextView) view.findViewById(R.id.version_number);
        TextView fetchMembersTimestamp = (TextView) view.findViewById(R.id.fetch_members_timestamp);
        TextView fetchBillablesTimestamp = (TextView) view.findViewById(R.id.fetch_billables_timestamp);
        TextView fetchMemberPicturesQuantity = (TextView) view.findViewById(R.id.fetch_member_pictures_quantity);
        TextView syncEditedMembersQuantity = (TextView) view.findViewById(R.id.sync_edited_members_quantity);
        TextView syncNewMembersQuantity = (TextView) view.findViewById(R.id.sync_new_members_quantity);
        TextView syncIdEventsQuantity = (TextView) view.findViewById(R.id.sync_id_events_quantity);
        TextView syncEncountersQuantity = (TextView) view.findViewById(R.id.sync_encounters_quantity);
        FloatingActionButton refreshButton = (FloatingActionButton) view.findViewById(R.id.refresh_button);

        setVersionNumber(versionNumber);
        setFetchMembersTimestamp(fetchMembersTimestamp);
        setFetchBillablesTimestamp(fetchBillablesTimestamp);
        setRefreshButton(refreshButton);

        try {
            setFetchMemberPicturesQuantity(fetchMemberPicturesQuantity);
            setSyncEditedMembersQuantity(syncEditedMembersQuantity);
            setSyncNewMembersQuantity(syncNewMembersQuantity);
            setSyncIdEventsQuantity(syncIdEventsQuantity);
            setSyncEncountersQuantity(syncEncountersQuantity);
        } catch (SQLException | IllegalStateException e) {
            Rollbar.reportException(e);
        }

        return view;
    }

    private void setVersionNumber(TextView textView) {
        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            textView.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Rollbar.reportException(e);
        }
    }

    private void setFetchMembersTimestamp(TextView textView) {
        Long lastFetchedTimestamp = ConfigManager.getMembersLastFetched(getActivity().getApplicationContext());
        Date date = new Date(lastFetchedTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy   HH:mm:ss z");

        textView.setText(sdf.format(date));
    }

    private void setFetchBillablesTimestamp(TextView textView) {
        Long lastFetchedTimestamp = ConfigManager.getBillablesLastFetched(getActivity().getApplicationContext());
        Date date = new Date(lastFetchedTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy   HH:mm:ss z");

        textView.setText(sdf.format(date));
    }

    private void setFetchMemberPicturesQuantity(TextView textView) throws SQLException {
        List<Member> membersWithPhotosToFetch = MemberDao.membersWithPhotosToFetch();
        int photosToFetch = membersWithPhotosToFetch.size();
        textView.setText(Integer.toString(photosToFetch));
    }

    private void setSyncEditedMembersQuantity(TextView textView) throws SQLException {
        List<Member> unsyncedMembers = MemberDao.getUnsyncedEditedMembers();
        int numberOfUnsynced = unsyncedMembers.size();
        textView.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncNewMembersQuantity(TextView textView) throws SQLException {
        List<Member> unsyncedMembers = MemberDao.getUnsyncedNewMembers();
        int numberOfUnsynced = unsyncedMembers.size();
        textView.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncIdEventsQuantity(TextView textView) throws SQLException {
        List<IdentificationEvent> unsyncedIds = IdentificationEventDao.unsynced();
        int numberOfUnsynced = unsyncedIds.size();
        textView.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncEncountersQuantity(TextView textView) throws SQLException {
        List<Encounter> unsyncedEncounters = EncounterDao.unsynced();
        int numberOfUnsynced = unsyncedEncounters.size();
        textView.setText(Integer.toString(numberOfUnsynced));
    }

    private void setRefreshButton(FloatingActionButton fab) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setVersionFragment();
                Toast.makeText(getContext(), "Page Refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
