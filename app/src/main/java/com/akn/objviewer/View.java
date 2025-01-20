package com.akn.objviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import java.lang.reflect.Field;

public class View extends GLSurfaceView {
    private static final float SENSITIVITY = 16;

    private static class FlingHandler extends FloatPropertyCompat<com.akn.objviewer.Renderer> {
        private float currentValue;

        public FlingHandler(String name) {
            super(name);
            currentValue = 0;
        }

        @Override
        public float getValue(com.akn.objviewer.Renderer object) {
            return currentValue;
        }

        @Override
        public void setValue(com.akn.objviewer.Renderer object, float value) {
            currentValue = value;
        }
    }

    private class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public boolean onScale(@NonNull ScaleGestureDetector scaleGestureDetector) {
            renderer.setScale(renderer.getScale() * scaleGestureDetector.getScaleFactor());
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector scaleGestureDetector) {}
    }

    private class GestureListener implements GestureDetector.OnGestureListener {
        private FlingAnimation flingAnimationX, flingAnimationY;

        @Override
        public boolean onDown(@NonNull MotionEvent motionEvent) {
            if (flingAnimationX != null)
                flingAnimationX.cancel();
            if (flingAnimationY != null)
                flingAnimationY.cancel();
            return true;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent motionEvent) {}

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
            return false;
        }

        private float toAngle(float offset) {
            return (float) (offset / (Math.PI * renderer.getTranslationZ()) * 180 * SENSITIVITY);
        }

        @Override
        public boolean onScroll(
                @Nullable MotionEvent motionEvent,
                @NonNull MotionEvent motionEvent1,
                float velocityX,
                float velocityY
        ) {
            if (scaleGestureDetector.isInProgress())
                return false;
            renderer.setRotation(
                    renderer.getRotationX() + toAngle(velocityY * renderer.getViewYFactor()),
                    renderer.getRotationY() - toAngle(velocityX * renderer.getViewXFactor())
            );
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent motionEvent) {}

        @Override
        public boolean onFling(
                @Nullable MotionEvent motionEvent,
                @NonNull MotionEvent motionEvent1,
                float velocityX,
                float velocityY
        ) {
            flingAnimationX = new FlingAnimation(
                    renderer,
                    new FlingHandler("flingX") {
                        @Override
                        public void setValue(com.akn.objviewer.Renderer object, float value) {
                            float velocity = value - getValue(object);
                            object.setRotation(
                                    object.getRotationX(),
                                    object.getRotationY()
                                            + toAngle(velocity * object.getViewXFactor())
                            );
                            super.setValue(object, value);
                        }
                    }
            );
            flingAnimationX.setStartVelocity(velocityX).start();
            flingAnimationY = new FlingAnimation(
                    renderer,
                    new FlingHandler("flingY") {
                        @Override
                        public void setValue(com.akn.objviewer.Renderer object, float value) {
                            float velocity = value - getValue(object);
                            object.setRotation(
                                    object.getRotationX()
                                            - toAngle(velocity * object.getViewYFactor()),
                                    object.getRotationY()
                            );
                            super.setValue(object, value);
                        }
                    }
            );
            flingAnimationY.setStartVelocity(velocityY).start();
            return true;
        }
    }

    private com.akn.objviewer.Renderer renderer;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;

    public View(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
        gestureDetector.setIsLongpressEnabled(false);
        try {
            @SuppressLint("DiscouragedPrivateApi")
            Field f_mTouchSlopSquare = GestureDetector.class.getDeclaredField("mTouchSlopSquare");
            f_mTouchSlopSquare.setAccessible(true);
            f_mTouchSlopSquare.setInt(gestureDetector, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
    }

    public View(Context context) {
        this(context, null);
    }

    public void setRenderer(com.akn.objviewer.Renderer renderer) {
        super.setRenderer(renderer);
        this.renderer = renderer;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (renderer == null)
            return false;
        boolean result = scaleGestureDetector.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event) || result;
    }
}
