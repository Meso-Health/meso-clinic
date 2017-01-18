package org.watsi.uhp.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;

import java.io.IOException;

public class DefaultFragment extends Fragment {

    private BarcodeDetector mBarcodeDetector;
    private CameraSource mCameraSource;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.default_fragment, container, false);
        mBarcodeDetector = new BarcodeDetector
                .Builder(getContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!mBarcodeDetector.isOperational()) {
            // TODO: handle not being ready for barcode
        } else {
            mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                    Log.d("UHP", "barcode processor release");
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    Barcode barcode = detections.getDetectedItems().get(0);
                    if (barcode != null) {
                        Log.d("UHP", "got a barcode");
                    }
                }
            });

            mCameraSource = new CameraSource
                    .Builder(getContext(), mBarcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedFps(15.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            Button scanBarcodeButton = (Button) view.findViewById(R.id.barcode_button);
            scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
                        } else {
                            mCameraSource.start();
                            Log.d("UHP", "camera source started");
                        }
                    } catch (IOException e) {
                        Rollbar.reportException(e);
                    }
                }
            });
        }

        return view;
    }
}
