package com.ogadai.ogadai_secure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SizeF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by alee on 30/09/2016.
 */

public class CameraView extends View implements CameraFeed.Listener {
    private ArrayList<CameraItem> mCameras;

    private boolean mAnimationRuning = false;
    private static final int ANIMATION_INTERVAL = 50;
    private static final int ANIMATION_STEP = 10;

    public CameraView(Context context) {
        super(context);
        init(null, 0);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public void close() {
        detachCameras();
    }

    public void setCameras(ArrayList<CameraFeed> cameras) {
        detachCameras();
        mCameras = new ArrayList<>();
        for(CameraFeed camera: cameras) {
            mCameras.add(new CameraItem(camera));
            camera.setFeedListener(this);
        }

        notifyDataSetChanged();
    }

    private void detachCameras() {
        if (mCameras != null) {
            for(CameraItem camera: mCameras) {
                camera.close();
            }
            mCameras = null;
        }
    }

    public void notifyDataSetChanged()
    {
        this.invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                selectAtPoint(x, y);
                break;
        }
        return true;
    }

    private void selectAtPoint(float x, float y) {
        // Sort for draw order
        ArrayList<CameraItem> cameraOrder = (ArrayList<CameraItem>)mCameras.clone();
        Collections.sort(cameraOrder, new CameraComparator(true));
        Rect contentRect = getContentRect();

        boolean found = false;
        for(CameraItem camera: cameraOrder) {
            RectF drawRect = camera.getDrawRect(contentRect);
            if (!found && (drawRect != null) && drawRect.contains(x, y)) {
                camera.setActive(!camera.isActive());
                found = true;
            } else {
                camera.setActive(false);
            }
        }

        checkAnimationTask();
    }

    private void checkAnimationTask() {
        if (!mAnimationRuning) {
            mAnimationRuning = true;
            triggerAnimationFrame();
        }
    }

    private void triggerAnimationFrame() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAnimationFrame();

