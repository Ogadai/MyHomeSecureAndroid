package com.ogadai.ogadai_secure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.SizeF;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by alee on 30/09/2016.
 */

public class CameraView extends View implements CameraFeed.Listener {
    private ArrayList<CameraFeed> mCameras;

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

    public ArrayList<CameraFeed> getCameras() { return mCameras; }
    public void setCameras(ArrayList<CameraFeed> cameras) {
        detachCameras();
        mCameras = cameras;
        attachCameras();

        notifyDataSetChanged();
    }

    private void attachCameras() {
        if (mCameras != null) {
            for(CameraFeed camera: mCameras) {
                camera.setFeedListener(this);
            }
        }
    }
    private void detachCameras() {
        if (mCameras != null) {
            for(CameraFeed camera: mCameras) {
                camera.setFeedListener(null);
            }
        }
    }

    public void notifyDataSetChanged()
    {
        this.invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCameras == null) return;

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int cols = (getWidth() > getHeight() && mCameras.size() > 1) ? 2 : 1;
        int rows = (int)Math.ceil((float)mCameras.size() / (float)cols);

        int contentWidth = (getWidth() - paddingLeft - paddingRight) / cols;
        int contentHeight = (getHeight() - paddingTop - paddingBottom) / rows;

        for(int n = 0; n < mCameras.size(); n++) {
            DrawBitmap(canvas, mCameras.get(n).getImage(),
                    (n % cols) * contentWidth, (n / cols) * contentHeight,
                    contentWidth, contentHeight);
        }
    }

    @Override
    public void updated() {
        this.invalidate();
    }

    private void DrawBitmap(Canvas canvas, Bitmap image, int left, int top, int contentWidth, int contentHeight) {
        if (image != null) {
            float ratioX = (float)contentWidth / (float)image.getWidth();
            float ratioY = (float)contentHeight / (float)image.getHeight();

            float ratio = Math.min(ratioX, ratioY);
            SizeF dstSize = new SizeF(image.getWidth() * ratio, image.getHeight() * ratio);
            PointF offset = new PointF(left + (contentWidth - dstSize.getWidth()) / 2, top + (contentHeight - dstSize.getHeight()) / 2);

            RectF dst = new RectF(offset.x, offset.y, offset.x + dstSize.getWidth(), offset.y + dstSize.getHeight());

            canvas.drawBitmap(image, null, dst, null);
        }
//        Paint myPaint = new Paint();
//        myPaint.setStyle(Paint.Style.STROKE);
//        myPaint.setColor(Color.rgb(0, 0, 0));
//        myPaint.setStrokeWidth(10);
//        canvas.drawRect(left, top, left + contentWidth, top + contentHeight, myPaint);
    }
}
