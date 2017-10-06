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

import java.util.Calendar;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class })
public class EnrollNewbornViewModelTest {
    private EnrollNewbornViewModel enrollNewbornViewModel;
    private final Member mMember = new Member();

    @Mock
    FormFragment mockFormFragment;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ExceptionManager.class);
        enrollNewbornViewModel = new EnrollNewbornViewModel(mockFormFragment, mMember);
    }

    @Test
    public void getSaveEnabled_invalidCardId() {
        mMember.setFullName("");
        mMember.setCardId("RWI123123");
        mMember.setGender(null);
        mMember.setBirthdate(Calendar.getInstance().getTime());
        enrollNewbornViewModel.updateSaveButton();
        assertFalse(enrollNewbornViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_noFullName() {
        mMember.setFullName("");
        mMember.setCardId("RWI123123");
        mMember.setGender(null);
        mMember.setBirthdate(Calendar.getInstance().getTime());
        enrollNewbornViewModel.updateSaveButton();
        assertFalse(enrollNewbornViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_noSelectedGender() {
        mMember.setFullName("Test Full Name");
        mMember.setCardId("RWI123123");
        mMember.setGender(null);
        mMember.setBirthdate(Calendar.getInstance().getTime());
        enrollNewbornViewModel.updateSaveButton();
        assertFalse(enrollNewbornViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_noCardId() {
        mMember.setFullName("Valid Full Name");
        mMember.setCardId("");
        mMember.setGender(Member.GenderEnum.F);
        mMember.setBirthdate(Calendar.getInstance().getTime());
        enrollNewbornViewModel.updateSaveButton();
        assertFalse(enrollNewbornViewModel.getSaveEnabled());
    }

    @Test
    public void updateSaveButton_valid() {
        mMember.setFullName("Valid Full Name");
        mMember.setCardId("RWI123123");
        mMember.setGender(Member.GenderEnum.F);
        mMember.setBirthdate(Calendar.getInstance().getTime());
        enrollNewbornViewModel.updateSaveButton();
        assertTrue(enrollNewbornViewModel.getSaveEnabled());
    }

    @Test
    public void onClickSave_valid() {
        mMember.setFullName("Test Full Name");
        mMember.setCardId("RWI123123");
        mMember.setGender(Member.GenderEnum.M);
        mMember.setBirthdate(Calendar.getInstance().getTime());

        enrollNewbornViewModel.updateSaveButton();
        assertTrue(enrollNewbornViewModel.getSaveEnabled());
        enrollNewbornViewModel.onClickSave();

        verify(mockFormFragment, times(1)).nextStep();
    }
}
