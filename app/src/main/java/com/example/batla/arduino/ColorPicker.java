package com.example.batla.arduino;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPicker extends View {

    private Paint mPaint;
    Paint mCenterPaint;
    private final int[] mColors;

    private boolean mHighlightCenter;
    private boolean mTrackingCenter;

    private final int CENTER_X = 500;
    private final int CENTER_Y = 350;
    private final int CENTER_RADIUS = 100;

    public ColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        mColors = new int[]{
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFF00, 0xFFFF0000
        };
        Shader s = new SweepGradient(0, 0, mColors, null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(60); // внешний круг

        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setStrokeWidth(5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float r = CENTER_X - mPaint.getStrokeWidth() * 5.0f;

        canvas.translate(CENTER_X, CENTER_Y);

        canvas.drawOval(new RectF(-r, -r, r, r), mPaint); //внешний
        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint); // точка

        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);

            if (mHighlightCenter) {
                mCenterPaint.setAlpha(0xFF);
            } else {
                mCenterPaint.setAlpha(0x80);
            }
            canvas.drawCircle(0, 0, CENTER_RADIUS + mCenterPaint.getStrokeWidth(), mCenterPaint);

            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
    }

    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }

        float p = unit * (colors.length - 1);
        int i = (int) p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i + 1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);

        return Color.argb(a, r, g, b);
    }

//        private int rotateColor(int color, float rad) {
//            float deg = rad * 180 / 3.1415927f;
//            int r = Color.red(color);
//            int g = Color.green(color);
//            int b = Color.blue(color);
//
//            ColorMatrix cm = new ColorMatrix();
//            ColorMatrix tmp = new ColorMatrix();
//
//            cm.setRGB2YUV();
//            tmp.setRotate(0, deg);
//            cm.postConcat(tmp);
//            tmp.setYUV2RGB();
//            cm.postConcat(tmp);
//
//            final float[] a = cm.getArray();
//
//            int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
//            int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
//            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
//
//            return Color.argb(Color.alpha(color), pinToByte(ir),
//                    pinToByte(ig), pinToByte(ib));
//        }

    private static final float PI = 3.1415926f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= CENTER_RADIUS;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float angle = (float) java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle / (2 * PI);
                    if (unit < 0) {
                        unit += 1;
                    }

                    mCenterPaint.setColor(interpColor(mColors, unit));
                    sendColor();
                    invalidate();
                }
                break;
//            case MotionEvent.ACTION_UP:
//                if (mTrackingCenter) {
//                    if (inCenter) {
//                        sendColor();
//                    }
//                    mTrackingCenter = false;    // so we draw w/o halo
//                    invalidate();
//                }
//                break;
        }
        return true;
    }

    private void sendColor() {
        int red = Color.red(mCenterPaint.getColor());
        int green = Color.green(mCenterPaint.getColor());
        int blue = Color.blue(mCenterPaint.getColor());
        String rgb = red+"."+green+"."+blue + "\r";
        byte[] bytesToSend = rgb.getBytes();
        MainActivity.myThreadConnected.write(bytesToSend);
        Log.i("mytag", rgb);
    }
}