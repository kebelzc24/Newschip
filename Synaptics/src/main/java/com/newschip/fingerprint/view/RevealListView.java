package com.newschip.fingerprint.view;

/**
 * @fileName RevealLayout.java
 * @description 实现Android L点击水波纹效果
 * @author kebelzc24
 * @version 1.0
 */

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

public class RevealListView extends ListView implements Runnable {

    private final int WAIT_REVEAL_END = 280;
    private static final int INVALIDATE_DURATION = 20;// 每次刷新的时间间隔
    private int mRevealRadiusGap;// 扩散半径增量
    private int mTargetWidth;// 控件宽度和高度
    private int mTargetHeight;
    private int mMaxRadio;// 扩散的最大半径
    private int mRevealRadio;
    private int mMinWidthHeight;
    private int mPointLocationX;
    private int mPointLocationY;
    private float mCenterX, mCenterY;

    int[] mParentLocation = new int[2];

    private boolean mIsPressed;// 记录是否按钮被按下
    private boolean mShouldDoAnimation = true;

    private Paint mPaint;
    private View mTouchTarget;

    private DispatchUpTouchEventRunnable mDispatchUpTouchEventRunnable;

    public RevealListView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public RevealListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public RevealListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0x1b000000);
        mDispatchUpTouchEventRunnable = new DispatchUpTouchEventRunnable();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        this.getLocationOnScreen(mParentLocation);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.dispatchDraw(canvas);
        if (!mShouldDoAnimation || mTargetWidth <= 0 || mTouchTarget == null) {
            return;
        }
//        if (mRevealRadio > mMinWidthHeight / 2) {
//            mRevealRadio += mRevealRadiusGap * 4;
//        } else {
//            mRevealRadio += mRevealRadiusGap;
//        }
        mRevealRadio += mRevealRadiusGap * 3;
        this.getLocationOnScreen(mParentLocation);
        int[] location = new int[2];
        mTouchTarget.getLocationOnScreen(location);
        int left = location[0] - mParentLocation[0];
        int top = location[1] - mParentLocation[1];
        int right = left + mTouchTarget.getMeasuredWidth();
        int bottom = top + mTouchTarget.getMeasuredHeight();
        canvas.save();
        canvas.clipRect(left, top, right, bottom);
        canvas.drawCircle(mCenterX, mCenterY, mRevealRadio, mPaint);
        canvas.restore();
        if (mRevealRadio <= mMaxRadio) {
            postInvalidateDelayed(INVALIDATE_DURATION, left, top, right, bottom);
        } else if (!mIsPressed) {
            mShouldDoAnimation = false;
            postInvalidateDelayed(INVALIDATE_DURATION, left, top, right, bottom);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        int x = (int) ev.getRawX();
        int y = (int) ev.getRawY();
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mTouchTarget = getTouchTarget(x, y);
            if (mTouchTarget != null) {
                initParametersForChild(ev, mTouchTarget);
                postInvalidateDelayed(INVALIDATE_DURATION);
            }

        } else if (action == MotionEvent.ACTION_UP) {
            mIsPressed = false;
            postInvalidateDelayed(INVALIDATE_DURATION);
            mDispatchUpTouchEventRunnable.event = ev;
            postDelayed(mDispatchUpTouchEventRunnable, WAIT_REVEAL_END);
            return true;

        } else if (action == MotionEvent.ACTION_CANCEL) {
            mIsPressed = false;
            postInvalidateDelayed(INVALIDATE_DURATION);
        }
        return super.dispatchTouchEvent(ev);

    }

    private View getTouchTarget(int x, int y) {
        ArrayList<View> views = getTouchables();
        for (View view : views) {
            if (isTouchPointInView(view, x, y)) {
                return view;
            }
        }
        return null;
    }

    private boolean isTouchPointInView(View view, int x, int y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (view.isClickable()) {
            if ((x >= left && x <= right) && (y >= top && y <= bottom)) {
                return true;
            }
        }
        return false;
    }

    private void initParametersForChild(MotionEvent event, View target) {

        mCenterX = event.getX();
        mCenterY = event.getY();
        mTargetHeight = target.getMeasuredHeight();
        mTargetWidth = target.getMeasuredWidth();
        mMinWidthHeight = Math.min(mTargetHeight, mTargetWidth);
        mIsPressed = true;
        mShouldDoAnimation = true;
        mRevealRadio = 0;
        mRevealRadiusGap = mMinWidthHeight / 4;
        int[] location = new int[2];
        target.getLocationOnScreen(location);
        int left = location[0] - mParentLocation[0];
        int transformedCenterX = (int) mPointLocationX - left;
        int top = location[1] - mParentLocation[1];
        int transformedCenterY = (int) mPointLocationY - top;
        mMaxRadio = Math
                .max(Math.max(left, mTargetWidth - transformedCenterX),
                        Math.max(transformedCenterY, mTargetHeight
                                - transformedCenterY));
    }

    @Override
    public void run() {
        super.performClick();
    }

    @Override
    public boolean performClick() {
        // TODO Auto-generated method stub
        postDelayed(this, INVALIDATE_DURATION);
        return true;
    }

    private class DispatchUpTouchEventRunnable implements Runnable {

        public MotionEvent event;

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mTouchTarget == null || !mTouchTarget.isClickable()) {
                return;
            }
            if (isTouchPointInView(mTouchTarget, (int) event.getRawX(),
                    (int) event.getRawY())) {
                mTouchTarget.dispatchTouchEvent(event);
            }
        }

    }

}
