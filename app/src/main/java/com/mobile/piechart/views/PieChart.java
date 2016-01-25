package com.mobile.piechart.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;

import com.mobile.piechart.R;

/**
 * @author David Castillo Fuentes
 * This is a very basic implementation of a PieGraph, it can draws 3 circles
 * calculated in the onLayout phase and drawed in the onDraw callback
 */
public class PieChart extends View {

    private enum CIRCLE {
        CIRCLE1,
        CIRCLE2,
        CIRCLE3,
    }

    public static final String LOG = PieChart.class.getName();
    private static final float START_ANGLE_DEFAULT = 0;
    private static final float END_ANGLE_DEFAULT = 180;
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_DURATION1 = 1500;
    private static final int DEFAULT_DURATION2 = 2000;
    private static final int DEFAULT_DURATION3 = 2500;
    private static final int DEFAULT_ALPHA_VALUE = 50;

    private Paint mPaint;
    private float mStrokeWidthPercentageBase = 0.07f;
    private float mBaseSize;

    private RectF mRectCircle1;
    private int mCircleColor1 = DEFAULT_COLOR;
    private float mCircleInitAngle1 = START_ANGLE_DEFAULT;
    private float mCircleEndAngle1 = END_ANGLE_DEFAULT;

    private RectF mRectCircle2;
    private int mCircleColor2 = DEFAULT_COLOR;
    private float mCircleInitAngle2 = START_ANGLE_DEFAULT;
    private float mCircleEndAngle2 = END_ANGLE_DEFAULT;

    private RectF mRectCircle3;
    private int mCircleColor3 = DEFAULT_COLOR;
    private float mCircleInitAngle3 = START_ANGLE_DEFAULT;
    private float mCircleEndAngle3 = END_ANGLE_DEFAULT;

    public PieChart(Context context) {
        super(context);
        init(null, 0);
    }

    public PieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        // Load attributes
        try {

            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PieChart, defStyle, 0);

            mCircleColor1 = a.getColor(R.styleable.PieChart_circle1Color, DEFAULT_COLOR);
            mCircleInitAngle1 = a.getFloat(R.styleable.PieChart_circle1InitialAngle, START_ANGLE_DEFAULT);
            mCircleEndAngle1 = a.getFloat(R.styleable.PieChart_circle1EndAngle, END_ANGLE_DEFAULT);

            mCircleColor2 = a.getColor(R.styleable.PieChart_circle2Color, DEFAULT_COLOR);
            mCircleInitAngle2 = a.getFloat(R.styleable.PieChart_circle2InitialAngle, START_ANGLE_DEFAULT);
            mCircleEndAngle2 = a.getFloat(R.styleable.PieChart_circle2EndAngle, END_ANGLE_DEFAULT);

            mCircleColor3 = a.getColor(R.styleable.PieChart_circle3Color, DEFAULT_COLOR);
            mCircleInitAngle3 = a.getFloat(R.styleable.PieChart_circle3InitialAngle, START_ANGLE_DEFAULT);
            mCircleEndAngle3 = a.getFloat(R.styleable.PieChart_circle3EndAngle, END_ANGLE_DEFAULT);

