package org.watsi.uhp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Member;

public class MemberDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);

        String memberId = (String) getIntent().getSerializableExtra("memberId");
        View contentView = findViewById(android.R.id.content);

        TextView memberDetailNameView = (TextView) contentView.findViewById(R.id.member_detail_name_view);
        memberDetailNameView.setText(memberId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_detail_menu, menu);
        return true;
    }
}
