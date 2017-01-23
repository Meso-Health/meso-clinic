package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.CheckInDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.CheckIn;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        String memberId = getArguments().getString("memberId");

        try {
            final Member member = MemberDao.findById(memberId);
            final SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd", Locale.US);

            TextView nameView = (TextView) view.findViewById(R.id.member_name);
            nameView.setText(member.getName());
            TextView ageView = (TextView) view.findViewById(R.id.member_age);
            ageView.setText(String.valueOf(member.getAge()));

            ImageView imageView = (ImageView) view.findViewById(R.id.member_photo);
            Bitmap photoBitmap = member.getPhotoBitmap();
            if (photoBitmap != null) {
                imageView.setImageBitmap(photoBitmap);
            } else {
                imageView.setImageResource(R.drawable.portrait_placeholder);
            }

            TextView idView = (TextView) view.findViewById(R.id.member_id);
            idView.setText(String.valueOf(member.getCardId()));

            CheckIn lastCheckIn = member.getLastCheckIn();
            final TextView lastCheckInView = (TextView) view.findViewById(R.id.member_last_check_in);

            if (lastCheckIn != null) {
                lastCheckInView.setText(simpleDate.format(lastCheckIn.getDate()));
            }

            Button checkInButton = (Button) view.findViewById(R.id.check_in_button);
            checkInButton.setVisibility(View.VISIBLE);
            checkInButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    // TODO: don't need to create these arrays everytime
                    String[] options = new String[]{
                            "Admitted as inpatient",
                            "Admitted as outpatient",
                            "Turned away"
                    };
                    final CheckIn.OutcomeEnum[] outcomes = new CheckIn.OutcomeEnum[]{
                            CheckIn.OutcomeEnum.ADMITTED_INPATIENT,
                            CheckIn.OutcomeEnum.ADMITTED_OUTPATIENT,
                            CheckIn.OutcomeEnum.TURNED_AWAY
                    };

                    builder.setTitle(R.string.check_in_cta)
                            .setCancelable(false)
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CheckIn checkIn = new CheckIn();
                                    checkIn.setDate(Calendar.getInstance().getTime());
                                    checkIn.setOutcome(outcomes[which]);
                                    checkIn.setMember(member);
                                    try {
                                        CheckInDao.create(checkIn);
                                        // TODO: this refresh is dangerous if member list is being refreshed at same time
                                        MemberDao.refresh(member);
                                        CheckIn lastCheckIn = member.getLastCheckIn();
                                        lastCheckInView.setText(simpleDate.format(lastCheckIn.getDate()));
                                    } catch (SQLException e) {
                                        Rollbar.reportException(e);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.create();
                    builder.show();
                }
            });

        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
        return view;
    }
}
