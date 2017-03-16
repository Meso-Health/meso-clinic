package org.watsi.uhp.fragments;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import junit.runner.Version;

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
import java.util.Locale;
import java.util.TimeZone;

public class VersionAndSyncFragment extends Fragment {

    private TextView versionNumber;
    private TextView fetchMembersTimestamp;
    private TextView fetchBillablesTimestamp;
    private TextView fetchMemberPicturesQuantity;
    private TextView syncEditedMembersQuantity;
    private TextView syncNewMembersQuantity;
    private TextView syncIdEventsQuantity;
    private TextView syncEncountersQuantity;
    private Button refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.version_and_sync_label);

        View view = inflater.inflate(R.layout.fragment_version_and_sync, container, false);

        versionNumber = (TextView) view.findViewById(R.id.version_number);
        fetchMembersTimestamp = (TextView) view.findViewById(R.id.fetch_members_timestamp);
        fetchBillablesTimestamp = (TextView) view.findViewById(R.id.fetch_billables_timestamp);
        fetchMemberPicturesQuantity = (TextView) view.findViewById(R.id.fetch_member_pictures_quantity);
        syncEditedMembersQuantity = (TextView) view.findViewById(R.id.sync_edited_members_quantity);
        syncNewMembersQuantity = (TextView) view.findViewById(R.id.sync_new_members_quantity);
        syncIdEventsQuantity = (TextView) view.findViewById(R.id.sync_id_events_quantity);
        syncEncountersQuantity = (TextView) view.findViewById(R.id.sync_encounters_quantity);
        refreshButton = (Button) view.findViewById(R.id.refresh_button);

        setVersionNumber();
        setFetchMembersTimestamp();
        setFetchBillablesTimestamp();
        setRefreshButton();

        try {
            setFetchMemberPicturesQuantity();
            setSyncEditedMembersQuantity();
            setSyncNewMembersQuantity();
            setSyncIdEventsQuantity();
            setSyncEncountersQuantity();
        } catch (SQLException | IllegalStateException e) {
            Rollbar.reportException(e);
        }

        return view;
    }

    private void setVersionNumber() {
        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            versionNumber.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Rollbar.reportException(e);
        }
    }

    private void setFetchMembersTimestamp() {
        Long lastFetchedTimestamp = ConfigManager.getMembersLastFetched(getActivity().getApplicationContext());
        Date date = new Date(lastFetchedTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("EAT"));

        fetchMembersTimestamp.setText(sdf.format(date));
    }

    private void setFetchBillablesTimestamp() {
        Long lastFetchedTimestamp = ConfigManager.getBillablesLastFetched(getActivity().getApplicationContext());
        Date date = new Date(lastFetchedTimestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("EAT"));

        fetchBillablesTimestamp.setText(sdf.format(date));
    }

    private void setFetchMemberPicturesQuantity() throws SQLException {
        List<Member> membersWithPhotosToFetch = MemberDao.membersWithPhotosToFetch();
        int photosToFetch = membersWithPhotosToFetch.size();
        fetchMemberPicturesQuantity.setText(Integer.toString(photosToFetch));
    }

    private void setSyncEditedMembersQuantity() throws SQLException {
        List<Member> unsyncedMembers = MemberDao.getUnsyncedEditedMembers();
        int numberOfUnsynced = unsyncedMembers.size();
        syncEditedMembersQuantity.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncNewMembersQuantity() throws SQLException {
        List<Member> unsyncedMembers = MemberDao.getUnsyncedNewMembers();
        int numberOfUnsynced = unsyncedMembers.size();
        syncNewMembersQuantity.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncIdEventsQuantity() throws SQLException {
        List<IdentificationEvent> unsyncedIds = IdentificationEventDao.unsynced();
        int numberOfUnsynced = unsyncedIds.size();
        syncIdEventsQuantity.setText(Integer.toString(numberOfUnsynced));
    }

    private void setSyncEncountersQuantity() throws SQLException {
        List<Encounter> unsyncedEncounters = EncounterDao.unsynced();
        int numberOfUnsynced = unsyncedEncounters.size();
        syncEncountersQuantity.setText(Integer.toString(numberOfUnsynced));
    }

    private void setRefreshButton() {
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setVersionFragment();
                Toast.makeText(getContext(), "Page Refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
