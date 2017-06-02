package org.watsi.uhp.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by michaelliang on 6/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class DetailPresenterTest {
    private DetailPresenter detailPresenter;

    @Mock
    View view;

    @Mock
    TextView textView;

    @Mock
    ImageView imageView;

    @Before
    public void setup() {
        detailPresenter = new DetailPresenter(view);
    }

    @Test
    public void getRejectIdentityLink() throws Exception {
        when(view.findViewById(R.id.reject_identity)).thenReturn(textView);
        assertEquals(detailPresenter.getRejectIdentityLink(), textView);
    }

    @Test
    public void getMemberPhotoView() throws Exception {
        when(view.findViewById(R.id.member_photo)).thenReturn(imageView);
        assertEquals(detailPresenter.getMemberPhotoView(), imageView);
    }
}
