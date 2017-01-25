package org.watsi.uhp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        String memberId = getArguments().getString("memberId");
        final String idMethod = getArguments().getString("idMethod");

        try {
            final Member member = MemberDao.findById(memberId);
            final SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

            TextView nameView = (TextView) view.findViewById(R.id.member_name);
            nameView.setText(member.getName());
            TextView ageView = (TextView) view.findViewById(R.id.member_age);
            ageView.setText("Age - " + String.valueOf(member.getAge()));

            ImageView imageView = (ImageView) view.findViewById(R.id.member_photo);
            Bitmap photoBitmap = member.getPhotoBitmap();
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
            } else {
                imageView.setImageResource(R.drawable.portrait_placeholder);
            }

            TextView idView = (TextView) view.findViewById(R.id.member_id);
            idView.setText(String.valueOf(member.getCardId()));

            Encounter lastEncounter = member.getLastEncounter();
            final TextView lastEncounterView = (TextView) view.findViewById(R.id.member_last_encounter);

            if (lastEncounter != null) {
                lastEncounterView.setText(simpleDate.format(lastEncounter.getDate()));
            }

            Button encounterButton = (Button) view.findViewById(R.id.create_encounter);
            encounterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EncounterFragment encounterFragment = new EncounterFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("idMethod", idMethod);
                    encounterFragment.setArguments(bundle);

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, encounterFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
        return view;
    }
}
