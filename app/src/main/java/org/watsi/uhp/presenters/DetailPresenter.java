package org.watsi.uhp.presenters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.models.IdentificationEvent;

/**
 * Created by michaelliang on 6/1/17.
 */

public class DetailPresenter {
    private View mView;

    public DetailPresenter(View view) {
        mView = view;
    }

    public TextView getRejectIdentityLink() {
        return (TextView) mView.findViewById(R.id.reject_identity);
    }

    public ImageView getMemberPhotoView() {
        return (ImageView) mView.findViewById(R.id.member_photo);
    }
}
