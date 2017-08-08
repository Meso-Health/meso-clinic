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
import static org.watsi.uhp.view_models.MemberViewModel.NAME_VALIDATION_ERROR;
import static org.watsi.uhp.view_models.MemberViewModel.PHONE_NUMBER_VALIDATION_ERROR;

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
        assertEquals(memberViewModel.getFullNameError(), NAME_VALIDATION_ERROR);
    }

    @Test
    public void getPhoneNumber_invalidNumber() throws Exception {
        memberViewModel.setPhoneNumber("123");
        assertEquals(memberViewModel.getPhoneNumber(), "123");
        // This one requires forcing validation. merely setting the field would not trigger validation by design.
        memberViewModel.validatePhoneNumber();
        assertEquals(memberViewModel.getPhoneNumberError(), PHONE_NUMBER_VALIDATION_ERROR);
    }

    @Test
    public void getPhoneNumber_validNumber() throws Exception {
        memberViewModel.setPhoneNumber("123123123");
        assertEquals(memberViewModel.getPhoneNumber(), "123123123");
        assertNull(memberViewModel.getPhoneNumberError());
    }

    @Test
    public void getPhoneNumber_emptyNumber() throws Exception {
        memberViewModel.setPhoneNumber("");
        assertEquals(memberViewModel.getPhoneNumber(), null);
        assertNull(memberViewModel.getPhoneNumberError());
    }

    @Test
    public void getCardId_validNoSpaces() throws Exception {
        mMember.setCardId("RWI123123");
        assertEquals(memberViewModel.getCardId(), "RWI123123");
        assertNull(memberViewModel.getCardIdError());
    }

    @Test
    public void getCardId_validWithSpaces() throws Exception {
        mMember.setCardId("RWI 123 123");
        assertEquals(memberViewModel.getCardId(), "RWI123123");
        assertNull(memberViewModel.getCardIdError());
    }
}
