package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity

class MemberNotFoundFragment : DaggerFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.household_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        return inflater?.inflate(R.layout.fragment_member_not_found, container, false)
    }
}
