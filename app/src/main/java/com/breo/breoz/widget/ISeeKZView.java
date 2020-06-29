package com.breo.breoz.widget;

import android.app.Service;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.breo.breoz.R;

public class ISeeKZView extends View {
    private final String TAG = "ISeeKZView";
    private Context context;

    private Paint mPaint;
    private PointF mFirstPoint;
    private PointF mSecondPoint;
    private boolean isFirstActive = false;
    private boolean isSecondActive = false;

    private int centerLeftX = 0;
    private int centerLeftY = 0;
    private int centerRightX = 0;
    private int centerRightY = 0;

    private int w;
    private int h;

    private int last1Index = -1;
    private int last2Index = -1;

    private Vibrator vibrator;
    private int vibratorTime = 34;

    public ISeeKZView(Context context) {
        super(context);
        init(context);
    }

    public ISeeKZView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ISeeKZView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ISeeKZView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context c) {
        this.context = c;
        mFirstPoint = new PointF(0, 0);
        mSecondPoint = new PointF(0, 0);
        isFirstActive = false;
        isSecondActive = false;
        vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(context.getResources().getColor(R.color.breo_yellow));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerLeftX = w / 4;
        centerLeftY = h / 2;
        centerRightX = w / 4 * 3;
        centerRightY = h / 2;
        this.w = w;
        this.h = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFirstPoint.x != 0 && mFirstPoint.y != 0) {
            canvas.drawCircle(mFirstPoint.x, mFirstPoint.y, 50, mPaint);
        }
        if (mSecondPoint.x != 0 && mSecondPoint.y != 0) {
            canvas.drawCircle(mSecondPoint.x, mSecondPoint.y, 50, mPaint);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        Log.d(TAG, "ACTION_MOVE---index------>" + index);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerId(index) == 0) {
                    isFirstActive = true;
                    mFirstPoint.set(event.getX(), event.getY());
                }
                if (event.getPointerId(index) == 1) {
                    isSecondActive = true;
                    mSecondPoint.set(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(index) == 0) {
                    isFirstActive = false;
                    mFirstPoint.set(0, 0);
                    last1Index = -1;
                }
                if (event.getPointerId(index) == 1) {
                    isSecondActive = false;
                    mSecondPoint.set(0, 0);
                    last2Index = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isFirstActive) {
                    int pointerIndex = event.findPointerIndex(0);
                    mFirstPoint.set(event.getX(pointerIndex), event.getY(pointerIndex));
                    checkFirstTouch(event.getX(pointerIndex), event.getY(pointerIndex));
                }
                if (isSecondActive) {
                    try {
                        int pointerIndex = event.findPointerIndex(1);
                        mSecondPoint.set(event.getX(pointerIndex), event.getY(pointerIndex));
                        checkSecondTouch(event.getX(pointerIndex), event.getY(pointerIndex));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                break;
        }
        invalidate();
        return true;
    }

    /**
     * 算出该点与水平的角度的值，用移动点角度减去起始点角度就是旋转角度。
     *
     * @param x
     * @param y
     */
    private void checkFirstTouch(float x, float y) {
        double angle = 0;
        boolean isLeft = false;
        int centerX = w / 2;
        if (x < centerX) {
            isLeft = true;
            angle = getLeftAngle(x, y);
        } else {
            angle = getRightAngle(x, y);
        }
        int index = (int) (angle / 45);
        if (isLeft) {
            switch (index) {
                case 0:
                    index = 2;
                    break;
                case 1:
                    index = 3;
                    break;
                case 2:
                    index = 11;
                    break;
                case 3:
                    index = 10;

                    break;
                case 4:
                    index = 9;

                    break;
                case 5:
                    index = 8;

                    break;
                case 6:
                    index = 0;

                    break;
                case 7:
                    index = 1;
                    break;
            }
        } else {
            switch (index) {
                case 0:
                    index = 6;
                    break;
                case 1:
                    index = 7;
                    break;
                case 2:
                    index = 15;
                    break;
                case 3:
                    index = 14;

                    break;
                case 4:
                    index = 13;

                    break;
                case 5:
                    index = 12;

                    break;
                case 6:
                    index = 4;

                    break;
                case 7:
                    index = 5;
                    break;
            }
        }

        if (isFirstActive && index != last1Index) {
            last1Index = index;
            if (index != last2Index) {
                vibrator.cancel();
                vibrator.vibrate(vibratorTime);
                Log.d(TAG, "index1 = " + index + ",last2Index =" + last2Index);
            }
        }
    }

    private void checkSecondTouch(float x, float y) {
        double angle = 0;
        boolean isLeft = false;
        int centerX = w / 2;
        if (x < centerX) {
            isLeft = true;
            angle = getLeftAngle(x, y);
        } else {
            angle = getRightAngle(x, y);
        }
        int index = (int) (angle / 45);
        if (isLeft) {
            switch (index) {
                case 0:
                    index = 2;
                    break;
                case 1:
                    index = 3;
                    break;
                case 2:
                    index = 11;
                    break;
                case 3:
                    index = 10;

                    break;
                case 4:
                    index = 9;

                    break;
                case 5:
                    index = 8;

                    break;
                case 6:
                    index = 0;

                    break;
                case 7:
                    index = 1;
                    break;
            }
        } else {
            switch (index) {
                case 0:
                    index = 6;
                    break;
                case 1:
                    index = 7;
                    break;
                case 2:
                    index = 15;
                    break;
                case 3:
                    index = 14;

                    break;
                case 4:
                    index = 13;

                    break;
                case 5:
                    index = 12;

                    break;
                case 6:
                    index = 4;

                    break;
                case 7:
                    index = 5;
                    break;
            }
        }

        if (isSecondActive && index != last2Index) {
            last2Index = index;
            if (index != last1Index) {
                vibrator.cancel();
                vibrator.vibrate(vibratorTime);
                Log.d(TAG, "index2 = " + index + ",last1Index =" + last1Index);
            }
        }
    }

    private double getLeftAngle(double xTouch, double yTouch) {
        double x = xTouch - centerLeftX;
        double y = yTouch - centerLeftY;
        int quadrant = getQuadrant(xTouch, yTouch, centerLeftX, centerLeftY);
        double angle = (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
        switch (quadrant) {
            case 1:
                angle = 90 + angle;
                break;
            case 2:
                angle = 270 + Math.abs(angle);
                break;
            case 3:
                angle = 270 - angle;
                break;
            case 4:
                angle = angle + 90;
                break;
        }
        return angle;
    }

    private double getRightAngle(double xTouch, double yTouch) {
        double x = xTouch - centerRightX;
        double y = yTouch - centerRightY;
        int quadrant = getQuadrant(xTouch, yTouch, centerRightX, centerRightY);
        double angle = (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
        switch (quadrant) {
            case 1:
                angle = 90 + angle;
                break;
            case 2:
                angle = 270 + Math.abs(angle);
                break;
            case 3:
                angle = 270 - angle;
                break;
            case 4:
                angle = angle + 90;
                break;
        }
        return angle;
    }

    private int getQuadrant(double x, double y, float centerX, float centerY) {
        int tmpX = (int) (x - centerX);
        int tmpY = (int) (y - centerY);
        if (tmpX >= 0) {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }
    }
}
