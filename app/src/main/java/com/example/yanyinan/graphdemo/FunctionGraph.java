package com.example.yanyinan.graphdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.yanyinan.graphdemo.util.DecimalFactory;
import com.example.yanyinan.graphdemo.util.DisplayUtil;

import java.text.DecimalFormat;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * 创建时间： 2018/11/2
 * 作者：yanyinan
 * 功能描述：函数绘图
 */
public class FunctionGraph extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final String TAG = FunctionGraph.class.getSimpleName();

    private static int DEFAULT_MIN_X_AXIS = -3;
    private static int DEFAULT_MAX_X_AXIS = 3;
    private static int DEFAULT_MIN_Y_AXIS = -5;
    private static int DEFAULT_MAX_Y_AXIS = 5;
    private static final String RENDER_THREAD_NAME = "FunctionGraphRenderThread";

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
    private LinkedBlockingQueue<MotionEvent> mEventQueue = new LinkedBlockingQueue<>(1000);
    //当前控件宽高
    private int mWidth;
    private int mHeight;
    //子线程标志位
    private boolean mIsDrawing;
    //需要显示的函数表达式
    private String computeExpression;
    /**
     * 屏幕显示的x,y轴的最值
     */
    private float mMinXMath = DEFAULT_MIN_X_AXIS, mMaxXMath = DEFAULT_MAX_X_AXIS, mMinYMath = DEFAULT_MIN_Y_AXIS, mMaxYMath = DEFAULT_MAX_Y_AXIS;
    private float mLastTouchX, mLastTouchY;

    public FunctionGraph(Context context) {
        super(context);
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
        mFunctionPaint.setColor(Color.RED);

//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        this.setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        mWidth = getWidth();
        mHeight = getHeight();
        Thread t = new Thread(this);
        t.setName(RENDER_THREAD_NAME);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mIsDrawing = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            startDraw();
            handleTouchEvent();
        }
    }

    private void handleTouchEvent() {
        try {
            MotionEvent event = mEventQueue.take();
            int action = event.getAction();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = mLastTouchX - event.getX();
                    //注意数学和屏幕像素坐标方向相反
                    float y = event.getY() - mLastTouchY;
                    double difXMath = (mMaxXMath - mMinXMath) * x / mWidth;
                    double difYMath = (mMaxYMath - mMinYMath) * y / mHeight;
                    mMinXMath += difXMath;
                    mMaxXMath += difXMath;
                    mMinYMath += difYMath;
                    mMaxYMath += difYMath;
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    break;
            }
        } catch (InterruptedException e) {
            Log.e(TAG + Thread.currentThread() + " mEventQueue take", e.getMessage());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            mEventQueue.put(event);
        } catch (InterruptedException e) {
            Log.e(TAG + Thread.currentThread() + " mEventQueue put", e.getMessage());
        }

        return true;
    }

    private void startDraw() {
        try {
            mCanvas = mHolder.lockCanvas();
            drawGraph(mCanvas);
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
        final double lengthX = (mMaxXMath - mMinXMath);
        //每格表示的数量值
        final double stepLength = Double.parseDouble(DecimalFactory.round(lengthX / 8, 2));
        //负数坐标X轴数量长度
        final double minXInteger = (Math.round(mMinXMath / stepLength)) * stepLength;
        //正数坐标X轴数量长度
        final double maxXInteger = Math.round(mMaxXMath / stepLength) * stepLength;
        //坐标数值文字高
        int fontHeight = (int) (mTextAxisFontMetrics.descent - mTextAxisFontMetrics.ascent);
        //坐标数值文字宽
        int fontWidth = (int) mAxisValuePaint.measureText(" ");
        //遍水平历每个格子
        for (double xMath = minXInteger; xMath < maxXInteger; xMath += stepLength) {
            //y轴所对应的屏幕像素垂直坐标
            int yPixel = mapYMathToPixel(0);
            //数学坐标为i对应的像素水平坐标
            int xPixel = mapXMathToPixel(xMath);
            //文字和坐标轴距离
            int yTextDistanceToAxis = fontHeight;
            if (yPixel < 0) {
                //设定y轴的最小垂直坐标。当图表拖拽到y轴下方的时候，防止y轴往上滑出屏幕
                yPixel = 0;
            } else if (yPixel > mHeight - 20) {
                //设定y轴的最大垂直坐标。当图表拖拽到y轴上方的时候，防止y轴往下滑出屏幕
                yPixel = mHeight;
                //y轴在底部则文字显示于坐标轴上方
                yTextDistanceToAxis = -fontHeight;
            }
            //从屏幕像素纵坐标0画一条经过屏幕像素xValue的直线到最底部（这里是画出垂直的提示轴即网格线）
            canvas.drawLine(xPixel, 0, xPixel, mHeight, mAxisHintPaint);
            //需要标注的坐标值
            String text = mDecimalFormat.format(xMath);
            //写坐标值（在纵坐标为y，即对应的数学纵坐标为0的地方画）
            canvas.drawText(text, xPixel - fontWidth * text.length() / 2, yPixel + yTextDistanceToAxis, mAxisValuePaint);
        }
        //负数坐标y轴数量长度
        final double minYInteger = Math.round(mMinYMath / stepLength) * stepLength;
        //正数坐标y轴数量长度
        final double maxYInteger = Math.round(mMaxYMath / stepLength) * stepLength;
        //遍历垂直方向的格子
        for (double yMath = minYInteger; yMath < maxYInteger; yMath += stepLength) {
            //绘制所在的屏幕像素水平位置坐标
            //数学坐标x为0对应的屏幕像素x坐标（即横坐标所在的像素水平坐标）
            int xPixel = mapXMathToPixel(0);
            //数学坐标为i对应的垂直像素坐标
            int yPixel = mapYMathToPixel(yMath);
            //文字距离纵坐标距离
            int xTextDistanceToAxis = fontWidth;
            if (xPixel < 0) {
                //设定x轴的最小垂直坐标。当图表拖拽到x轴右方的时候，防止x轴往左滑出屏幕
                xPixel = 0;
            } else if (xPixel > mWidth - 20) {
                //设定x轴的最大垂直坐标。当图表拖拽到x轴左方的时候，防止x轴往右滑出屏幕
                xPixel = mWidth;
                xTextDistanceToAxis = -fontWidth;
            }
            //从屏幕像素横坐标0画一条经过屏幕像素y1的直线到最右（这里是画出水平的提示轴即网格线）
            canvas.drawLine(0, yPixel, mWidth, yPixel, mAxisHintPaint);
            String text = mDecimalFormat.format(yMath);
            //写坐标值（在纵坐标为y，即对应的数学横坐标为0的地方画）
            canvas.drawText(text, xPixel + xTextDistanceToAxis, yPixel, mAxisValuePaint);
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
        double yFirstMath = Math.sin(mMinXMath);
        double ySecondMath = yFirstMath;
        //水平遍历每个像素.y1是前一个点的纵坐标，y2是后一个点的纵坐标，j为横坐标
        for (int j = 0; j < mWidth; j++) {
            yFirstMath = ySecondMath;
            //根据像素得到对应的横坐标的值，再得到对应的纵坐标的值
            ySecondMath = Math.sin(mMinXMath + ((double) j + 1) * (mMaxXMath - mMinXMath) / mWidth);
            if (yFirstMath != Double.POSITIVE_INFINITY && ySecondMath != Double.POSITIVE_INFINITY) {
                //这个if意义？

                //如果两个点纵坐标差太大则不画？？
                if (!((yFirstMath > 20) && (ySecondMath < -20)) && !((yFirstMath < -20) && (ySecondMath > 20))) {
                    //前后两个点连起来
                    canvas.drawLine(j, mapYMathToPixel(yFirstMath), j + 1, mapYMathToPixel(ySecondMath), mFunctionPaint);
                }
            }
        }
    }


    /**
     * 将数值x坐标值映射为屏幕像素x坐标
     *
     * @param x
     * @return
     */
    private int mapXMathToPixel(double x) {
        return (int) (mWidth * (x - mMinXMath) / (mMaxXMath - mMinXMath));
    }

    /**
     * 将数值y坐标值映射为屏幕像素y坐标
     *
     * @param y
     * @return
     */
    private int mapYMathToPixel(double y) {
        return (int) (mHeight - mHeight * (y - mMinYMath) / (mMaxYMath - mMinYMath));
    }

}
