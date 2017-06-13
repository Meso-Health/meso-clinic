package org.watsi.uhp.presenters;

import android.content.Context;
import android.view.View;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.watsi.uhp.models.Member;

/**
 * Created by michaelliang on 6/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class MemberDetailPresenterTest {
    private MemberDetailPresenter memberDetailPresenter;

    @Mock
    View view;

    @Mock
    Context context;

    @Mock
    Member member;

    @Before
    public void setup() {
        memberDetailPresenter = new MemberDetailPresenter(view, context, member);
    }
}
