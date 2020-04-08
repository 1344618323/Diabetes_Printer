package com.example.lenovo.graduworkapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DIYDrawingView extends View {
    private static final String TAG = "BoxDrawingView";
    private Paint mBackgroundPaint;
    private Path mPath;
    private Paint mPaint;
    private float mposX, mposY;
    private int mLinesum;
    private OnDrawListener listener;

    public DIYDrawingView(Context context) {
        this(context, null);
    }

    public DIYDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        /**设置画笔变为圆滑状**/
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        /**设置线的宽度**/
        mPaint.setStrokeWidth(30);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(), event.getY());//单位是像素
        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(current.x, current.y);
                mLinesum = 0;
                action = "ACTION_DOWN";
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mposX, mposY, current.x, current.y);
                invalidate();
                action = "ACTION_MOVE";
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mPath.reset();
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                break;
        }

        mposX = current.x;
        mposY = current.y;
        mLinesum++;
        Log.i(TAG, action + " at x=" + current.x + " ,y= " + current.y);
        Log.i(TAG, "LineNum: " + mLinesum);
        this.draw(mLinesum);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);
        canvas.drawPath(mPath, mPaint);
    }

    public int getLinesum() {
        return mLinesum;
    }

    public PointF getCurrentPoint(){
        return new PointF(mposX,mposY);
    }


    public interface OnDrawListener {
        public void onDraw(DIYDrawingView view,int linesum);
    }
    public void draw(int linesum) {
        listener.onDraw(this,linesum);
    }
    public void setOnCxnListener(OnDrawListener listener) {
        this.listener = listener;
    }
}
