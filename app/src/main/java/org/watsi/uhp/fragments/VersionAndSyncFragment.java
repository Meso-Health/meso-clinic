package org.watsi.uhp.fragments;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.watsi.domain.entities.Delta;
import org.watsi.domain.repositories.DeltaRepository;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.PreferencesManager;

import java.util.List;

import javax.inject.Inject;

public class VersionAndSyncFragment extends BaseFragment {

    @Inject MemberRepository memberRepository;
    @Inject DeltaRepository deltaRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.version_and_sync_label);

        View view = inflater.inflate(R.layout.fragment_version_and_sync, container, false);

        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) view.findViewById(R.id.version_number)).setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            ExceptionManager.reportException(e);
        }

        final View finalView = view;
        view.findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshValues(finalView);
            }
        });

        refreshValues(view);
        return view;
    }

    private void updateTimestamps(View view) {
        PreferencesManager preferencesManager = new PreferencesManager(getContext());
        ((TextView) view.findViewById(R.id.fetch_members_timestamp))
                .setText(preferencesManager.getMemberLastModified());
        ((TextView) view.findViewById(R.id.fetch_billables_timestamp))
                .setText(preferencesManager.getBillablesLastModified());
        ((TextView) view.findViewById(R.id.fetch_diagnoses_timestamp))
                .setText(preferencesManager.getDiagnosesLastModified());
    }

    private void refreshValues(View view) {
        updateTimestamps(view);

        final ProgressDialog spinner = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        spinner.setCancelable(true);
        spinner.setMessage("Loading...");
        spinner.show();

        new AsyncTask<String, Void, int[]>() {
            @Override
            protected int[] doInBackground(String... params) {
                int[] counts = new int[6];
                counts[0] = memberRepository.membersWithPhotosToFetch().size();
                List<Delta> unsyncedMembers = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet();
                int newMembersCount = 0;
                int editedMembersCount = 0;
                for (Delta delta : unsyncedMembers) {
                    if (delta.getAction().equals(Delta.Action.ADD)) {
                        newMembersCount++;
                    } else {
                        editedMembersCount++;
                    }
                }
                counts[1] = newMembersCount;
                counts[2] = editedMembersCount;
                counts[3] = deltaRepository.unsynced(Delta.ModelName.IDENTIFICATION_EVENT)
                        .blockingGet().size();
                counts[4] = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER)
                        .blockingGet().size();
                counts[5] = deltaRepository.unsynced(Delta.ModelName.ENCOUNTER_FORM)
                        .blockingGet().size();
                return counts;
            }

            @Override
            protected void onPostExecute(int[] result) {
                View view = getView();

                if (view != null) {
                    ((TextView) view.findViewById(R.id.fetch_member_pictures_quantity))
                            .setText(formattedQuantity(result[0]));
                    ((TextView) view.findViewById(R.id.sync_new_members_quantity))
                            .setText(formattedQuantity(result[1]));
                    ((TextView) view.findViewById(R.id.sync_edited_members_quantity))
                            .setText(formattedQuantity(result[2]));
                    ((TextView) view.findViewById(R.id.sync_id_events_quantity))
                            .setText(formattedQuantity(result[3]));
                    ((TextView) view.findViewById(R.id.sync_encounters_quantity))
                            .setText(formattedQuantity(result[4]));
                    ((TextView) view.findViewById(R.id.sync_encounter_forms_quantity))
                            .setText(formattedQuantity(result[5]));
                }

                spinner.dismiss();
            }
        }.execute();
    }

    private String formattedQuantity(int count) {
        if (count == 0) {
            return getString(R.string.all_synced);
        } else {
            return count + " pending";
        }
    }
}
