package org.watsi.uhp.services

import android.util.Log
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.watsi.uhp.models.Photo

import junit.framework.Assert.assertTrue
import org.junit.runner.RunWith
import org.mockito.Mockito.spy
import org.mockito.Mockito.doReturn
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.watsi.uhp.database.PhotoDao

@RunWith(PowerMockRunner::class)
@PrepareForTest(DeleteFetchedPhotoService::class, Log::class, PhotoDao::class)
class DeleteFetchedPhotoServiceTest {
    val mockPhoto = spy(Photo::class.java)
    val photoList = listOf(mockPhoto)
    val service = DeleteFetchedPhotoService()

    @Before
    fun setup() {
        PowerMockito.mockStatic(Log::class.java)
        PowerMockito.mockStatic(DeleteFetchedPhotoService::class.java)
        PowerMockito.mockStatic(PhotoDao::class.java)
    }

    @Test
    fun performSync() {
        doReturn(true).`when`(mockPhoto).delete(service)
        PowerMockito.doReturn(photoList).`when`(PhotoDao::class.java)
        PhotoDao.canBeDeleted()

        val result = service.performSync()

        Mockito.verify(mockPhoto).delete(service)
        assertTrue(result)
    }
}