            // Recycle
            a.recycle();

        } catch (Exception e) {
            Log.i(LOG, "Failed to load initial parameters :: " + e);
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        mRectCircle1 = new RectF();
        mRectCircle2 = new RectF();
        mRectCircle3 = new RectF();

        // Animation Set
        AnimationSet set = new AnimationSet(false);

        // Circle 1
        CircleAnimation animator = new CircleAnimation(this, mCircleEndAngle1, CIRCLE.CIRCLE1);
        animator.setDuration(DEFAULT_DURATION1);
        set.addAnimation(animator);

        // Circle 2
        animator = new CircleAnimation(this, mCircleEndAngle2, CIRCLE.CIRCLE2);
        animator.setDuration(DEFAULT_DURATION2);
        set.addAnimation(animator);

        // Circle 3
        animator = new CircleAnimation(this, mCircleEndAngle3, CIRCLE.CIRCLE3);
        animator.setDuration(DEFAULT_DURATION3);
        set.addAnimation(animator);

        // Start animation
        startAnimation(set);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw Circle 1
        mPaint.setStrokeWidth(mStrokeWidthPercentageBase * mBaseSize);
        mPaint.setColor(Color.argb(DEFAULT_ALPHA_VALUE, 0, 0, 0));
        canvas.drawArc(mRectCircle1, 0, 360, false, mPaint);
        mPaint.setColor(mCircleColor1);
        canvas.drawArc(mRectCircle1, mCircleInitAngle1, mCircleEndAngle1, false, mPaint);

        // Draw Circle 2
        mPaint.setStrokeWidth((mStrokeWidthPercentageBase - 0.01f) * mBaseSize);
        mPaint.setColor(Color.argb(DEFAULT_ALPHA_VALUE, 0, 0, 0));
        canvas.drawArc(mRectCircle2, 0, 360, false, mPaint);
        mPaint.setColor(mCircleColor2);
        canvas.drawArc(mRectCircle2, mCircleInitAngle2, mCircleEndAngle2, false, mPaint);

        // Draw Circle 3
        mPaint.setStrokeWidth((mStrokeWidthPercentageBase - 0.02f) * mBaseSize);
        mPaint.setColor(Color.argb(DEFAULT_ALPHA_VALUE, 0, 0, 0));
        canvas.drawArc(mRectCircle3, 0, 360, false, mPaint);
        mPaint.setColor(mCircleColor3);
        canvas.drawArc(mRectCircle3, mCircleInitAngle3, mCircleEndAngle3, false, mPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);

            // If there is no change, skip the calculation
            if (!changed) return;

            // Make all the calculations
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int contentWidth = getWidth() - paddingLeft - paddingRight;
            int contentHeight = getHeight() - paddingTop - paddingBottom;

            mBaseSize = Math.min(contentWidth, contentHeight);

            // Calculate the base left, top
            float mLeft;
            float mTop;
            float mRight;
            float mBottom;
            float padding = mStrokeWidthPercentageBase * mBaseSize;
            if (contentHeight > contentWidth) {
                mLeft = padding / 2f;
                mTop = (contentHeight - contentWidth + padding) / 2f;
                mRight = contentWidth - padding / 2f;
                mBottom = contentHeight - mTop;
            } else if (contentWidth > contentHeight) {
                mLeft = (contentWidth - contentHeight + padding) / 2f;
                mTop = padding / 2f;
                mRight = (contentWidth + contentHeight - padding) / 2f;
                mBottom = contentHeight - padding / 2f;
            } else {
                mLeft = padding / 2f;
                mTop = padding / 2f;
                mRight = contentWidth - padding / 2f;
                mBottom = contentHeight - padding / 2f;
            }

            // Measure & Position Circle 1
            mRectCircle1.left = mLeft + paddingLeft;
            mRectCircle1.top = mTop + paddingTop;
            mRectCircle1.right = mRight + paddingRight;
            mRectCircle1.bottom = mBottom + paddingBottom;

            // Measure & Position Circle 2
            mRectCircle2.left = mRectCircle1.left + 3 * padding;
            mRectCircle2.top = mRectCircle1.top + 3 * padding;
            mRectCircle2.right = mRectCircle1.right - 3 * padding;
            mRectCircle2.bottom = mRectCircle1.bottom - 3 * padding;

            // Measure & Position Circle 3
            mRectCircle3.left = mRectCircle1.left + 5 * padding;
            mRectCircle3.top = mRectCircle1.top + 5 * padding;
            mRectCircle3.right = mRectCircle1.right - 5 * padding;
            mRectCircle3.bottom = mRectCircle1.bottom - 5 * padding;
        }

    // Measure the custom view to the specified size
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = widthSize;
        } else {
            // Be whatever you want
            width = widthSize;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            height = heightSize;
        } else {
            // Be whatever you want
            height = heightSize;
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    /**
     * This inner class is the responsible of adding the animation
     * feature to the graph drawing process
     */
    private static class CircleAnimation extends Animation {

        private PieChart pieChart;
        private float newAngle;
        private CIRCLE circleNum;

        public CircleAnimation(PieChart pieChart, float newAngle, CIRCLE circleNum) {
            this.newAngle = newAngle;
            this.pieChart = pieChart;
            this.circleNum = circleNum;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            float angle = newAngle * interpolatedTime;
            if (circleNum == CIRCLE.CIRCLE1) {
                pieChart.setCircleEndAngle1(angle);
            } else if (circleNum == CIRCLE.CIRCLE2) {
                pieChart.setCircleEndAngle2(angle);
            } else if (circleNum == CIRCLE.CIRCLE3) {
                pieChart.setCircleEndAngle3(angle);
            }
            pieChart.invalidate();
        }
    }

    /**
     * @return returns the first circle's initial angle
     */
    public float getCircleInitAngle1() {
        return mCircleInitAngle1;
    }

    /**
     * @param circleInitAngle1 update the initial angle of the first circle and update the UI
     */
    public void setCircleInitAngle1(float circleInitAngle1) {
        this.mCircleInitAngle1 = circleInitAngle1;
        invalidate();
    }

    /**
     * @return returns the first circle's end angle
     */
    public float getCircleEndAngle1() {
        return mCircleEndAngle1;
    }

    /**
     * @param circleEndAngle1 update the end angle of the first circle and update the UI
     */
    public void setCircleEndAngle1(float circleEndAngle1) {
        this.mCircleEndAngle1 = circleEndAngle1;
        invalidate();
    }

    /**
     * @return returns the second circle's initial angle
     */
    public float getCircleInitAngle2() {
        return mCircleInitAngle2;
    }

    /**
     * @param circleInitAngle2 update the initial angle of the second circle and update the UI
     */
    public void setCircleInitAngle2(float circleInitAngle2) {
        this.mCircleInitAngle2 = circleInitAngle2;
        invalidate();
    }

    /**
     * @return returns the second circle's end angle
     */
    public float getCircleEndAngle2() {
        return mCircleEndAngle2;
    }

    /**
     * @param circleEndAngle2 update the end angle of the second circle and update the UI
     */
    public void setCircleEndAngle2(float circleEndAngle2) {
        this.mCircleEndAngle2 = circleEndAngle2;
        invalidate();
    }

    /**
     * @return returns the third circle's initial angle
     */
    public float getCircleInitAngle3() {
        return mCircleInitAngle3;
    }

    /**
     * @param circleInitAngle3 update the initial angle of the third circle and update the UI
     */
    public void setCircleInitAngle3(float circleInitAngle3) {
        this.mCircleInitAngle3 = circleInitAngle3;
        invalidate();
    }

    /**
     * @return returns the third circle's end angle
     */
    public float getCircleEndAngle3() {
        return mCircleEndAngle3;
    }

    /**
     * @param circleEndAngle3 update the end angle of the third circle and update the UI
     */
    public void setCircleEndAngle3(float circleEndAngle3) {
        this.mCircleEndAngle3 = circleEndAngle3;
        invalidate();
    }

}
