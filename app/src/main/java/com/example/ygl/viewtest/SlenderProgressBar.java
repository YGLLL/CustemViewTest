package com.example.ygl.viewtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
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
    private float ratio;

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

        ratio=0;
    }

    //在每次载入视图时执行一次
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        c=canvas;
        //画矩形
        drawRect(ratio);
    }

    public void drawRect(float ratio){
        //根据输入比例算出需要画的长度
        Log.i("drawRect","drawRect:"+ratio/100);
        float length =(ratio/100)*getMeasuredWidth();
        //定义一个矩形
        RectF rectF=new RectF(0,0,length,getMeasuredHeight());
        c.drawRect(rectF,p);
    }
}
