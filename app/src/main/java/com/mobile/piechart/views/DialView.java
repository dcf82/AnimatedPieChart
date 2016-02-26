package com.mobile.piechart.views;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.mobile.piechart.R;

/**
 * @author David Castillo Fuentes
 * This is an implementation of a Time Selector, in which you can set a given amount of time by
 * adding minutes when you dial the selector in clockwise and removing minutes by dialing in the
 * opposite side.
 *
 * If the Vibrator is available in your device & proper permissions are added in your manifest
 * file and granted by the user, it will vibrate for each minute you add/remove from the selector
 */
public class DialView extends View {
    private static final int MINUTES_PER_HOUR = 60;
    private static final int DEFAULT_TEXT_SIZE = 20;
    private static final int MIN_ANGLE_REQUIRED = -90;
    private static final int MAX_ANGLE_REQUIRED = 8550;
    private static final int DEFAULT_ALPHA_VALUE = 255;
    private static final int INVALID_PROGRESS_VALUE = -1;
    private static final int MINUTE_VALUE_TO_DEGREES_STEP_SIZE = 6;
    private static final float BASE_STROKE_WIDTH_PERCENTAGE = 0.01f;

    private Paint mPaint;

    private float mTextSize;
    private float mBaseSize;

    private float centerX;
    private float centerY;

    private float mMinutesRadio;
    private float mRadioKnob;

    private final int[] minutes = {30, 45, 60, 15};

    private long mCurrentAngle = MIN_ANGLE_REQUIRED;
    private long mCurrentTime;
    private long mVibrator1;
    private long mVibrator2;

    private float mTouchIgnoreRadius;
    private double mTouchAngle;

    private int mTextColor;
    private int mAngle1;
    private int mAngle2;
    private int mAngle;
    private int mDiff;

    private Drawable mLinesImage;
    private Drawable mOvalImage;
    private Drawable mKnobImage;

    private Vibrator mVibrator;
    private boolean mVibratorPermissionEnabled;
    private OnDialViewChangeListener mOnDialViewChangeListener;

    public interface OnDialViewChangeListener {

        /**
         * Client can be notified when the progress level has changed.
         *
         * @param dialView
         *            The DialView whose progress has changed
         * @param progress
         *            The current progress time. This will be reported in seconds
         */
        void onProgressChanged(DialView dialView, long progress, boolean changed);

    }

    public DialView(Context context) {
        super(context);
        init(null, 0);
    }

