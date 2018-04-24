package org.watsi.uhp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_barcode.barcode_preview_surface
import kotlinx.android.synthetic.main.fragment_barcode.search_member

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.uhp.R
import org.watsi.uhp.managers.ExceptionManager
import org.watsi.uhp.managers.NavigationManager

import java.io.IOException

import javax.inject.Inject

class BarcodeFragment : DaggerFragment(), SurfaceHolder.Callback {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var identificationEventRepository: IdentificationEventRepository

    lateinit var scanPurpose: ScanPurpose
    var member: Member? = null
    var cameraSource: CameraSource? = null

    companion object {
        const val PARAM_SCAN_PURPOSE = "scan_purpose"
        const val PARAM_MEMBER = "member"

        fun forPurpose(purpose: ScanPurpose, member: Member? = null): BarcodeFragment {
            val fragment = BarcodeFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_SCAN_PURPOSE, purpose)
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
                // TODO: navigate to SearchMemberFragment
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            val barcodeDetector = BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build()

            while (!barcodeDetector.isOperational) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    ExceptionManager.reportExceptionWarning(e)
                }
            }

            setBarcodeProcessor(barcodeDetector)
            cameraSource?.start(holder)
        } catch (e: IOException) {
            ExceptionManager.reportException(e)
        } catch (e: SecurityException) {
            ExceptionManager.reportException(e)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // no-op
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        cameraSource?.release()
    }

    private fun setBarcodeProcessor(barcodeDetector: BarcodeDetector) {
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                // no-op
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
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
        })

        cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build()
    }
}
