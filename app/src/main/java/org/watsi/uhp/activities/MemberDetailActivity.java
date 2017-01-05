package org.watsi.uhp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class MemberDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);

        int memberId = (int) getIntent().getSerializableExtra("memberId");
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        try {
            Dao<Member, Integer> memberDao = dbHelper.getMemberDao();
            Member member = memberDao.queryForId(memberId);

            View contentView = findViewById(android.R.id.content);

            TextView memberDetailNameView = (TextView) contentView.findViewById(R.id.member_detail_name_view);
            memberDetailNameView.setText(member.getName());

            TextView memberDetailIdView = (TextView) contentView.findViewById(R.id.member_detail_id_view);
            memberDetailIdView.setText(String.valueOf(memberId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_detail_menu, menu);
        return true;
    }
}
