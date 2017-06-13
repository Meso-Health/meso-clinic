package org.watsi.uhp.presenters;

import android.view.View;
import android.widget.TextView;

import org.watsi.uhp.R;

/**
 * Created by michaelliang on 6/1/17.
 */

public class MemberDetailPresenter {
    private View mView;

    public MemberDetailPresenter(View view) {
        mView = view;
    }

    public TextView getMemberActionLink() {
        return (TextView) mView.findViewById(R.id.member_action_link);
    }
}
