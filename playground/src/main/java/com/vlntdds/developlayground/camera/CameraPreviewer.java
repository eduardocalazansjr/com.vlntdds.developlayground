package com.vlntdds.developlayground.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eduardo C. on 10/05/2016
 * Camera Previewer to extend SurfaceView class, put the camera preview in a proportion
 * of 1:1 and deal with on-touch-focus/on-pinch-zoom
 */

@SuppressWarnings("deprecation")
public class CameraPreviewer extends SurfaceView {

    private static final double PHOTO_RATIO = 3.0 / 4.0;
    private Camera mCamera;
    private int CameraMaxZoom;
    private boolean CameraSupportsZoom;
    private boolean CameraIsFocused;
    private int CameraZoomFactor = 1;
    private boolean CameraIsReadyToFocus;
    private Camera.Area CameraFocusArea;
    private ArrayList<Camera.Area> CameraFocusAreas;
    private ScaleGestureDetector mScaleGestureDetector;
    private float LastTouchPointX;
    private float LastTouchPointY;
    private int mActivePointer = -1;

    public CameraPreviewer(Context c) {
        super(c);
        initialize(c);
    }

    public CameraPreviewer(Context c, AttributeSet a) {
        super(c, a);
        initialize(c);
    }

    public CameraPreviewer(Context c, AttributeSet a, int s) {
        super (c, a, s);
        initialize(c);
    }

    public void initialize(Context c) {
        mScaleGestureDetector = new ScaleGestureDetector(c, new ScaleListener());
        CameraFocusArea = new Camera.Area(new Rect(), 1000);
        CameraFocusAreas = new ArrayList<>();
        CameraFocusAreas.add(CameraFocusArea);
    }

    @Override
    protected void onMeasure(int width, int height) {
        width = MeasureSpec.getSize(width);
        height = MeasureSpec.getSize(height);

        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            if (height > width * PHOTO_RATIO)
                height = (int) (width * PHOTO_RATIO + 0.5);
            else
                width = (int) (height / PHOTO_RATIO + 0.5);
         } else {
            if (width > height * PHOTO_RATIO)
                width = (int) (height * PHOTO_RATIO + 0.5);
            else
                height = (int) (width / PHOTO_RATIO + 0.5);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleGestureDetector.onTouchEvent(e);

        int event = e.getAction();
        switch (event & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_UP:
                if (CameraIsFocused && CameraIsReadyToFocus)
                    focusHandler(mCamera.getParameters());
                mActivePointer = -1;
                break;

            case MotionEvent.ACTION_DOWN:
                CameraIsFocused = true;
                LastTouchPointX = e.getX();
                LastTouchPointY = e.getY();
                mActivePointer = e.getPointerId(0);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mCamera.cancelAutoFocus();
                CameraIsFocused = false;
                break;

            case MotionEvent.ACTION_CANCEL:
                mActivePointer = -1;
                break;

        }
        return true;
    }

    public void initCamera(Camera camera) {
        mCamera = camera;

        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            CameraSupportsZoom = p.isZoomSupported();
            if (CameraSupportsZoom)
                CameraMaxZoom = p.getMaxZoom();
        }
    }

    public void focusHandler(Camera.Parameters p) {
        float x = LastTouchPointX;
        float y = LastTouchPointY;

        if (!focusLimit(x, y)) return;

        List<String> focusModes = p.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            p.setFocusAreas(CameraFocusAreas);
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setParameters(p);
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                }
            });
        }

    }

    public boolean focusLimit(float x, float y) {
        int left = (int) (x - 100 / 2);
        int right = (int) (x + 100 / 2);
        int top = (int) (y - 100 / 2);
        int bottom = (int) (y + 100 / 2);

        if (-1000 > left || left > 1000) return false;
        if (-1000 > right || right > 1000) return false;
        if (-1000 > top || top > 1000) return false;
        if (-1000 > bottom || bottom > 1000) return false;

        CameraFocusArea.rect.set(left, top, right, bottom);
        return true;
    }

    public void zoomHandler(Camera.Parameters p) {
        int zoom = p.getZoom();

        if (CameraZoomFactor == 1) {
            if (zoom < CameraMaxZoom) zoom += 1;
        } else if (CameraZoomFactor == 0) {
            if (zoom > 0) zoom -= 1;
        }
        p.setZoom(zoom);
        mCamera.setParameters(p);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector d) {
            CameraZoomFactor = (int) d.getScaleFactor();
            zoomHandler(mCamera.getParameters());
            return true;
        }
    }

}
