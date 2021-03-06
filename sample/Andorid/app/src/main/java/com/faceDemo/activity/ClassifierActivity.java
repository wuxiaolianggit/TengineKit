package com.faceDemo.activity;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Size;

import com.faceDemo.R;
import com.faceDemo.camera.CameraEngine;
import com.faceDemo.currencyview.OverlayView;
import com.faceDemo.encoder.BitmapEncoder;
import com.faceDemo.encoder.CircleEncoder;
import com.faceDemo.encoder.EncoderBus;
import com.faceDemo.encoder.RectEncoder;
import com.tenginekit.FaceManager;
import com.tenginekit.configs.FaceInfo;
import com.tenginekit.configs.LandmarkInfo;

import java.util.ArrayList;
import java.util.List;


public class ClassifierActivity extends CameraActivity {
    private static final String TAG = "ClassifierActivity";

    private OverlayView trackingOverlay;

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return new Size(1280, 960);
    }

    public void Registe() {
        /**
         * canvas 绘制人脸框，人脸关键点
         * */
        EncoderBus.GetInstance().Registe(new BitmapEncoder(this));
        EncoderBus.GetInstance().Registe(new CircleEncoder(this));
        EncoderBus.GetInstance().Registe(new RectEncoder(this));
    }

    @Override
    public void onPreviewSizeChosen(final Size size) {
        Registe();
        EncoderBus.GetInstance().onSetFrameConfiguration(previewHeight, previewWidth);

        trackingOverlay = (OverlayView) findViewById(R.id.facing_overlay);
        trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            @Override
            public void drawCallback(final Canvas canvas) {
                EncoderBus.GetInstance().onDraw(canvas);
            }
        });
    }

    @Override
    protected void processImage() {
        getCameraBytes();
        int degree = CameraEngine.getInstance().getCameraOrientation(sensorEventUtil.orientation);
        FaceInfo[] faceInfos = FaceManager.getInstance().getFaceInfo(mNV21Bytes);
        FaceManager.getInstance().setRotation(degree - 90, false, CameraActivity.ScreenWidth, CameraActivity.ScreenHeight);
        if (faceInfos != null && faceInfos.length > 0) {
            Rect[] face_rect = new Rect[faceInfos.length];
            List<List<LandmarkInfo>> face_landmarks = new ArrayList<>();
            for (int i = 0; i < faceInfos.length; i++) {
                face_rect[i] = faceInfos[i].faceRect;
                face_landmarks.add(faceInfos[i].landmarks);
            }
            EncoderBus.GetInstance().onProcessResults(face_rect);
            EncoderBus.GetInstance().onProcessResults(face_landmarks);
        }

        runInBackground(new Runnable() {
            @Override
            public void run() {
                readyForNextImage();
                trackingOverlay.postInvalidate();
            }
        });
    }
}