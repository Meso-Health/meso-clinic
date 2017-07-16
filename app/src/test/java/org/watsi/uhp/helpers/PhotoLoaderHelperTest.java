package org.watsi.uhp.helpers;

import android.content.Context;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileManager.class, PhotoLoaderHelper.class })
public class PhotoLoaderHelperTest {
    @Mock
    Context mockContext;

    @Mock
    Member mockMember;

    @Mock
    ImageView mockImageView;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(FileManager.class);
        mockStatic(PhotoLoaderHelper.class);
    }

    @Test
    public void loadMemberPhoto_nonLocalPhoto() throws Exception {
        byte[] photoBytes = new byte[]{(byte)0xe0};
        when(mockMember.getPhotoUrl()).thenReturn(null);
        when(mockMember.getPhoto()).thenReturn(photoBytes);
        when(PhotoLoaderHelper.getWidthFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_width)).thenReturn(90);
        when(PhotoLoaderHelper.getHeightFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_height)).thenReturn(120);

        doCallRealMethod().when(PhotoLoaderHelper.class, "loadMemberPhoto", mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        PhotoLoaderHelper.loadMemberPhoto(mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        verifyStatic();
        PhotoLoaderHelper.loadSmallPhotoWithGlide(mockContext, mockImageView, photoBytes, 90, 120);
    }

    @Test
    public void loadMemberPhoto_localFullSizePhoto() throws Exception {
        when(mockMember.getPhotoUrl()).thenReturn("random-local-full-size-image-url");
        when(FileManager.isLocal("random-local-full-size-image-url")).thenReturn(true);
        when(PhotoLoaderHelper.getWidthFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_width)).thenReturn(90);
        when(PhotoLoaderHelper.getHeightFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_height)).thenReturn(120);

        doCallRealMethod().when(PhotoLoaderHelper.class, "loadMemberPhoto", mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        PhotoLoaderHelper.loadMemberPhoto(mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        verifyStatic();
        PhotoLoaderHelper.loadFullSizeImageWithGlide(mockContext, mockImageView, "random-local-full-size-image-url", 90, 120);
    }
}
