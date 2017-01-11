package org.watsi.uhp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.CheckIn;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReceptionActivity extends Activity {

    private Dao<Member, Integer> mMemberDao;
    private Dao<CheckIn, Integer> mCheckInDao;
    private MenuItem mMenuItem;
    private Member mCurrentMember;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: move this key to a secure Keystore
        Rollbar.init(this, "f5fe49118ae44f6397da61934e8f0a2c", "development");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);

        // initial DB code based on this guide: https://blog.jayway.com/2016/03/15/android-ormlite/
        try {
            DatabaseHelper helper = new DatabaseHelper(this);
            mMemberDao = helper.getMemberDao();
            mCheckInDao = helper.getCheckInDao();
            seedDb(helper);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        } catch (IOException e) {
            Rollbar.reportException(e);
        }
    }

    private void seedDb(DatabaseHelper helper) throws SQLException, IOException {
        TableUtils.clearTable(helper.getConnectionSource(), Member.class);

        List<Member> newMembers = new ArrayList<Member>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        for (int i = 0; i < 10; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            member.setBirthdate(new Date(cal.getTimeInMillis()));
            member.setPhotoUrl("https://d3w52z135jkm97.cloudfront.net/uploads/profile/photo/11325/profile_638x479_177b9aad-88b7-49c5-860b-52bf97a0e7d9.jpg");
            newMembers.add(member);
        }
        mMemberDao.create(newMembers);
        Member firstMember = newMembers.get(0);
        firstMember.fetchAndSetPhotoFromUrl(getApplicationContext(), mMemberDao);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String memberId = intent.getDataString();

            if (memberId != null) {
                try {
                    this.mCurrentMember = mMemberDao.queryForId(Integer.parseInt(memberId));
                    fillOutMemberDetails(mCurrentMember);
                    mMenuItem.collapseActionView();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fillOutMemberDetails(Member member) throws SQLException {
        TextView nameView = (TextView) findViewById(R.id.member_name);
        nameView.setText(member.getName());
        TextView birthdateView = (TextView) findViewById(R.id.member_birthdate);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd");
        birthdateView.setText(simpleDate.format(member.getBirthdate()));

        ImageView imageView = (ImageView) findViewById(R.id.member_photo);
        Bitmap photoBitmap = member.getPhotoBitmap();
        if (photoBitmap != null) {
            imageView.setImageBitmap(photoBitmap);
        } else {
            imageView.setImageResource(R.drawable.sample_portrait);
        }

        TextView idView = (TextView) findViewById(R.id.member_id);
        idView.setText(String.valueOf(member.getId()));
        CheckIn lastCheckIn = member.getLastCheckIn(mCheckInDao);
        if (lastCheckIn != null) {
            TextView lastCheckInView = (TextView) findViewById(R.id.member_last_check_in);
            lastCheckInView.setText(simpleDate.format(lastCheckIn.getDate()));
        }

        Button checkInButton = (Button) findViewById(R.id.check_in_button);
        checkInButton.setVisibility(View.VISIBLE);
        checkInButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                checkIn.setMember(mCurrentMember);
                                try {
                                    mCheckInDao.create(checkIn);
                                    mMemberDao.refresh(mCurrentMember);
                                    fillOutMemberDetails(mCurrentMember);
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
    }
}
