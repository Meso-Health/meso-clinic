package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;

public class BarcodeFragment extends Fragment implements SurfaceHolder.Callback {

    private CameraSource mCameraSource;
    private Button mSearchMemberButton;
    private Toast mErrorToast;
    private ScanPurposeEnum mScanPurpose;
    private Member mMember = null;
    private IdentificationEvent.SearchMethodEnum mIdMethod = null;

    public enum ScanPurposeEnum { ID, MEMBER_EDIT, NEWBORN }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().setTitle(R.string.barcode_fragment_label);

        View view = inflater.inflate(R.layout.fragment_barcode, container, false);

        mScanPurpose = ScanPurposeEnum.valueOf(
                getArguments().getString(NavigationManager.SCAN_PURPOSE_BUNDLE_FIELD, ""));

        SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.barcode_preview_surface);
        surfaceView.getHolder().addCallback(this);
        mSearchMemberButton = (Button) view.findViewById(R.id.search_member);

        if (!mScanPurpose.equals(ScanPurposeEnum.ID)) {
            mSearchMemberButton.setVisibility(View.GONE);
            mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
            String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
            if (searchMethodString != null) {
                mIdMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
            }
        }

        mErrorToast = Toast.makeText(getActivity().getApplicationContext(),
                R.string.id_not_found_toast, Toast.LENGTH_LONG);

        setupSearchMemberButton();
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Log.d("UHP", "surface created");
            BarcodeDetector barcodeDetector = new BarcodeDetector
                    .Builder(getContext())
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();

            while (!barcodeDetector.isOperational()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Rollbar.reportException(e);
                }
            }

            setBarcodeProcessor(barcodeDetector);
            mCameraSource.start(holder);
        } catch (IOException | SecurityException e) {
            Rollbar.reportException(e);
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
        final MainActivity activity = (MainActivity) getActivity();

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
                            switch (mScanPurpose) {
                                case ID:
                                    Member member = MemberDao.findByCardId(barcode.displayValue);
                                    new NavigationManager(activity).setDetailFragment(
                                            member.getId(),
                                            IdentificationEvent.SearchMethodEnum.SCAN_BARCODE,
                                            null
                                    );
                                    break;
                                case MEMBER_EDIT:
                                    new NavigationManager(activity).setMemberEditFragment(
                                            mMember.getId(),
                                            mIdMethod,
                                            barcode.displayValue
                                    );
                                    break;
                                case NEWBORN:
                                    new NavigationManager(activity).setEnrollNewbornInfoFragment(
                                            mMember,
                                            barcode.displayValue
                                    );
                                    break;
                            }
                        } catch (SQLException e) {
                            displayFailureToast();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e1) {
                                Rollbar.reportException(e1);
                            }
                        }
                    }
                }
            }
         });

        Log.d("UHP", "camera source started");
        mCameraSource = new CameraSource
                .Builder(activity.getBaseContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void setupSearchMemberButton() {
        mSearchMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setSearchMemberFragment();
            }
        });
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
}
