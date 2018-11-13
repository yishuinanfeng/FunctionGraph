package com.example.yanyinan.graphdemo;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.example.yanyinan.graphdemo.calculate.StringCalculator;
import com.example.yanyinan.graphdemo.util.DecimalFactory;
import com.example.yanyinan.graphdemo.util.DisplayUtil;

import java.text.DecimalFormat;


/**
 * 创建时间： 2018/11/2
 * 作者：yanyinan
 * 功能描述：函数绘图
 */
public class FunctionGraph extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = FunctionGraph.class.getSimpleName();

    private static int DEFAULT_MIN_X_AXIS = -10;
    private static int DEFAULT_MAX_X_AXIS = 10;
    private static int DEFAULT_MIN_Y_AXIS = -10;
    private static int DEFAULT_MAX_Y_AXIS = 10;
    private static int DEFAULT_STEP_COUNT = 10;
    private static final String RENDER_THREAD_NAME = "FunctionGraphRenderThread";
    private static final String X_VARIABLE = "x";

    private SurfaceHolder mHolder; // 用于控制SurfaceView
    //用于绘图的canvas
    private Canvas mCanvas;

    private Paint.FontMetrics mTextAxisFontMetrics;

    private final DecimalFormat mDecimalFormat = new DecimalFormat("#.##");
    //坐标轴数值画笔
    private final TextPaint mAxisValuePaint = new TextPaint();
    //画网格线
    private final TextPaint mAxisHintPaint = new TextPaint();
    //绘制函数画笔
    private final Paint mFunctionPaint = new Paint();
    //绘制坐标轴画笔
    private Paint mAxisPaint = new Paint();
    //绘制线程和主线程间传递MotionEvent，确保线程安全
    //private LinkedBlockingQueue<GraphTouchEvent> mEventQueue = new LinkedBlockingQueue<>(1000);
    //当前控件宽高
    private int mWidth;
    private int mHeight;
    private boolean isTwoFingerMode;

    GestureDetector mGestureDetector;
    //需要显示的函数表达式
    private String mComputeExpression;

    /**
     * 屏幕显示的x,y轴的最值
     */
    private float mMinXMath = DEFAULT_MIN_X_AXIS, mMaxXMath = DEFAULT_MAX_X_AXIS, mMinYMath = DEFAULT_MIN_Y_AXIS, mMaxYMath = DEFAULT_MAX_Y_AXIS;
    private float mScreenHeightWidthRatio;
    private float mLastTouchX, mLastTouchY;
    private float mLastScrollX, mLastScrollY;
    private float mLastFingerDistance;

    private HandlerThread mHandlerThread;
    private Handler mDrawThreadHandler;

    private OverScroller mOverScroller;

    public FunctionGraph(Context context, float screenHeightWidthRatio, String computeExpression) {
        super(context);
        mScreenHeightWidthRatio = screenHeightWidthRatio;
        mComputeExpression = computeExpression;
        init();
    }

    public FunctionGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mAxisPaint.setColor(Color.BLACK);
        mAxisPaint.setStrokeWidth(4f);

        mAxisHintPaint.setAntiAlias(true);
        mAxisHintPaint.setColor(Color.GRAY);
        mAxisValuePaint.setStrokeWidth(2f);

        mAxisValuePaint.setAntiAlias(true);
        mAxisValuePaint.setTypeface(Typeface.MONOSPACE);
        mAxisValuePaint.setColor(Color.BLACK);
        mAxisValuePaint.setTextSize(DisplayUtil.dpTpPx(getContext(), 10));
        mTextAxisFontMetrics = mAxisValuePaint.getFontMetrics();

        mFunctionPaint.setStrokeWidth(DisplayUtil.dpTpPx(getContext(), 2));
        mFunctionPaint.setAntiAlias(true);
        mFunctionPaint.setStyle(Paint.Style.STROKE);
        mFunctionPaint.setColor(Color.RED);

        mComputeExpression = StringCalculator.insetBlanks(mComputeExpression);

        mOverScroller = new OverScroller(getContext());
        mGestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                //滑动过程又有down则停止滑动
                if (!mOverScroller.isFinished()) {
                    mOverScroller.forceFinished(true);
                }
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG + "onFling", "velocityX： " + velocityX + ",velocityY: " + velocityY);

                if (isTwoFingerMode) {
                    return false;
                }
                mOverScroller.fling((int) mLastTouchX, (int) mLastTouchY, (int) velocityX,
                        (int) velocityY, -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE);

                mLastScrollX = mLastTouchX;
                mLastScrollY = mLastTouchY;

                final ValueAnimator mScrollAnimator = ValueAnimator.ofInt(0, 1000);
                mScrollAnimator.setDuration(1000);
                mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Log.d(TAG + "onFling", "onAnimationUpdate");
                        if (!mOverScroller.isFinished()) {
                            handleSlowDown();
                        } else {
                            mScrollAnimator.cancel();
                        }
                    }
                });
                mScrollAnimator.start();
                return false;
            }
        });
    }

    private void handleSlowDown() {
        Log.d(TAG + "onFling", "handleSlowDown");
        mOverScroller.computeScrollOffset();

        float difXPixel = mLastScrollX - mOverScroller.getCurrX();
        float difYPixel = mOverScroller.getCurrY() - mLastScrollY;
        Log.d(TAG + "onFling difXPixel ", difXPixel + "");
        Log.d(TAG + "onFling difYPixel ", difYPixel + "");

        float difXMath = (mMaxXMath - mMinXMath) * difXPixel / mWidth;
        float difYMath = (mMaxYMath - mMinYMath) * difYPixel / mHeight;

        mMaxXMath += difXMath;
        mMaxYMath += difYMath;
        mMinXMath += difXMath;
        mMinYMath += difYMath;
        refreshView();

        mLastScrollX = mOverScroller.getCurrX();
        mLastScrollY = mOverScroller.getCurrY();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mWidth = getWidth();
        mHeight = getHeight();

        mHandlerThread = new DrawHandlerThread(RENDER_THREAD_NAME);
        mHandlerThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawThreadHandler.getLooper().quit();
    }

    /**
     * 通过函数在指定横坐标范围的纵坐标最大最小值确定y轴的长度
     */
    private void setMaximumValueForYAxis() {
        //最小横坐标对应的纵坐标的值
        String firstXInput = mComputeExpression.replace(X_VARIABLE, String.valueOf(mMinXMath));
        float yFirstMath = StringCalculator.evaluateExpression(firstXInput);
        mMaxYMath = yFirstMath;
        mMinYMath = yFirstMath;

        //为了加速绘制，每两个像素点进行遍历
        for (int j = 0; j < mWidth; j = j + 2) {

            float xMath = mMinXMath + ((float) j + 1) * (mMaxXMath - mMinXMath) / mWidth;
            String input = mComputeExpression.replace(X_VARIABLE, String.valueOf(xMath));

            yFirstMath = StringCalculator.evaluateExpression(input);
            if (mMaxYMath < yFirstMath) {
                mMaxYMath = yFirstMath;
            } else if (mMinYMath > yFirstMath) {
                mMinYMath = yFirstMath;
            }
        }

        float maxAbs = (float) (Math.abs(mMaxYMath) > Math.abs(mMinYMath) ? Math.abs(mMaxYMath) * 1.5 : Math.abs(mMinYMath) * 1.5);
        mMaxYMath = maxAbs;
        mMinYMath = -maxAbs;
        mMaxXMath = mMaxYMath / mScreenHeightWidthRatio;
        mMinXMath = mMinYMath / mScreenHeightWidthRatio;

        Log.d(TAG + "mMaxYMath: ", mMaxYMath + "");
        Log.d(TAG + "mMinYMath: ", mMinYMath + "");
        Log.d(TAG + "mMaxXMath: ", mMaxXMath + "");
        Log.d(TAG + "mMinXMath: ", mMinXMath + "");
    }

    private void handleTouchEvent(MotionEvent motionEvent) {
        mGestureDetector.onTouchEvent(motionEvent);
        int action = motionEvent.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG + "handleTouchEvent ACTION_DOWN: ", motionEvent.getPointerCount() + "");
                mLastTouchX = motionEvent.getX();
                mLastTouchY = motionEvent.getY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG + "handleTouchEvent ACTION_POINTER_DOWN: ", motionEvent.getPointerCount() + "");
                if (motionEvent.getPointerCount() == 2) {
                    isTwoFingerMode = true;
                    mLastFingerDistance = getDistanceBetweenFingers(motionEvent);
                    break;
                }
            case MotionEvent.ACTION_MOVE:

                switch (motionEvent.getPointerCount()) {
                    case 1:
                        if (isTwoFingerMode) {
                            return;
                        }
                        float difXPixel = mLastTouchX - motionEvent.getX();
                        //注意数学和屏幕像素坐标方向相反
                        float difYPixel = motionEvent.getY() - mLastTouchY;

                        Log.d(TAG + "handleTouchEvent difXPixel: ", difXPixel + "");
                        Log.d(TAG + "handleTouchEvent difYPixel: ", difYPixel + "");

                        float difXMath = (mMaxXMath - mMinXMath) * difXPixel / mWidth;
                        float difYMath = (mMaxYMath - mMinYMath) * difYPixel / mHeight;
                        mMinXMath += difXMath;
                        mMaxXMath += difXMath;
                        mMinYMath += difYMath;
                        mMaxYMath += difYMath;
                        mLastTouchX = motionEvent.getX();
                        mLastTouchY = motionEvent.getY();
                        refreshView();
                        break;
                    case 2:
                        float fingersDistance = getDistanceBetweenFingers(motionEvent);
                        //缩放倍数
                        Log.d(TAG + "handleTouchEvent mLastFingerDistance: ", mLastFingerDistance + "");
                        //有时候mLastFingerDistance会等于0，原因暂时未知
                        if (mLastFingerDistance == 0) {
                            return;
                        }
                        float scaleDelta = (mLastFingerDistance - fingersDistance) / fingersDistance;
//
                        Log.d(TAG + "handleTouchEvent scaleDelta: ", scaleDelta + "");
                        //左右最大值同时加上缩放增加的值，中心点在控件像素中心点
                        float mathWidth = mMaxXMath - mMinXMath;
                        float mathHeight = mMaxYMath - mMinYMath;
                        mMaxXMath += mathWidth * scaleDelta / 2;
                        mMaxYMath += mathHeight * scaleDelta / 2;
                        mMinXMath -= mathWidth * scaleDelta / 2;
                        mMinYMath -= mathHeight * scaleDelta / 2;

                        Log.d(TAG + "handleTouchEvent mMaxXMath: ", mMaxXMath + "");
                        Log.d(TAG + "handleTouchEvent mMinXMath: ", mMinXMath + "");

                        //左右最大值同时缩放倍数，中心点在数学坐标原点
//                            mMaxXMath *= scale;
//                            mMaxYMath *= scale;
//                            mMinXMath *= scale;
//                            mMinYMath *= scale;
                        mLastFingerDistance = fingersDistance;
                        refreshView();
                        break;
                }

            case MotionEvent.ACTION_POINTER_UP:
                //为啥滑动的时候也会多次进入？？
                Log.d(TAG + "handleTouchEvent: ", "ACTION_POINTER_UP");
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastTouchX = motionEvent.getX();
                mLastTouchY = motionEvent.getY();
                mLastFingerDistance = 0;
                isTwoFingerMode = false;
                break;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        mDrawThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                handleTouchEvent(event);
            }
        });
        return true;
    }

    private void refreshView() {
        try {
            mCanvas = mHolder.lockCanvas();
            drawGraph(mCanvas);
            Log.d(TAG + "refreshView Thread： ", Thread.currentThread().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    private void drawGraph(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        //   canvas.save();
        //将画布原点移动到坐标系原点
//        canvas.translate(mWidth / 2, mHeight / 2);
        drawAxis(canvas);
        drawFunction(canvas);

        //   canvas.restore();
    }

    /**
     * 画坐标轴（数量长度指的是针对数学式子，屏幕像素长度是针对安卓View的坐标体系）
     *
     * @param canvas
     */
    private void drawAxis(Canvas canvas) {
        //需要画的x轴坐标数量长度
        final float lengthX = (mMaxXMath - mMinXMath);
        //每格表示的数量值
        final float stepLength = Float.parseFloat(DecimalFactory.round(lengthX / DEFAULT_STEP_COUNT, 2));
        //负数坐标X轴数量长度
        final float minXInteger = (Math.round(mMinXMath / stepLength)) * stepLength;
        //正数坐标X轴数量长度
        final float maxXInteger = Math.round(mMaxXMath / stepLength) * stepLength;
        //坐标数值文字高
        int fontHeight = (int) (mTextAxisFontMetrics.descent - mTextAxisFontMetrics.ascent);
        //坐标数值文字宽
        int fontWidth = (int) mAxisValuePaint.measureText(" ");
        //遍水平历每个格子，画垂直线和横坐标坐标数值
        for (float xMath = minXInteger; xMath < maxXInteger; xMath += stepLength) {
            //y轴所对应的屏幕像素垂直坐标
            int yAxisPixel = mapYMathToPixel(0);
            //数学坐标为i对应的像素水平坐标
            int xPixel = mapXMathToPixel(xMath);
            //文字和坐标轴距离
            int yTextDistanceToAxis = fontHeight;
            if (yAxisPixel < 0) {
                //设定x轴的最小垂直坐标。当图表拖拽到x轴下方的时候，防止x轴往上滑出屏幕
                yAxisPixel = 0;
            } else if (yAxisPixel > mHeight - 20) {
                //设定x轴的最大垂直坐标。当图表拖拽到x轴上方的时候，防止x轴往下滑出屏幕
                yAxisPixel = mHeight;
                //y轴在底部则文字显示于坐标轴上方
                yTextDistanceToAxis = -fontHeight;
            }
            //屏幕像素横坐标xPixel的直线（这里是画出垂直的提示轴即网格线）
            canvas.drawLine(xPixel, 0, xPixel, mHeight, mAxisHintPaint);
            //需要标注的坐标值
            String text = mDecimalFormat.format(xMath);
            //写坐标值（在横坐标轴上）
            canvas.drawText(text, xPixel - fontWidth * text.length() / 2, yAxisPixel + yTextDistanceToAxis, mAxisValuePaint);
        }

        //负数坐标y轴数量长度
        final float minYInteger = Math.round(mMinYMath / stepLength) * stepLength;
        //正数坐标y轴数量长度
        final float maxYInteger = Math.round(mMaxYMath / stepLength) * stepLength;
        //遍历垂直方向的格子，画水平线和纵坐标坐标数值
        for (float yMath = minYInteger; yMath < maxYInteger; yMath += stepLength) {
            //绘制所在的屏幕像素水平位置坐标
            //数学坐标x为0对应的屏幕像素x坐标（即横坐标所在的像素水平坐标）
            int xAxisPixel = mapXMathToPixel(0);
            //数学坐标为i对应的垂直像素坐标
            int yPixel = mapYMathToPixel(yMath);
            //文字距离纵坐标距离
            int xTextDistanceToAxis = fontWidth;
            String text = mDecimalFormat.format(yMath);
            if (xAxisPixel < 0) {
                //设定y轴的最小垂直坐标。当图表拖拽到y轴右方的时候，防止y轴往左滑出屏幕
                xAxisPixel = 0;
            } else if (xAxisPixel > mWidth - 20) {
                //设定x轴的最大垂直坐标。当图表拖拽到x轴左方的时候，防止y轴往右滑出屏幕
                xAxisPixel = mWidth;
                xTextDistanceToAxis = -fontWidth * (text.length() + 1);
            }
            //纵坐标yPixel的水平直线（这里是画出水平的提示轴即网格线）
            canvas.drawLine(0, yPixel, mWidth, yPixel, mAxisHintPaint);
            //写坐标值（在纵坐标轴上）
            canvas.drawText(text, xAxisPixel + xTextDistanceToAxis, yPixel, mAxisValuePaint);
        }

        int xAxisPixel = mapXMathToPixel(0);
        int yAxisPixel = mapYMathToPixel(0);
        //画横纵坐标轴
        canvas.drawLine(xAxisPixel, 0, xAxisPixel, mHeight, mAxisPaint);
        canvas.drawLine(0, yAxisPixel, mWidth, yAxisPixel, mAxisPaint);
    }

    /**
     * 算出水平每个像素点对应的函数的点，然后连起来
     *
     * @param canvas
     */
    private void drawFunction(Canvas canvas) {
        //最小横坐标对应的纵坐标的值
        String firstXInput = mComputeExpression.replace("x", String.valueOf(mMinXMath));
        float yFirstMath = StringCalculator.evaluateExpression(firstXInput);
        float ySecondMath = yFirstMath;

        //水平遍历每个像素.y1是前一个点的纵坐标，y2是后一个点的纵坐标，j为横坐标
        long a = System.nanoTime();
        //为了加速绘制，每两个像素点进行遍历

        Path path = new Path();
        path.moveTo((float) 0, (float) mapYMathToPixel(yFirstMath));

        for (int j = 0; j < mWidth; j = j + 5) {

            yFirstMath = ySecondMath;
            //根据像素得到对应的横坐标的值，再得到对应的纵坐标的值
            //  ySecondMath = Math.sin(mMinXMath + ((double) j + 1) * (mMaxXMath - mMinXMath) / mWidth);

            float xMath = mMinXMath + ((float) j + 1) * (mMaxXMath - mMinXMath) / mWidth;
            String input = mComputeExpression.replace(X_VARIABLE, String.valueOf(xMath));

            Log.d(TAG + "calculate input: ", input);

            long a2 = System.nanoTime();
            ySecondMath = StringCalculator.evaluateExpression(input);

            Log.d("calculate Expression", System.nanoTime() - a2 + "");

            if (yFirstMath != Float.POSITIVE_INFINITY && ySecondMath != Float.POSITIVE_INFINITY) {
                //如果两个点纵坐标差太大则不画？？
                if (!((yFirstMath > 20) && (ySecondMath < -20)) && !((yFirstMath < -20) && (ySecondMath > 20))) {
                    //前后两个点连起来
                    //canvas.drawLine(j, mapYMathToPixel(yFirstMath), j + 1, mapYMathToPixel(ySecondMath), mFunctionPaint);
                    path.lineTo((float) (j + 1), (float) mapYMathToPixel(ySecondMath));
                }
            }
        }

        canvas.drawPath(path, mFunctionPaint);

        Log.d(TAG + " calculate whole time", System.nanoTime() - a + "");
    }

    /**
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return 两个手指之间的距离
     */
    private float getDistanceBetweenFingers(MotionEvent event) {
        float disX = Math.abs(event.getX(0) - event.getX(1));
        float disY = Math.abs(event.getY(0) - event.getY(1));
        return (float) Math.sqrt(disX * disX + disY * disY);
    }


    /**
     * 将数值x坐标值映射为屏幕像素x坐标
     *
     * @param x
     * @return
     */
    private int mapXMathToPixel(float x) {
        return (int) (mWidth * (x - mMinXMath) / (mMaxXMath - mMinXMath));
    }

    /**
     * 将数值y坐标值映射为屏幕像素y坐标
     *
     * @param y
     * @return
     */
    private int mapYMathToPixel(float y) {
        return (int) (mHeight - mHeight * (y - mMinYMath) / (mMaxYMath - mMinYMath));
    }

    class DrawHandlerThread extends HandlerThread {

        DrawHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mDrawThreadHandler = new Handler(mHandlerThread.getLooper());
            setMaximumValueForYAxis();
            refreshView();
        }
    }
}
