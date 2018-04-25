package org.watsi.uhp.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_barcode.barcode_preview_surface
import kotlinx.android.synthetic.main.fragment_barcode.search_member

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.managers.QrCodeDetectorManager

import javax.inject.Inject

class BarcodeFragment : DaggerFragment(), SurfaceHolder.Callback {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository

    lateinit var qrCodeDetectorManager: QrCodeDetectorManager
    lateinit var scanPurpose: ScanPurpose
    var member: Member? = null

    companion object {
        const val PARAM_SCAN_PURPOSE = "scan_purpose"
        const val PARAM_MEMBER = "member"

        fun forPurpose(purpose: ScanPurpose, member: Member? = null): BarcodeFragment {
            val fragment = BarcodeFragment()
            fragment.arguments = Bundle().apply {
                putString(PARAM_SCAN_PURPOSE, purpose.name)
                putSerializable(PARAM_MEMBER, member)
            }
            return fragment
        }
    }

    enum class ScanPurpose {
        ID, MEMBER_EDIT, NEWBORN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scanPurpose = ScanPurpose.valueOf(arguments.getString(PARAM_SCAN_PURPOSE))
        member = arguments.getSerializable(PARAM_MEMBER) as Member?
        qrCodeDetectorManager = QrCodeDetectorManager(activity)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            throw Exception("Camera permission not granted")
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.barcode_fragment_label)
        return inflater?.inflate(R.layout.fragment_barcode, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        barcode_preview_surface.holder.addCallback(this)

        if (scanPurpose != ScanPurpose.ID) {
            search_member.visibility = View.GONE
        } else {
            search_member.setOnClickListener {
                navigationManager.goTo(SearchMemberFragment())
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (qrCodeDetectorManager.isOperational()) {
            qrCodeDetectorManager.start(QrCodeDetector(), holder)
        } else {
            // TODO: handle better
            throw Exception("BatcodeDetector is not operational")
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // no-op
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        qrCodeDetectorManager.releaseResources()
    }

    inner class QrCodeDetector : Detector.Processor<Barcode> {
        override fun release() {
            // no-op
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            val barcodes = detections?.detectedItems
            if (barcodes != null && barcodes.size() > 0) {
                val barcode = barcodes.valueAt(0)?.displayValue ?: return
                if (!Member.validCardId(barcode)) {
                    // TODO: show invalid card ID error notification
                } else {
                    when (scanPurpose) {
                    // TODO: probably want to restructure this Fragment into an Activity
                    // that returns this as a result - will hopefully simplify the call-out
                    // and back-stack complexity of this screen
                        ScanPurpose.ID -> {
                            val member = memberRepository.findByCardId(barcode)
                            if (member == null) {
                                // TODO: show member not found error
                            } else {
                                val openCheckIn = identificationEventRepository.openCheckIn(member.id)
                                if (openCheckIn == null) {
                                    navigationManager.goTo(CheckInMemberDetailFragment.forMember(member))
                                } else {
                                    navigationManager.goTo(
                                            CurrentMemberDetailFragment.forIdentificationEvent(openCheckIn))
                                }
                            }
                        }
                        ScanPurpose.MEMBER_EDIT -> {
                            // TODO: handle null member case
                            member?.let {
                                navigationManager.popTo(
                                        MemberEditFragment.forMember(it.copy(cardId = barcode)))
                            }
                        }
                        ScanPurpose.NEWBORN -> {
                            // TODO: handle null member
                            member?.let {
                                // TODO: don't set card
                                navigationManager.popTo(
                                        EnrollNewbornInfoFragment.forParent(it))
                            }
                        }
                    }
                }
            }
        }
    }
}
