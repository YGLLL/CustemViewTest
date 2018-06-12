package com.example.ygl.viewtest;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import java.text.DecimalFormat;
import java.util.ArrayList;

/*
 *创建人:yanggl
 *创建时间:2018-5-3  17:10
 *类描述:
 *备注:
 */
public class ProgressButton extends android.support.v7.widget.AppCompatTextView {

    //跳跳球风格
    public static final int STYLE_BALL_PULSE = 1;
    public static final int STYLE_BALL_JUMP = 2;

    public ProgressButton(Context context) {
        this(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            initAttrs();
            init();
            setupAnimations();
        }
    }

    //背景颜色
    private int mBackgroundColor;
    //下载中后半部分后面背景颜色
    private int mBackgroundSecondColor;
    //按钮边框圆滑程度
    private float mButtonRadius;
    //文字颜色
    private int mTextColor;
    //覆盖后颜色
    private int mTextCoverColor;
    //边框宽度
    private float mBorderWidth;
    //点动画样式
    private int mBallStyle = STYLE_BALL_JUMP;

    //获取xml中的参数（待加入）
    private void initAttrs() {
        mBackgroundColor = Color.parseColor("#0EACB2");
        mBackgroundSecondColor = Color.parseColor("#e0e0e0");
        mButtonRadius = dp2px(4);
        mTextColor = mBackgroundColor;
        mTextCoverColor = Color.WHITE;
        mBorderWidth = dp2px(2);
        mBallStyle = STYLE_BALL_JUMP;
    }

    private int mMaxProgress;
    private int mMinProgress;
    private float mProgress = -1;
    //是否显示边框
    private boolean showBorder;
    //背景画笔
    private Paint mBackgroundPaint;
    //按钮文字画笔
    private volatile Paint mTextPaint;
    private int mState;
    public static final int STATE_NORMAL = 0;//开始下载
    public static final int STATE_DOWNLOADING = 1;//下载之中
    public static final int STATE_PAUSE = 2;//暂停下载
    public static final int STATE_FINISH = 3;//下载完成

    //初始化各字段
    private void init() {
        mMaxProgress = 100;
        mMinProgress = 0;
        mProgress = 0;

        showBorder = false;

        //设置背景画笔
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //设置文字画笔
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(getTextSize());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //解决文字有时候画不出问题
            setLayerType(LAYER_TYPE_SOFTWARE, mTextPaint);
        }

