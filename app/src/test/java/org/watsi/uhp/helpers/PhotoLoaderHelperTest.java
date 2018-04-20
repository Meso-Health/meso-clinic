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
import org.watsi.domain.entities.Member;
import org.watsi.domain.entities.Photo;
import org.watsi.uhp.R;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PhotoLoaderHelper.class })
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
        mockStatic(PhotoLoaderHelper.class);
    }

    @Test
    public void loadMemberPhoto_nonLocalPhoto() throws Exception {
        byte[] photoBytes = new byte[]{(byte)0xe0};
        when(mockMember.getCroppedPhotoBytes()).thenReturn(photoBytes);
        when(PhotoLoaderHelper.getWidthFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_width)).thenReturn(90);
        when(PhotoLoaderHelper.getHeightFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_height)).thenReturn(120);

        doCallRealMethod().when(PhotoLoaderHelper.class, "loadMemberPhoto", mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        PhotoLoaderHelper.loadMemberPhoto(mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        verifyStatic();
        PhotoLoaderHelper.loadPhotoFromBytes(mockContext, mockImageView, photoBytes, 90, 120);
    }

    @Test
    public void loadMemberPhoto_localFullSizePhoto() throws Exception {
        Photo photo = new Photo();
        photo.setUrl("random-local-full-size-image-url");
        when(mockMember.getLocalMemberPhoto()).thenReturn(photo);
        when(mockMember.getCroppedPhotoBytes()).thenReturn(null);
        when(PhotoLoaderHelper.getWidthFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_width)).thenReturn(90);
        when(PhotoLoaderHelper.getHeightFromDimensionResource(mockContext, R.dimen.detail_fragment_photo_height)).thenReturn(120);

        doCallRealMethod().when(PhotoLoaderHelper.class, "loadMemberPhoto", mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        PhotoLoaderHelper.loadMemberPhoto(mockContext, mockMember, mockImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);

        verifyStatic();
        PhotoLoaderHelper.loadPhotoFromContentUri(mockContext, mockImageView, "random-local-full-size-image-url", 90, 120);
    }
}
