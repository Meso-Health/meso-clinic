package org.watsi.uhp.models

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(Photo::class, Uri::class)
class PhotoTest {

    val mockContext = mock(Context::class.java)
    val mockContentResolver = mock(ContentResolver::class.java)
    val mockUri = mock(Uri::class.java)
    val mockCursor = mock(Cursor::class.java)

    val photo: Photo = spy(Photo::class.java)
    val url = "http://watsi.org/photo/1"

    @Before
    fun setup() {
        photo.url = url
        PowerMockito.mockStatic(Uri::class.java)
        `when`(mockContext.contentResolver).thenReturn(mockContentResolver)
        `when`(Uri.parse(url)).thenReturn(mockUri)
    }

    @Test
    fun delete_synced_deleteFails_returnsFalse() {
        `when`(mockContentResolver.delete(mockUri, null, null)).thenReturn(0)

        assertFalse(photo.delete(mockContext))
        assertFalse(photo.deleted)
    }

    @Test
    fun delete_synced_deleteSucceeds_returnsTrue() {
        `when`(mockContentResolver.delete(mockUri, null, null)).thenReturn(1)
        `when`(mockContentResolver.query(
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
                .thenReturn(mockCursor)

        assertTrue(photo.delete(mockContext))
        assertTrue(photo.deleted)
    }
}
