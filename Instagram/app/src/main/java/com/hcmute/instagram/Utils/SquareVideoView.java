package com.hcmute.instagram.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class SquareVideoView extends VideoView {

    public SquareVideoView(Context context) {
        super(context);
    }

    public SquareVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // Make height same as width
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width; // Choose the smaller dimension
        setMeasuredDimension(size, size); // Set both width and height to the smaller dimension
    }
}
