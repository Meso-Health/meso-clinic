package org.watsi.uhp.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;

public class VersionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.version_label);
        View view = inflater.inflate(R.layout.fragment_version, container, false);

        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            TextView versionTextView = (TextView) view.findViewById(R.id.version_number);
            versionTextView.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Rollbar.reportException(e);
        }

        return view;
    }
}
