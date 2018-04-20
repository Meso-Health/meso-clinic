package org.watsi.uhp.listeners

import android.view.View
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.uhp.fragments.BarcodeFragment
import org.watsi.uhp.managers.LegacyNavigationManager

class SetBarcodeFragmentListenerTest {

    val mockView = Mockito.mock(View::class.java)
    val mockNavigationManager = Mockito.mock(LegacyNavigationManager::class.java)
    val scanPurpose = BarcodeFragment.ScanPurposeEnum.ID
    val mockMember = Mockito.mock(Member::class.java)
    val mockIdEvent = Mockito.mock(IdentificationEvent::class.java)

    @Test
    fun onClick() {
        val listener = SetBarcodeFragmentListener(
                mockNavigationManager, scanPurpose, mockMember, mockIdEvent)

        listener.onClick(mockView)

        Mockito.verify(mockNavigationManager, times(1)).setBarcodeFragment(
                scanPurpose, mockMember, mockIdEvent)
    }
}
