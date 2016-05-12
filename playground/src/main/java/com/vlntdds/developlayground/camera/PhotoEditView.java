package com.vlntdds.developlayground.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Eduardo C on 11/05/2016
 */

public class PhotoEditView extends ImageView {

    public PhotoEditView(Context context) {super(context);}

    public PhotoEditView(Context context, AttributeSet attrs) {super(context, attrs);}

    public PhotoEditView(Context context, AttributeSet attrs, int defStyleAttr) {super(context, attrs, defStyleAttr);}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int squareLen = width > height ? height : width;
        setMeasuredDimension(squareLen, squareLen);
    }
}
