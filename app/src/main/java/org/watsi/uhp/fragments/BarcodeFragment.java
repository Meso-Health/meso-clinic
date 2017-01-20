package org.watsi.uhp.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ReceptionActivity;

public class BarcodeFragment extends Fragment implements SurfaceHolder.Callback {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barcode_fragment, container, false);
        SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.barcode_preview_surface);
        surfaceView.getHolder().addCallback(this);
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        ReceptionActivity activity = (ReceptionActivity) getActivity();
        // TODO: this permission check is just used for debugging
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("UHP", "camera permission NOT granted");
        } else {
            Log.d("UHP", "camera permission granted");
        }
        activity.startBarcodeCapture(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // intentionally blank
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // intentionally blank
    }
}