                if (mAnimationRuning) {
                    triggerAnimationFrame();
                }
            }
        }, ANIMATION_INTERVAL);
    }

    private void updateAnimationFrame() {
        boolean continueAnimation = false;
        for(CameraItem camera: mCameras) {
            if (camera.isActive()) {
                if (camera.getExpandPercent() < 100) {
                    camera.setExpandPercent(Math.min(camera.getExpandPercent() + ANIMATION_STEP, 100));
                    continueAnimation = true;
                }
            } else {
                if (camera.getExpandPercent() > 0) {
                    camera.setExpandPercent(Math.max(camera.getExpandPercent() - ANIMATION_STEP, 0));
                    continueAnimation = true;
                }
            }
        }

        if (continueAnimation) {
            triggerAnimationFrame();
            this.invalidate();
        } else {
            mAnimationRuning = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCameras == null) return;

        int cols = (int)Math.ceil(mCameras.size());
        if (getWidth() < getHeight()) {
            cols = (cols <= 5) ? 1 : 2;
        } else {
            cols = (int)Math.ceil((float)cols / 2.0);
        }

        int rows = (int)Math.ceil((float)mCameras.size() / (float)cols);
        Rect contentRect = getContentRect();

        int contentWidth = contentRect.width() / cols;
        int contentHeight = contentRect.height() / rows;

        // Work out the camera rectangles
        for(int n = 0; n < mCameras.size(); n++) {
            PointF point = new PointF((n % cols) * contentWidth, (n / cols) * contentHeight);
            mCameras.get(n).setScreenRect(new RectF(point.x, point.y,
                    point.x + contentWidth, point.y + contentHeight));
        }

        // Sort for draw order
        ArrayList<CameraItem> cameraOrder = (ArrayList<CameraItem>)mCameras.clone();
        Collections.sort(cameraOrder, new CameraComparator());

        // Draw each image
        for(CameraItem camera: cameraOrder) {
            camera.draw(canvas, contentRect);
        }
    }

    private Rect getContentRect() {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        return new Rect(paddingLeft, paddingTop, getWidth() - paddingRight, getHeight() - paddingBottom);
    }

    @Override
    public void updated() {
        this.invalidate();
    }

    private class CameraItem {
        private CameraFeed mFeed;
        private boolean mActive = false;

        private RectF mScreenRect = null;
        private int mExpandPercent = 0;

        public CameraItem(CameraFeed feed) {
            mFeed = feed;
        }

        public void close() {
            mFeed.close();
        }

        public SizeF getImageSize() {
            Bitmap image = mFeed.getImage();
            if (image != null) {
                return new SizeF(image.getWidth(), image.getHeight());
            }
            return null;
        }

        public void draw(Canvas canvas, Rect contentRect) {
            RectF drawRect = getDrawRect(contentRect);
            if (drawRect != null) {
                frameRect(canvas, drawRect);

                Bitmap image = mFeed.getImage();
                if (image != null) {
                    RectF imageRect = imageRectFromDrawRect(drawRect);
                    canvas.drawBitmap(image, null, imageRect, null);
                } else {
                    drawCameraName(canvas, drawRect);
                }
            }
        }

        private void frameRect(Canvas canvas, RectF drawRect) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.GRAY);
            borderPaint.setStrokeWidth(2);
            borderPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(drawRect, borderPaint);

            Paint fillPaint = new Paint();
            fillPaint.setColor(Color.WHITE);
            fillPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(drawRect, fillPaint);
        }

        private void drawCameraName(Canvas canvas, RectF drawRect) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.BLACK);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(28);

            String cameraName = mFeed.getName();
            canvas.drawText(cameraName, drawRect.centerX(), drawRect.centerY() - 14, textPaint);
        }

        public RectF getScreenRect() { return mScreenRect; }
        public void setScreenRect(RectF drawRect) { mScreenRect = drawRect; }

        public boolean isActive() {
            return mActive;
        }

        public void setActive(boolean active) {
            mActive = active;
            mFeed.setStreaming(mActive);
        }

        public int getExpandPercent() {
            return mExpandPercent;
        }

        public void setExpandPercent(int expandPercent) {
            mExpandPercent = expandPercent;
        }

        public RectF getDrawRect(Rect contentRect) {
            if (mExpandPercent > 0 && mScreenRect != null) {
                float ratio = (float)mExpandPercent / 100f;
                return new RectF(
                        mScreenRect.left + ratio * (contentRect.left - mScreenRect.left),
                        mScreenRect.top + ratio * (contentRect.top - mScreenRect.top),
                        mScreenRect.right + ratio * (contentRect.right - mScreenRect.right),
                        mScreenRect.bottom + ratio * (contentRect.bottom - mScreenRect.bottom)
                );
            }
            return mScreenRect;
        }

        private RectF imageRectFromDrawRect(RectF drawRect) {
            SizeF imageSize = getImageSize();
            if (imageSize != null) {
                float ratioX = drawRect.width() / imageSize.getWidth();
                float ratioY = drawRect.height() / imageSize.getHeight();

                float ratio = Math.min(ratioX, ratioY);
                SizeF dstSize = new SizeF(imageSize.getWidth() * ratio, imageSize.getHeight() * ratio);
                PointF offset = new PointF(
                        drawRect.left + (drawRect.width() - dstSize.getWidth()) / 2,
                        drawRect.top + (drawRect.height() - dstSize.getHeight()) / 2);

                return new RectF(offset.x, offset.y, offset.x + dstSize.getWidth(), offset.y + dstSize.getHeight());
            }
            return null;
        }
    }

    private class CameraComparator implements Comparator<CameraItem>
    {
        private boolean mReverse;
        public CameraComparator() {
            mReverse = false;
        }
        public CameraComparator(boolean reverse) {
            mReverse = reverse;
        }

        @Override
        public int compare(CameraItem lhs, CameraItem rhs) {
            int result = (lhs.isActive() ? 1 : 0) - (rhs.isActive() ? 1 : 0);

            if (result == 0) {
                result = lhs.getExpandPercent() - rhs.getExpandPercent();
            }

            RectF leftRect = lhs.getScreenRect();
            RectF rightRect = rhs.getScreenRect();
            if (leftRect != null && rightRect != null) {
                if (result == 0) {
                    result = (int) leftRect.top - (int) rightRect.top;
                }

                if (result == 0) {
                    result = (int) leftRect.left - (int) rightRect.left;
                }
            }

            return mReverse ? -result : result;
        }
    }
}
