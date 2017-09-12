package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;

public class BarcodeFragment extends BaseFragment implements SurfaceHolder.Callback {

    private CameraSource mCameraSource;
    private Button mSearchMemberButton;
    private Toast mErrorToast;
    private ScanPurposeEnum mScanPurpose;

    public enum ScanPurposeEnum { ID, MEMBER_EDIT, NEWBORN }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.barcode_fragment_label);

        View view = inflater.inflate(R.layout.fragment_barcode, container, false);

        mScanPurpose = getScanPurposeFromArguments();

        SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.barcode_preview_surface);
        surfaceView.getHolder().addCallback(this);
        mSearchMemberButton = (Button) view.findViewById(R.id.search_member);

        if (!mScanPurpose.equals(ScanPurposeEnum.ID)) mSearchMemberButton.setVisibility(View.GONE);

        mErrorToast = Toast.makeText(getActivity(), R.string.id_not_found_toast, Toast.LENGTH_LONG);

        setupSearchMemberButton();
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            BarcodeDetector barcodeDetector = new BarcodeDetector
                    .Builder(getContext())
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();

            while (!barcodeDetector.isOperational()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    ExceptionManager.reportExceptionWarning(e);
                }
            }

            setBarcodeProcessor(barcodeDetector);
            mCameraSource.start(holder);
        } catch (IOException | SecurityException e) {
            ExceptionManager.reportException(e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // intentionally blank
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCameraSource.release();
    }

    private void setBarcodeProcessor(BarcodeDetector barcodeDetector) {
         barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // no-op
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    Barcode barcode = barcodes.valueAt(0);
                    if (barcode != null) {
                        try {
                            Member member;
                            IdentificationEvent idEvent;
                            switch (mScanPurpose) {
                                case ID:
                                    member = MemberDao.findByCardId(barcode.displayValue);
                                    idEvent = new IdentificationEvent(member, IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, null);
                                    getNavigationManager().setMemberDetailFragment(member, idEvent);
                                    break;
                                case MEMBER_EDIT:
                                    member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
                                    idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
                                    if (handleCardIdScan(member, barcode.displayValue)) {
                                        getNavigationManager().setMemberEditFragment(member, idEvent);
                                    }
                                    break;
                                case NEWBORN:
                                    member = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
                                    idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
                                    if (handleCardIdScan(member, barcode.displayValue)) {
                                        getNavigationManager().setEnrollNewbornInfoFragment(member, idEvent);
                                    }
                                    break;
                            }
                        } catch (SQLException e) {
                            displayFailureToast();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e1) {
                                ExceptionManager.reportExceptionWarning(e1);
                            }
                        }
                    }
                }
            }
         });

        mCameraSource = new CameraSource
                .Builder(getContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void setupSearchMemberButton() {
        mSearchMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationManager().setSearchMemberFragment();
            }
        });
    }

    private boolean handleCardIdScan(Member member, String barcodeDisplayValue) {
        if (Member.validNonNullCardId(barcodeDisplayValue)) {
            member.setCardId(barcodeDisplayValue);
            return true;
        } else {
            ExceptionManager.reportErrorMessage("Detected invalid card when scanning card for member edit. Card scanned: " + member.getCardId());
            mErrorToast.setText("Invalid card ID. ID must be 3 letters followed by 6 numbers.");
            displayFailureToast();
            return false;
        }
    }

    private void displayFailureToast() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                // prevents numerous toasts from being triggered by the barcode detector's
                // "receive detections" thread
                if (!mErrorToast.getView().isShown()) {
                    mErrorToast.show();
                }
            }
        });
    }

    private ScanPurposeEnum getScanPurposeFromArguments() {
        return ScanPurposeEnum.valueOf(
                getArguments().getString(NavigationManager.SCAN_PURPOSE_BUNDLE_FIELD, ""));
    }

    @Override
    public String getName() {
        // We want distinct entries in the backstack per member so that we can back through family members.
        return "BarcodeFragment-" + getScanPurposeFromArguments();
    }
}
