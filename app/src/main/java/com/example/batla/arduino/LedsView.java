package com.example.batla.arduino;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LedsView extends View {
    float x, y, radius = 100;
    int n, color;

    Paint paint;
    Canvas mCanvas;

    public LedsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCanvas = new Canvas();
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;
        super.onDraw(mCanvas);
    }

    public void drawLed(Canvas canvas, Paint p, int color) {
        p.setColor(color);
        canvas.drawCircle(x, y, radius, p);
    }
}
