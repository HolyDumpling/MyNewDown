package com.hy.library_download.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.hy.library_download.DownloadConfig;
import com.hy.library_download.R;

import java.util.ArrayList;

public class MagicCircleView extends View {
    AnimCallback animCallback = null;
    Paint mLinePaint;


    private int mLineColor; //线条颜色
    int lineNum;           //线条数量
    int strokeWidth;        //画笔宽度
    int ringSpacing;        //画笔间隙

    //控件尺寸
    int mWidth;
    int mHeight;
    //画布中心
    int[] drawCenter;
    //内圆半径
    int innerCircleRadius;
    //大圆尺寸
    int bigRingXY[][];
    //小圆尺寸
    int smallRingXY[][];

    //全部下载进度
    private double pos;

    Bitmap iconError;//错误
    Bitmap iconCompleter;
    Bitmap iconStop;
    Bitmap iconWait;

    int status = 0;


    Rect srcRect ;
    Rect dstRect ;



    public MagicCircleView(Context context) {
        this(context, null);
    }

    public MagicCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MagicCircleView);
        mLineColor = typedArray.getColor(R.styleable.MagicCircleView_lineColor, Color.parseColor("#e03070"));
        lineNum = typedArray.getInt(R.styleable.MagicCircleView_lineNum, 5);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.MagicCircleView_strokeWidth, 6);
        ringSpacing = typedArray.getDimensionPixelSize(R.styleable.MagicCircleView_ringSpacing, 6);

        int iconErrorId = typedArray.getResourceId(R.styleable.MagicCircleView_imgError,R.drawable.icon_error);
        iconError = BitmapFactory.decodeResource(getResources(), iconErrorId);
        int iconCompleterId = typedArray.getResourceId(R.styleable.MagicCircleView_imgCompleter,R.drawable.icon_completer);
        iconCompleter = BitmapFactory.decodeResource(getResources(), iconCompleterId);
        int iconStopId = typedArray.getResourceId(R.styleable.MagicCircleView_imgStop,R.drawable.icon_stop);
        iconStop = BitmapFactory.decodeResource(getResources(), iconStopId);
        int iconWaitId = typedArray.getResourceId(R.styleable.MagicCircleView_imgWait,R.drawable.icon_wait);
        iconWait = BitmapFactory.decodeResource(getResources(), iconWaitId);

        srcRect = new Rect(0,0,iconError.getWidth(),iconError.getHeight());

        typedArray.recycle();
        init();
    }

    private static class MyPoint{
        double progress;
        int x;
        int y;
        MyPoint(){ }
        MyPoint(int x,int y){
            this.x = x;
            this.y = y;
        }
        void setXY(int[] xy) {
            if(xy==null)
                return;
            this.x = xy[0];
            this.y = xy[1];
        }
    }

    public void setmLineColor(int lineColor) {
        mLinePaint.setColor(lineColor);
        invalidate();
    }

    public void setStatus(int status){
        this.status = status;
        invalidate();
    }

    ArrayList<MyPoint> pointList ;
    private void init(){
        pointList = new ArrayList<>();
        for(int i=0;i<lineNum;i++)
            pointList.add(new MyPoint());

        bigRingXY = new int[2][2];
        smallRingXY = new int[2][2];
        drawCenter = new int[2];

        mLinePaint = new Paint();
        //设置成填充
        mLinePaint.setColor(mLineColor);
        mLinePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(strokeWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);
    }

    public void setAnimCallback(AnimCallback animCallback) {
        this.animCallback = animCallback;
    }

    public interface AnimCallback {
        public abstract void callback();
    }

    public void setProgressRate(double[][] progress) {
        double total = 0;
        if(progress==null)
            return;
        for(int i=0;i<lineNum;i++){
            if(i < progress.length){
                pointList.get(i).progress = progress[i][1] * 100;
                total += progress[i][1];
            } else {
                pointList.get(i).progress = progress[0][1] * 100;
            }
        }
        pos =total * 100/progress.length;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY)
            mWidth = widthSize;
        if (heightMode == MeasureSpec.EXACTLY)
            mHeight = heightSize;

        drawCenter[0] = mWidth/2;
        drawCenter[1] = mHeight/2;

        int strokeWidthHalf = strokeWidth/2;

        bigRingXY[0][0] = ringSpacing + strokeWidthHalf;
        bigRingXY[0][1] = ringSpacing + strokeWidthHalf;
        bigRingXY[1][0] = mWidth - ringSpacing - strokeWidthHalf;
        bigRingXY[1][1] = mHeight - ringSpacing - strokeWidthHalf;

        smallRingXY[0][0] = bigRingXY[0][0] + ringSpacing + strokeWidth;
        smallRingXY[0][1] = bigRingXY[0][1] + ringSpacing + strokeWidth;
        smallRingXY[1][0] = bigRingXY[1][0] - ringSpacing - strokeWidth;
        smallRingXY[1][1] = bigRingXY[1][1] - ringSpacing - strokeWidth;

        innerCircleRadius = (smallRingXY[1][0] - strokeWidth - smallRingXY[0][0] )/2;

        int angle = 360 /lineNum;
        for(int i=0;i<lineNum;i++)
            pointList.get(i).setXY(computingCirclePoint(angle*i));

        if(dstRect==null)
            dstRect = new Rect(drawCenter[0] - innerCircleRadius,drawCenter[1] - innerCircleRadius,drawCenter[0] + innerCircleRadius,drawCenter[1] + innerCircleRadius);
        else{
            dstRect.left = drawCenter[0] - innerCircleRadius;
            dstRect.top = drawCenter[1] - innerCircleRadius;
            dstRect.right = drawCenter[0] + innerCircleRadius;
            dstRect.bottom = drawCenter[1] + innerCircleRadius;
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    private int[] computingCirclePoint(int angle){
        //将角度转换成弧度值
        double x = Math.toRadians(angle);
        int[] point = new int[2];
        if(drawCenter==null)
            return null;
        point[0] = (int) (drawCenter[0] + innerCircleRadius * Math.cos(x));
        point[1] = (int) (drawCenter[1] + innerCircleRadius * Math.sin(x));
        return point;
    }

    private double computingLinePoint(int start,int end,double pos){
        return start + ((end - start) * pos / 100);
    }


    private void drawMagic(Canvas canvas) {
        float angle =(float) (360 * pos / 100);
        canvas.drawArc(new RectF(bigRingXY[0][0], bigRingXY[0][1], bigRingXY[1][0], bigRingXY[1][1]), 270, -angle, false, mLinePaint);
        canvas.drawArc(new RectF(smallRingXY[0][0], smallRingXY[0][1], smallRingXY[1][0], smallRingXY[1][1]), 270, angle, false, mLinePaint);
        for(int i=0;i<lineNum;i++){
            MyPoint pointA = pointList.get(i);
            MyPoint pointB;
            if(i+2<lineNum)
                pointB = pointList.get(i+2);
            else
                pointB = pointList.get(i+2-lineNum);
            canvas.drawLine(pointA.x,pointA.y,
                    (int)computingLinePoint(pointA.x,pointB.x,pointA.progress),
                    (int)computingLinePoint(pointA.y,pointB.y,pointA.progress),
                    mLinePaint);
        }
    }
    private void drawImage(Canvas canvas, Bitmap bitmap){
        canvas.drawBitmap(bitmap,srcRect,dstRect,mLinePaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (status){
            case DownloadConfig.RUN_FLAG:
                drawMagic(canvas);
                break;
            case DownloadConfig.WAIT_FLAG:
                drawImage(canvas,iconWait);
                break;
            case DownloadConfig.STOP_FLAG:
                drawImage(canvas,iconStop);
                break;
            case DownloadConfig.COMPLETER_FLAG:
                drawImage(canvas,iconCompleter);
                break;
            case DownloadConfig.ERROR_FLAG:
                drawImage(canvas,iconError);
                break;
        }
    }

}