        //初始化状态设为NORMAL
        mState = STATE_NORMAL;
        invalidate();
    }

    //下载平滑动画
    private ValueAnimator mProgressAnimation;
    private float mToProgress;

    //设置动画
    private void setupAnimations() {
        //ProgressBar的动画
        mProgressAnimation = ValueAnimator.ofFloat(0, 1).setDuration(500);
        mProgressAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float timePercent = (float) animation.getAnimatedValue();
                mProgress = ((mToProgress - mProgress) * timePercent + mProgress);
                invalidate();
            }
        });

        setBallStyle(mBallStyle);
    }

    //***********************************核心方法*******************************************
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            drawing(canvas);
        }
    }

    private void drawing(Canvas canvas) {
        drawBackground(canvas);
        drawTextAbove(canvas);
    }

    private RectF mBackgroundBounds;
    private float mProgressPercent;

    private void drawBackground(Canvas canvas) {

        mBackgroundBounds = new RectF();
        //根据Border宽度得到Button的显示区域
        //背景的的范围=显示范围-边框
        mBackgroundBounds.left = showBorder ? mBorderWidth : 0;
        mBackgroundBounds.top = showBorder ? mBorderWidth : 0;
        mBackgroundBounds.right = getMeasuredWidth() - (showBorder ? mBorderWidth : 0);
        mBackgroundBounds.bottom = getMeasuredHeight() - (showBorder ? mBorderWidth : 0);

        if (showBorder) {
            mBackgroundPaint.setStyle(Paint.Style.STROKE);
            mBackgroundPaint.setColor(mBackgroundColor);
            mBackgroundPaint.setStrokeWidth(mBorderWidth);
            canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
        }
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        //color
        switch (mState) {
            case STATE_NORMAL:
                mBackgroundPaint.setColor(mBackgroundColor);
                canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
                break;
            case STATE_PAUSE:
            case STATE_DOWNLOADING:
                //计算当前的进度
                mProgressPercent = mProgress / (mMaxProgress + 0f);
                mBackgroundPaint.setColor(mBackgroundSecondColor);
                canvas.save();
                //画出dst图层
                canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
                //设置图层显示模式为 SRC_ATOP
                PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);
                mBackgroundPaint.setColor(mBackgroundColor);
                mBackgroundPaint.setXfermode(porterDuffXfermode);
                //计算 src 矩形的右边界
                float right = mBackgroundBounds.right * mProgressPercent;
                //在dst画出src矩形
                canvas.drawRect(mBackgroundBounds.left, mBackgroundBounds.top, right, mBackgroundBounds.bottom, mBackgroundPaint);
                canvas.restore();
                mBackgroundPaint.setXfermode(null);
                break;
            case STATE_FINISH:
                mBackgroundPaint.setColor(mBackgroundColor);
                canvas.drawRoundRect(mBackgroundBounds, mButtonRadius, mButtonRadius, mBackgroundPaint);
                break;
        }
    }

    //记录当前文字
    private CharSequence mCurrentText;
    private float mTextRightBorder;
    private LinearGradient mProgressTextGradient;

    private void drawTextAbove(Canvas canvas) {
        //计算Baseline绘制的Y坐标
        final float y = canvas.getHeight() / 2 - (mTextPaint.descent() / 2 + mTextPaint.ascent() / 2);
        if (mCurrentText == null) {
            mCurrentText = "";
        }
        final float textWidth = mTextPaint.measureText(mCurrentText.toString());
        mTextBottomBorder = y;
        mTextRightBorder = (getMeasuredWidth() + textWidth) / 2;
        //color
        switch (mState) {
            case STATE_NORMAL:
                mTextPaint.setShader(null);
                mTextPaint.setColor(mTextCoverColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case STATE_PAUSE:
            case STATE_DOWNLOADING:

                //进度条压过距离
                float coverLength = getMeasuredWidth() * mProgressPercent;
                //开始渐变指示器
                float indicator1 = getMeasuredWidth() / 2 - textWidth / 2;
                //结束渐变指示器
                float indicator2 = getMeasuredWidth() / 2 + textWidth / 2;
                //文字变色部分的距离
                float coverTextLength = textWidth / 2 - getMeasuredWidth() / 2 + coverLength;
                float textProgress = coverTextLength / textWidth;
                if (coverLength <= indicator1) {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextColor);
                } else if (indicator1 < coverLength && coverLength <= indicator2) {
                    //设置变色效果
                    mProgressTextGradient = new LinearGradient((getMeasuredWidth() - textWidth) / 2, 0, (getMeasuredWidth() + textWidth) / 2, 0,
                            new int[]{mTextCoverColor, mTextColor},
                            new float[]{textProgress, textProgress + 0.001f},
                            Shader.TileMode.CLAMP);
                    mTextPaint.setColor(mTextColor);
                    mTextPaint.setShader(mProgressTextGradient);
                } else {
                    mTextPaint.setShader(null);
                    mTextPaint.setColor(mTextCoverColor);
                }
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                break;
            case STATE_FINISH:
                mTextPaint.setColor(mTextCoverColor);
                canvas.drawText(mCurrentText.toString(), (getMeasuredWidth() - textWidth) / 2, y, mTextPaint);
                drawLoadingBall(canvas);
                break;

        }

    }


    private Boolean showProgressNum = false;

    /**
     * 设置带下载进度的文字
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setProgressText(String text, float progress) {
        if (progress >= mMinProgress && progress <= mMaxProgress) {
            if (showProgressNum) {
                DecimalFormat format = new DecimalFormat("##0.0");
                mCurrentText = text + format.format(progress) + "%";
            } else {
                mCurrentText = text;
            }
            mToProgress = progress;
            if (mProgressAnimation.isRunning()) {
                mProgressAnimation.resume();
                mProgressAnimation.start();
            } else {
                mProgressAnimation.start();
            }
        } else if (progress < mMinProgress) {
            mProgress = 0;
        } else if (progress > mMaxProgress) {
            mProgress = 100;
            if (showProgressNum) {
                mCurrentText = text + progress + "%";
            } else {
                mCurrentText = text;
            }
            invalidate();
        }
    }

    //************************辅助方法*******************************************
    //点的间隙
    private float mBallSpacing = 4;

    public void drawLoadingBall(Canvas canvas) {
        for (int i = 0; i < 3; i++) {
            canvas.save();
            float translateX = mTextRightBorder + 10 + (mBallRadius * 2) * i + mBallSpacing * i;
            canvas.translate(translateX, mTextBottomBorder);
            canvas.drawCircle(0, translateYFloats[i], mBallRadius * scaleFloats[i], mTextPaint);
            canvas.restore();
        }
    }

    //点运动动画
    private ArrayList<ValueAnimator> mAnimators;

    //设置点动画样式
    private void setBallStyle(int mBallStyle) {
        this.mBallStyle = mBallStyle;
        if (mBallStyle == STYLE_BALL_PULSE) {
            mAnimators = createBallPulseAnimators();
        } else {
            mAnimators = createBallJumpAnimators();
        }
    }

    private float[] scaleFloats = new float[]{SCALE,
            SCALE,
            SCALE};
    public static final float SCALE = 1.0f;

    public ArrayList<ValueAnimator> createBallPulseAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        int[] delays = new int[]{120, 240, 360};
        for (int i = 0; i < 3; i++) {
            final int index = i;

            ValueAnimator scaleAnim = ValueAnimator.ofFloat(1, 0.3f, 1);

            scaleAnim.setDuration(750);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);

            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scaleFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }

    private float mTextBottomBorder;
    //点的半径
    private float mBallRadius = 6;
    private float[] translateYFloats = new float[3];

    public ArrayList<ValueAnimator> createBallJumpAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        int[] delays = new int[]{70, 140, 210};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            ValueAnimator scaleAnim = ValueAnimator.ofFloat(mTextBottomBorder, mTextBottomBorder - mBallRadius * 2, mTextBottomBorder);
            scaleAnim.setDuration(600);
            scaleAnim.setRepeatCount(-1);
            scaleAnim.setStartDelay(delays[i]);
            scaleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    translateYFloats[index] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            animators.add(scaleAnim);
        }
        return animators;
    }

    private void startAnimators() {
        for (int i = 0; i < mAnimators.size(); i++) {
            ValueAnimator animator = mAnimators.get(i);
            animator.start();
        }
    }

    private void stopAnimators() {
        if (mAnimators != null) {
            for (ValueAnimator animator : mAnimators) {
                if (animator != null && animator.isStarted()) {
                    animator.end();
                }
            }
        }
    }

    private int dp2px(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    //****************************get/set方法***************************************
    public boolean isShowBorder() {
        return showBorder;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public void setBorderWidth(int width) {
        this.mBorderWidth = dp2px(width);
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
    }

    public float getButtonRadius() {
        return mButtonRadius;
    }

    public void setButtonRadius(float buttonRadius) {
        mButtonRadius = buttonRadius;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public int getTextCoverColor() {
        return mTextCoverColor;
    }

    public void setTextCoverColor(int textCoverColor) {
        mTextCoverColor = textCoverColor;
    }

    public int getMinProgress() {
        return mMinProgress;
    }

    public void setMinProgress(int minProgress) {
        mMinProgress = minProgress;
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    //进度条颜色
    public int getmBackgroundColor() {
        return mBackgroundColor;
    }

    public void setmBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    //进度条背景色
    public int getmBackgroundSecondColor() {
        return mBackgroundSecondColor;
    }

    public void setmBackgroundSecondColor(int mBackgroundSecondColor) {
        this.mBackgroundSecondColor = mBackgroundSecondColor;
    }

    //文字颜色
    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    //被进度条覆盖后的文字颜色
    public int getmTextCoverColor() {
        return mTextCoverColor;
    }

    public void setmTextCoverColor(int mTextCoverColor) {
        this.mTextCoverColor = mTextCoverColor;
    }

    public Boolean getShowProgressNum() {
        return showProgressNum;
    }

    public void setShowProgressNum(Boolean showProgressNum) {
        this.showProgressNum = showProgressNum;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        if (mState != state) {//状态确实有改变
            this.mState = state;
            invalidate();
            if (state == STATE_FINISH) {
                //开启点动画
                startAnimators();
            } else {
                stopAnimators();
            }
        }

    }

    /**
     * 设置当前按钮文字
     */
    public void setCurrentText(CharSequence charSequence) {
        mCurrentText = charSequence;
        invalidate();
    }

    //*****************************保存状态***********************************
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        ProgressButton.SavedState ss = (ProgressButton.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mState = ss.state;
        mProgress = ss.progress;
        mCurrentText = ss.currentText;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new ProgressButton.SavedState(superState, (int) mProgress, mState, mCurrentText.toString());
    }

    public static class SavedState extends BaseSavedState {

        private int progress;
        private int state;
        private String currentText;

        public SavedState(Parcelable parcel, int progress, int state, String currentText) {
            super(parcel);
            this.progress = progress;
            this.state = state;
            this.currentText = currentText;
        }

        private SavedState(Parcel in) {
            super(in);
            progress = in.readInt();
            state = in.readInt();
            currentText = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(progress);
            out.writeInt(state);
            out.writeString(currentText);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public ProgressButton.SavedState createFromParcel(Parcel in) {
                return new ProgressButton.SavedState(in);
            }

            @Override
            public ProgressButton.SavedState[] newArray(int size) {
                return new ProgressButton.SavedState[size];
            }
        };
    }

    //*******************************针对调音助手专用方法******************************
    public void isNoDownload(){
        setState(STATE_NORMAL);
        setmBackgroundColor(getResources().getColor(R.color.down_button_gray));
        setCurrentText(getResources().getString(R.string.download));
    }
    public void isDownloading(float p){
        setState(STATE_DOWNLOADING);
        setmBackgroundColor(getResources().getColor(R.color.down_button_blue));
        setProgressText(getResources().getString(R.string.downloading),p);
    }
    public void isNoUse(){
        setState(STATE_NORMAL);
        setmBackgroundColor(getResources().getColor(R.color.down_button_blue));
        setCurrentText(getResources().getString(R.string.use));
    }
    public void isUseAlready(){
        setState(STATE_NORMAL);
        setmBackgroundColor(getResources().getColor(R.color.down_button_yellow));
        setCurrentText(getResources().getString(R.string.use_already));
    }
}
