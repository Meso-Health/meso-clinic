package org.watsi.uhp.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;

public class BarcodeFragment extends Fragment implements SurfaceHolder.Callback {

    private CameraSource mCameraSource;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupBarcodeDetector();
        View view = inflater.inflate(R.layout.fragment_barcode, container, false);
        SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.barcode_preview_surface);
        surfaceView.getHolder().addCallback(this);
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MainActivity activity = (MainActivity) getActivity();
        // TODO: this permission check is just used for debugging
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UHP", "camera permission NOT granted");
        } else {
            Log.d("UHP", "camera permission granted");
        }
        try {
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
        // intentionally blank
    }

    private void setupBarcodeDetector() {
        final MainActivity activity = (MainActivity) getActivity();

        BarcodeDetector barcodeDetector = new BarcodeDetector
                .Builder(activity.getBaseContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!barcodeDetector.isOperational()) {
            // TODO: handle not being ready for barcode
            Log.d("UHP", "barcode detector is not operational");
        } else {
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                    Log.d("UHP", "barcode processor release");
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    if (barcodes.size() > 0) {
                        Barcode barcode = barcodes.valueAt(0);
                        if (barcode != null) {
                            try {
                                // TODO: lookup appropriate member Id once we determine barcode encoding scheme
                                Member member = MemberDao.all().get(0);
                                activity.setDetailFragment(String.valueOf(member.getId()));
                            } catch (SQLException e) {
                                Rollbar.reportException(e);
                            }
                        }
                    }
                }
            });

            mCameraSource = new CameraSource
                    .Builder(activity.getBaseContext(), barcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build();
        }
    }
}
