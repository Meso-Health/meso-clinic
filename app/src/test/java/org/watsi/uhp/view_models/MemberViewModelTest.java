package org.watsi.uhp.view_models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Member;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by michaelliang on 8/2/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class })
public class MemberViewModelTest {
    private MemberViewModel memberViewModel;
    private final Member mMember = new Member();

    @Mock
    FormFragment mockFormFragment;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ExceptionManager.class);
        memberViewModel = new MemberViewModel(mockFormFragment, mMember) {

            @Override
            public void setUpViewModel() {
                // no-op
            }

            @Override
            public void updateSaveButton() {
                // no-op
            }

            @Override
            public void onClickSave() {
                // no-op
            }
        };
    }

    @Test
    public void init() throws Exception {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("123123123");
        mMember.setCardId("RWI123123");

        assertEquals(memberViewModel.getFullName(), "Test Full Name");
        assertEquals(memberViewModel.getPhoneNumber(), "123123123");
        assertEquals(memberViewModel.getCardId(), "RWI123123");


        assertNull(memberViewModel.getFullNameError());
        assertNull(memberViewModel.getPhoneNumberError());
        assertNull(memberViewModel.getCardIdError());

        assertFalse(memberViewModel.getSaveEnabled());
    }

    @Test
    public void setFullName_validName() throws Exception {
        memberViewModel.setFullName("Test Full Name");
        assertEquals(memberViewModel.getFullName(), "Test Full Name");
        assertNull(memberViewModel.getFullNameError());
    }

    @Test
    public void getFullNameError_nullName() throws Exception {
        memberViewModel.setFullName(null);
        assertNull(memberViewModel.getFullName());
        assertEquals(memberViewModel.getFullNameError(), "Name cannot be blank.");
    }

    @Test
    public void getPhoneNumberError_validNumber() throws Exception {

    }


}
