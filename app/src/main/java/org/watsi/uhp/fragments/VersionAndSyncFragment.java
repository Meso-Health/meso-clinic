package org.watsi.uhp.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VersionAndSyncFragment extends Fragment {

    private SimpleDateFormat mLastModifiedFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    private SimpleDateFormat mDisplayDateFormat = new SimpleDateFormat("hh:mm:ss a  yyyy/M/d");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.version_and_sync_label);

        View view = inflater.inflate(R.layout.fragment_version_and_sync, container, false);
        Context context = getContext();

        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) view.findViewById(R.id.version_number)).setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Rollbar.reportException(e);
        }

        ((TextView) view.findViewById(R.id.fetch_members_timestamp))
                .setText(formatTimestamp(ConfigManager.getMemberLastModified(context)));

        ((TextView) view.findViewById(R.id.fetch_billables_timestamp))
                .setText(formatTimestamp(ConfigManager.getBillablesLastModified(context)));

        view.findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setVersionFragment();
                Toast.makeText(getContext(), "Page Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            ((TextView) view.findViewById(R.id.fetch_member_pictures_quantity))
                    .setText(formattedQuantity(MemberDao.membersWithPhotosToFetch().size(), "downloading"));

            List<Member> unsyncedMembers = MemberDao.unsynced();
            int newMembersCount = 0;
            int editedMembersCount = 0;
            for (Member member : unsyncedMembers) {
                if (member.isNew()) {
                    newMembersCount++;
                } else {
                    editedMembersCount++;
                }
            }

            ((TextView) view.findViewById(R.id.sync_edited_members_quantity))
                    .setText(formattedQuantity(editedMembersCount, "uploading"));
            ((TextView) view.findViewById(R.id.sync_new_members_quantity))
                    .setText(formattedQuantity(newMembersCount, "uploading"));
            ((TextView) view.findViewById(R.id.sync_id_events_quantity))
                    .setText(formattedQuantity(IdentificationEventDao.unsynced().size(), "uploading"));
            ((TextView) view.findViewById(R.id.sync_encounters_quantity))
                    .setText(formattedQuantity(EncounterDao.unsynced().size(), "uploading"));
        } catch (SQLException | IllegalStateException e) {
            Rollbar.reportException(e);
        }

        return view;
    }

    private String formattedQuantity(int count, String verb) {
        if (count == 0) {
            return getString(R.string.all_synced);
        } else {
            return count + " " + verb;
        }
    }

    private String formatTimestamp(String lastModifiedTimestamp) {
        try {
            Date lastModified = mLastModifiedFormat.parse(lastModifiedTimestamp);
            return mDisplayDateFormat.format(lastModified);
        } catch (ParseException e) {
            return "Could not parse timestamp";
        }
    }
}
