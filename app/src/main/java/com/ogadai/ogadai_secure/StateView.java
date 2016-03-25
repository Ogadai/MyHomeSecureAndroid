package com.ogadai.ogadai_secure;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.SizeF;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class StateView extends View {
    private ArrayList<StateItem> mStates;
    private ArrayList<StateImage> mStateImages;

    public StateView(Context context) {
        super(context);
        init(null, 0);
    }

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public StateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public ArrayList<StateItem> getStates() { return mStates; }
    public void setStates(ArrayList<StateItem> states) { mStates = states; notifyDataSetChanged(); }

    public ArrayList<StateImage> getStateImages() { return mStateImages; }
    public void setStateImages(ArrayList<StateImage> stateImages) { mStateImages = stateImages; notifyDataSetChanged(); }

    public void notifyDataSetChanged()
    {
        this.invalidate();
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mStateImages == null || mStates == null) return;

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        for(int n = 0; n < mStateImages.size(); n++) {
            StateImage imageInfo = mStateImages.get(n);
            for(int m = 0; m < mStates.size(); m++) {
                StateItem state = mStates.get(m);
                if (state.getName().toLowerCase().compareTo(imageInfo.getState()) == 0) {
                    DrawStateBitmap(canvas, state, imageInfo, contentWidth, contentHeight);
                }
            }
        }
    }

    private void DrawStateBitmap(Canvas canvas, StateItem state, StateImage imageInfo, int contentWidth, int contentHeight) {
        Bitmap stateBitmap = state.getActive() ? imageInfo.getActiveBitmap() : imageInfo.getInactiveBitmap();
        if (stateBitmap != null) {
            float ratioX = (float)contentWidth / (float)stateBitmap.getWidth();
            float ratioY = (float)contentHeight / (float)stateBitmap.getHeight();

            float ratio = Math.min(ratioX, ratioY);
            SizeF dstSize = new SizeF(stateBitmap.getWidth() * ratio, stateBitmap.getHeight() * ratio);
            PointF offset = new PointF((contentWidth - dstSize.getWidth()) / 2, (contentHeight - dstSize.getHeight()) / 2);

            RectF dst = new RectF(offset.x, offset.y, offset.x + dstSize.getWidth(), offset.y + dstSize.getHeight());

            canvas.drawBitmap(stateBitmap, null, dst, null);
        }
    }

}
