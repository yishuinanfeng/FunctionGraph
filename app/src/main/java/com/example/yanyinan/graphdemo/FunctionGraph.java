package com.example.yanyinan.graphdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;


/**
 * 创建时间： 2018/11/2
 * 作者：yanyinan
 * 功能描述：函数绘图
 */
public class FunctionGraph extends SurfaceView implements  SurfaceHolder.Callback,Runnable {
    private SurfaceHolder mHolder; // 用于控制SurfaceView
    //用于绘图的canvas
    private Canvas mCanvas;
    //子线程标志位
    private boolean mIsDrawing;

    private ArrayList<EntryPoint> mEntryPoints = new ArrayList<>();

    public FunctionGraph(Context context) {
        super(context);
        init();
    }

    public FunctionGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FunctionGraph(Context context, AttributeSet attrs, ArrayList<EntryPoint> entryPoints) {
        super(context, attrs);
        //避免外部修改影响到内部
        mEntryPoints.addAll(entryPoints);
        init();
    }



    private void init(){
        mHolder = getHolder();
        mHolder.addCallback(this);

//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        this.setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
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
        int width = getWidth();
        int height = getHeight();
        mCanvas.save();
        //将画布原点移动到坐标系原点
        canvas.translate(width/2,height/2);


        mCanvas.restore();

    }

    /**
     * 一个坐标点
     */
    public static class EntryPoint{
        //数学上的坐标值
        private float x;
        private float y;

        public EntryPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }
}
