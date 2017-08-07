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
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by michaelliang on 8/7/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class })
public class MemberEditViewModelTest {
    private MemberEditViewModel memberEditViewModel;
    private final Member mMember = new Member();

    @Mock
    FormFragment mockFormFragment;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ExceptionManager.class);
        memberEditViewModel = new MemberEditViewModel(mockFormFragment, mMember);
    }

    @Test
    public void updateSaveButton_enabled() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("123123123");
        mMember.setCardId("RWI123123");

        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_noPhoneNumber() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("");
        mMember.setCardId("RWI123123");

        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_invalidPhoneNumber() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("123dfdf");
        mMember.setCardId("RWI123123");

        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_noFullName() {
        mMember.setFullName("");
        mMember.setPhoneNumber("123123123");
        mMember.setCardId("RWI123123");

        memberEditViewModel.updateSaveButton();
        assertFalse(memberEditViewModel.getSaveEnabled());
    }

    @Test
    public void onClickSave_noPhoneNumber() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("");
        mMember.setCardId("RWI123123");
        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
        memberEditViewModel.onClickSave();
        assertNull(memberEditViewModel.getPhoneNumberError());
    }

    @Test
    public void onClickSave_invalidPhoneNumber() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("invalid-phone-number");
        mMember.setCardId("RWI123123");
        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
        memberEditViewModel.onClickSave();
        assertEquals(memberEditViewModel.getPhoneNumberError(), MemberViewModel.PHONE_NUMBER_VALIDATION_ERROR);
    }

    @Test
    public void onClickSave_valid() {
        mMember.setFullName("Test Full Name");
        mMember.setPhoneNumber("123456789");
        mMember.setCardId("RWI123123");
        memberEditViewModel.updateSaveButton();
        assertTrue(memberEditViewModel.getSaveEnabled());
        memberEditViewModel.onClickSave();

        verify(mockFormFragment, times(1)).nextStep();
    }
}
