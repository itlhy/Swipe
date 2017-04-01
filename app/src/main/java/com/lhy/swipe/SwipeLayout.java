package com.lhy.swipe;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 创 建 人: 路好营
 * 创建日期: 2017/3/30 15:34
 * 添加备注:
 */

public class SwipeLayout extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private ViewGroup mSideContent;
    private ViewGroup mMainContent;
    private int mRange;//条目向左拖动的范围,最长是子控件mSideContent的宽度
    private int mWidth;//父控件宽度
    private int mHeight; //父控件的高度

    public static enum Status {
        Close, Open, Swiping
    }

    private Status status = Status.Close;

    public interface onSwipeListener {
        void onClosed(SwipeLayout swipeLayout);

        void onOpened(SwipeLayout swipeLayout);

        void onStartOpen(SwipeLayout swipeLayout);

        void onStartClose(SwipeLayout swipeLayout);

    }

    private onSwipeListener onSwipeListener;

    public SwipeLayout.onSwipeListener getOnSwipeListener() {
        return onSwipeListener;
    }

    public void setOnSwipeListener(SwipeLayout.onSwipeListener onSwipeListener) {
        this.onSwipeListener = onSwipeListener;
    }

    public SwipeLayout(Context context) {
        this(context, null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        viewDragHelper = ViewDragHelper.create(this, 1.0f, callback);
    }

    /**
     * 1  判断是否拦截触摸事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);//是否拦截触目事件,交给viewDragHelper处理
    }

    /**
     * 2  处理拦截的触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            viewDragHelper.processTouchEvent(event);//由viewDragHelper处理触摸事件
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRange = mSideContent.getMeasuredWidth();//条目向左拖动的范围最长是mSideContent的宽度
        mWidth = getMeasuredWidth();//父控件宽度
        mHeight = getMeasuredHeight(); //父控件的高度
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("至少需要两个子View控件!(Need at least two child View!)");
        }
        if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
            throw new IllegalArgumentException("孩子必须是ViewGroup的子类!(The child must be a subclass of ViewGroup!)");
        }
        mSideContent = (ViewGroup) getChildAt(0);
        mMainContent = (ViewGroup) getChildAt(1);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutContent(false);
    }

    /**
     * 调整布局
     *
     * @param isOpenSideContent
     */
    private void layoutContent(boolean isOpenSideContent) {
        Rect mainRect = computeMainRect(isOpenSideContent);
        mMainContent.layout(mainRect.left, mainRect.top, mainRect.right, mainRect.bottom);
        Rect sideRect = computeSideRect(mainRect);
        mSideContent.layout(sideRect.left, sideRect.top, sideRect.right, sideRect.bottom);
        //把任意布局顺序调整到最上面显示
//        bringChildToFront(mMainContent);//由于没有重叠关系,所以可忽略此行代码
    }

    /**
     * 计算侧边布局的矩形区域
     *
     * @param mainRect
     * @return
     */
    private Rect computeSideRect(Rect mainRect) {
        int left = mainRect.right;
        return new Rect(left, 0, left + mRange, mHeight);
    }

    /**
     * 计算前布局的矩形区域
     *
     * @param isOpenSideContent
     * @return
     */
    private Rect computeMainRect(boolean isOpenSideContent) {
        int left = 0;
        if (isOpenSideContent) {
            left = -mRange;
        }
        return new Rect(left, 0, left + mWidth, mHeight);
    }

    /**
     * 3  重写触摸事件的回调方法
     */
    ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /**
         * 当位置发生变化的时候,把水平方向的偏移量传递给侧边布局,以此绑定两个控件的位置
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mMainContent) {
                mSideContent.offsetLeftAndRight(dx);
            } else if (changedView == mSideContent) {
                mMainContent.offsetLeftAndRight(dx);
            }
            dispatchDragEvent();
            invalidate();//强制生效(兼容低版本)
        }

        /**
         * 松手的时候调用
         * @param releasedChild
         * @param xvel 向右为+ 向左为-
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel == 0) {
                if (mMainContent.getLeft() < -mRange * 0.5f) {
                    openSideContent(true);
                } else {
                    closeSideContent(true);
                }
            } else if (xvel < 0) {
                openSideContent(true);
            } else {
                closeSideContent(true);
            }
        }

        /**
         * 横向拖拽范围
         * @param child
         * @return
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 预设移动的位置的范围
         * @param child
         * @param left 左右滑动的位置
         * @param dx
         * @return 决定了将要移动的位置
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mMainContent) {
                if (left < -mRange) {
                    return -mRange;//限定向左滑动的范围
                } else if (left > 0) {
                    return 0;//限定向右滑动的范围
                }
            } else if (child == mSideContent) {
                if (left < mWidth - mRange) {
                    return mWidth - mRange;
                } else if (left > mWidth) {
                    return mWidth;
                }
            }
            return left;
        }
    };

    /**
     * 分发拖拽事件,更新当前状态
     */
    private void dispatchDragEvent() {
        Status lastStatus = status;
        status = getStatus();
        if (onSwipeListener != null) {
            if (lastStatus != status) {
                if (status == Status.Open) {
                    onSwipeListener.onOpened(this);
                } else if (status == Status.Close) {
                    onSwipeListener.onClosed(this);
                } else if (status == Status.Swiping) {
                    if (lastStatus == Status.Close) {
                        onSwipeListener.onStartOpen(this);
                    } else if (lastStatus == Status.Open) {
                        onSwipeListener.onStartClose(this);
                    }
                }
            }
        }
    }

    /**
     * 获取最新的状态
     *
     * @return
     */
    private Status getStatus() {
        int left = mMainContent.getLeft();
        if (left == 0) {
            return Status.Close;
        } else if (left == -mRange) {
            return Status.Open;
        }
        return Status.Swiping;
    }

    /**
     * 维持动画的继续
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void closeSideContent(boolean isSmooth) {
        if (isSmooth) {
            if (viewDragHelper.smoothSlideViewTo(mMainContent, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(false);
        }

    }

    public void openSideContent(boolean isSmooth) {
        if (isSmooth) {
            if (viewDragHelper.smoothSlideViewTo(mMainContent, -mRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            layoutContent(true);
        }
    }

}