    public DialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DialView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE,
                getResources().getDisplayMetrics());
        mTextColor = Color.argb(DEFAULT_ALPHA_VALUE, 138, 138, 138);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);

        int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.VIBRATE);
        mVibratorPermissionEnabled = permissionCheck == PackageManager.PERMISSION_GRANTED;
        if (mVibratorPermissionEnabled) {
            mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Point point;
        int index = 0;

        // Draw Texts for Minutes
        for (int i = 1; i <= MINUTES_PER_HOUR; i++) {
            if (i % 15 == 0) {
                // Get coordinates for the quadrants (every 15 minutes)
                point = buildCoordinateXY(mMinutesRadio, centerX, centerY,
                        MINUTE_VALUE_TO_DEGREES_STEP_SIZE * i);
                // Draw quadrant time (every 15 minutes)
                canvas.drawText(Integer.toString(minutes[index++]), point.x, point.y +
                        mTextSize / 3, mPaint);
            }
        }

        // Draw Lines Image
        mLinesImage.draw(canvas);

        // Draw Oval Image
        mOvalImage.draw(canvas);

        // Draw Minutes Indicator Image
        canvas.save();
        canvas.rotate(normalizeCurrentAngle(), centerX, centerY);
        mKnobImage.draw(canvas);
        canvas.restore();
    }

    protected long normalizeCurrentAngle() {
        return (mCurrentAngle / MINUTE_VALUE_TO_DEGREES_STEP_SIZE) *
                MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
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

        // Calculate the base top, left, button, right
        float mLeft;
        float mTop;
        float mRight;
        float mBottom;
        float padding = BASE_STROKE_WIDTH_PERCENTAGE * mBaseSize;

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

        // Measure & Position Minutes Text Area
        RectF rect = new RectF();
        RectF rectMinutes = new RectF();
        rectMinutes.left = mLeft + paddingLeft;
        rectMinutes.top = mTop + paddingTop;
        rectMinutes.right = mRight + paddingRight;
        rectMinutes.bottom = mBottom + paddingBottom;

        mMinutesRadio = Math.min(rectMinutes.width() / 2f, rectMinutes.height() / 2f);

        // Measure & Position Lines Image
        rectMinutes.left += mTextSize;
        rectMinutes.top += mTextSize;
        rectMinutes.right -= mTextSize;
        rectMinutes.bottom -= mTextSize;

        // Getting Lines Image
        mLinesImage = getImageResized(R.drawable.lines, rectMinutes.width(), rectMinutes.height());
        mLinesImage.setBounds((int)rectMinutes.left, (int) rectMinutes.top,
                (int) rectMinutes.right, (int) rectMinutes.bottom);

        // Measure & Position Oval Image
        rect.left = rectMinutes.left + 6f * padding;
        rect.top = rectMinutes.top + 6f * padding;
        rect.right = rectMinutes.right - 0f * padding;
        rect.bottom = rectMinutes.bottom - 0f * padding;

        // Getting Oval Image
        mOvalImage = getImageResized(R.drawable.oval, rect.width(), rect.height());
        mOvalImage.setBounds((int) rect.left, (int) rect.top,
                (int) rect.right, (int) rect.bottom);

        // Measure & Position Knob Image
        rect.left = rectMinutes.left + 5f * padding;
        rect.top = rectMinutes.top + 5f * padding;
        rect.right = rectMinutes.right - 5f * padding;
        rect.bottom = rectMinutes.bottom - 5f * padding;

        mRadioKnob = Math.min(rect.width() / 2f, rect.height() / 2f);

        // Getting Knob Image
        mKnobImage = getImageResized(R.drawable.knob, rect.width(), rect.height());
        mKnobImage.setBounds((int) rect.left, (int) rect.top,
                (int) rect.right, (int) rect.bottom);

        // Calculate the center of the view
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        setTouchInSide();
    }

    /**
     * simply re-sizes a given drawable resource to the given width and height */
    private Drawable getImageResized(int resId, float newWidth, float newHeight) {

        // Load the original Bitmap
        Bitmap BitmapOrg = BitmapFactory.decodeResource(getResources(), resId);

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();

        // calculate the scale
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return new BitmapDrawable(resizedBitmap);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                mAngle = updateOnTouch(event);
                if (mAngle != INVALID_PROGRESS_VALUE) {
                    mAngle1 = mAngle;
                }
                mVibrator1 = mVibrator2 = 0;
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                mAngle = updateOnTouch(event);
                boolean changed = false;
                if (mAngle != INVALID_PROGRESS_VALUE) {
                    mAngle2 = mAngle;

                    if (isIn1stQuadrant(mAngle1) && isIn4thQuadrant(mAngle2)) {
                        mDiff = -(mAngle1 + (360 - mAngle2));
                    } else if (isIn1stQuadrant(mAngle2) && isIn4thQuadrant(mAngle1)) {
                        mDiff = mAngle2 + (360 - mAngle1);
                    } else {
                        mDiff = mAngle2 - mAngle1;
                    }
                    mAngle1 = mAngle2;

                    // Calculate Total Angle
                    mCurrentAngle += mDiff;
                    if (mCurrentAngle < MIN_ANGLE_REQUIRED) {
                        mCurrentAngle = MIN_ANGLE_REQUIRED;
                    } else if(mCurrentAngle > MAX_ANGLE_REQUIRED) {
                        mCurrentAngle = MAX_ANGLE_REQUIRED;
                    }

                    mVibrator2 = mCurrentAngle / MINUTE_VALUE_TO_DEGREES_STEP_SIZE;
                    if (mVibrator1 != mVibrator2) {
                        // The angle changed
                        changed = true;

                        // Play the device vibrator
                        if (mVibrator != null && mVibrator.hasVibrator()) {
                            mVibrator.vibrate(50);
                        }

                        // Store last state
                        mVibrator1 = mVibrator2;
                    }

                    // Calculate current time in seconds
                    mCurrentTime = calculateCurrentTime();

                    // Update UI
                    invalidate();

                    // Update time to the client
                    // Note: This call MUST be fast to avoid affecting the performance of the
                    // entire component
                    if (mOnDialViewChangeListener != null) {
                        mOnDialViewChangeListener.onProgressChanged(this, mCurrentTime, changed);
                    }
                }
            }
            break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                setPressed(false);
                getParent().requestDisallowInterceptTouchEvent(false);
                // Turn off vibrator
                if (mVibrator != null && mVibrator.hasVibrator()) {
                    mVibrator.cancel();
                }
            }
            break;
        }
        return true;
    }

    private int updateOnTouch(MotionEvent event) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) return INVALID_PROGRESS_VALUE;
        setPressed(true);
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        return getProgressForAngle(mTouchAngle);
    }

    private boolean isIn1stQuadrant(double angle) {
        return angle >= 0 && angle <= 90;
    }

    private boolean isIn4thQuadrant(double angle) {
        return angle >= 270 && angle <= 360;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > 360) ? INVALID_PROGRESS_VALUE
                : touchProgress;

        return touchProgress;
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - centerX;
        float y = yPos - centerY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    public void setTouchInSide() {
        mTouchIgnoreRadius = mRadioKnob / 4;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - centerX;
        float y = yPos - centerY;
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2));

        if (angle < 0) {
            angle = 360 + angle;
        }

        return angle;
    }

    public static class Point {
        float x;
        float y;
    }

    public long getCurrentTime() {
        return mCurrentTime;
    }

    public long getCurrentAngle() {
        return mCurrentAngle;
    }

    public void setCurrentTime(long currentTime) {
        mCurrentTime = currentTime;
        mCurrentAngle = normalizeCurrentAngle(currentTime);
        invalidate();
    }

    private long calculateCurrentTime() {
        return 10 * (normalizeCurrentAngle() + 90);
    }

    private long normalizeCurrentAngle(long currentTime) {
        return currentTime / 10 - 90;
    }

    private Point buildCoordinateXY(float radio, float centerX, float centerY, float
            angleInDegrees) {
        Point point = new Point();
        point.x = centerX + (float)(radio * Math.cos(Math.toRadians(angleInDegrees)));
        point.y = centerY + (float)(radio * Math.sin(Math.toRadians(angleInDegrees)));
        return point;
    }

    public void setOnDialViewChangeListener(OnDialViewChangeListener dialViewChangeListener) {
        mOnDialViewChangeListener = dialViewChangeListener;
    }
}
