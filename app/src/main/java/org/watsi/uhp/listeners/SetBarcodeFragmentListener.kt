package org.watsi.uhp.listeners

import android.view.View
import org.watsi.uhp.fragments.BarcodeFragment
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.models.IdentificationEvent
import org.watsi.uhp.models.Member

class SetBarcodeFragmentListener(
        private val mNavigationManager: NavigationManager,
        private val mScanPurpose: BarcodeFragment.ScanPurposeEnum,
        private val mMember: Member?,
        private val mIdEvent: IdentificationEvent?) : View.OnClickListener {

    override fun onClick(v: View) {
        mNavigationManager.setBarcodeFragment(mScanPurpose, mMember, mIdEvent)
    }
}
