package com.example.ygl.viewtest;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/*
 *创建人:yanggl
 *创建时间:2018-6-11  20:19
 *类描述:
 *备注:
 */
public class SlenderProgressBar extends View {

    private Paint p;
    private Canvas c;

    private ValueAnimator mProgressAnimation;
    private float mToProgress;
    private float mProgress;

    public SlenderProgressBar(Context context) {
        this(context,null);
    }

    public SlenderProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    //至少调用super(context, attrs)不然报错
    public SlenderProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化画笔
        p=new Paint();
        p.setColor(Color.GREEN);
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
        p.setXfermode(porterDuffXfermode);

        mProgress=0;
        mToProgress=0;
        setupAnimations();
    }

    //在每次载入视图时执行一次
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        c=canvas;
        //画矩形
        float length =(mProgress/100)*getMeasuredWidth();
        //定义一个矩形
        RectF rectF=new RectF(0,0,length,getMeasuredHeight());
        c.drawRect(rectF,p);

//        drawRect(mProgress);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void drawRect(float ratio){
        //根据输入比例算出需要画的长度
        Log.i("drawRect","drawRect:"+ratio/100);
//        mProgress=ratio;
//        invalidate();
        mToProgress = ratio;
        if (mProgressAnimation.isRunning()) {
            mProgressAnimation.resume();
            mProgressAnimation.start();
        } else {
            mProgressAnimation.start();
        }
    }

    //设置动画
    private void setupAnimations() {
        //ProgressBar的动画
        mProgressAnimation = ValueAnimator.ofFloat(0, 1).setDuration(500);
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float timePercent = (float) animation.getAnimatedValue();
                mProgress = ((mToProgress - mProgress) * timePercent + mProgress);
                //更新视图
                invalidate();
            }
        });
    }
}
