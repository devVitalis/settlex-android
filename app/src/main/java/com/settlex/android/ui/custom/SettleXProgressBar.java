package com.settlex.android.ui.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

public class SettleXProgressBar extends View {

    private Paint ringPaint;
    private Paint flashPaint;
    private RectF ringBounds;
    private float rotationAngle;
    private int flashAlpha = 255;

    private ValueAnimator rotationAnimator;
    private ValueAnimator pulseAnimator;

    public SettleXProgressBar(Context context) {
        super(context);
        init();
    }

    public SettleXProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        /*------------------------------------
        Setup base ring paint with gradient
        -------------------------------------*/
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);

        /*------------------------------------
        Setup flash paint with soft blur
        -------------------------------------*/
        flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        flashPaint.setStyle(Paint.Style.STROKE);
        flashPaint.setMaskFilter(new BlurMaskFilter(20, BlurMaskFilter.Blur.NORMAL));

        /*------------------------------------
        Animate rotation angle for flash
        -------------------------------------*/
        rotationAnimator = ValueAnimator.ofFloat(0f, 360f);
        rotationAnimator.setDuration(800);
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.addUpdateListener(animation -> {
            rotationAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
        rotationAnimator.start();

        /*------------------------------------
        Animate flash alpha pulsing effect
        -------------------------------------*/
        pulseAnimator = ValueAnimator.ofInt(100, 255);
        pulseAnimator.setDuration(800);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(animation -> flashAlpha = (int) animation.getAnimatedValue());
        pulseAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        /*------------------------------------
          Dynamically size ring based on view
        -------------------------------------*/
        float ringWidth = 20f;
        ringPaint.setStrokeWidth(ringWidth);
        flashPaint.setStrokeWidth(ringWidth);

        float padding = ringWidth / 2f;
        ringBounds = new RectF(padding, padding, w - padding, h - padding);

        /*----------------------------------
        Soft radial gradient ring shader
        ----------------------------------*/
        Shader radialRingShader = new SweepGradient(
                w / 2f, h / 2f,
                new int[]{
                        Color.parseColor("#B3E5FC"),
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#1565C0"),
                        Color.parseColor("#B3E5FC")
                },
                new float[]{0f, 0.5f, 0.75f, 1f}
        );
        ringPaint.setShader(radialRingShader);

        /*------------------------------------
        Transparent blue flash shader
        -------------------------------------*/
        Shader flashShader = new SweepGradient(
                w / 2f, h / 2f,
                new int[]{
                        Color.TRANSPARENT,
                        Color.parseColor("#4433B5E5"),
                        Color.parseColor("#6633B5E5"),
                        Color.TRANSPARENT
                },
                new float[]{0.25f, 0.45f, 0.55f, 0.75f}
        );
        flashPaint.setShader(flashShader);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        /*------------------------------------
        Draw soft gradient ring
        -------------------------------------*/
        canvas.drawArc(ringBounds, 0, 360, false, ringPaint);

        /*------------------------------------
        Draw rotating flash overlay
        -------------------------------------*/
        flashPaint.setAlpha(flashAlpha); // apply pulsing alpha here

        canvas.save();
        canvas.rotate(rotationAngle, getWidth() / 2f, getHeight() / 2f);
        canvas.drawArc(ringBounds, 0, 360, false, flashPaint);
        canvas.restore();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        rotationAnimator.cancel();
        pulseAnimator.cancel();
    }
}
