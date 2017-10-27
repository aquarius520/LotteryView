package com.aquarius.customview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by aquarius on 17-4-11.
 */
public class LotteryView extends View {

    private int mScreenWidth;   // 屏幕宽度
    private int mScreenHeight;  // 屏幕高度

    private int mSelfTotalWidth;    // 自身最大的宽度

    private static float DEFAULT_SIZE_FACTOR = 0.85f;   // 自身占用屏幕宽度的比例

    private int mOuterCircleWidth;  // 最外边圆环
    private Paint mOuterCirclePaint;
    private int mOuterCircleBackgroundColor;

    private Paint mInnerPaint;
    private int mInnerCircleBackgroundColor;

    private Paint mSmallCirclePaint;
    private int mSmallCircleBlueColor;
    private int mSmallCircleYellowColor;
    private int mInnerCardTextColor;
    private int mCenterCardBackgroundColor;
    private int mInnerCardDefaultColor;

    private int mSmallCircleRadius;  // 小圆圈半径
    private int mInnerCardWidth;    // 卡片宽度
    private int mInnerCardSpace;    // 卡片间隔


    private boolean mHadInitial = false;
    private ArrayList<Pair<Pair<Integer, Integer>,Pair<Integer, Integer>>> mCardPositionInfoList;
    private Context mContext;
    private AlertDialog mAlertDialog;

    private int[] mPicResId;
    private String[] mInfoResArray;
    private Rect mBounds = new Rect();
    private float mSmallInfoTextSize;
    private float mBigInfoTextSize;

    private boolean mNeedRandomTimes = false;
    private int mInvalidateCircleCount;
    private int mInvalidateInnerCardCount;
    private int mLotteryInvalidateTimes;
    private boolean mStartAnimation = false; // not real animation
    private boolean mLastEndSelected = false;

    public LotteryView(Context context) {
        this(context, null);
    }

    public LotteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LotteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        acquireCustomAttValues(context, attrs);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mSelfTotalWidth = mScreenWidth < mScreenHeight ?
                (int)(mScreenWidth * DEFAULT_SIZE_FACTOR) : (int)(mScreenHeight * DEFAULT_SIZE_FACTOR);

        mSmallInfoTextSize = context.getResources().getDimension(R.dimen.lotteryview_inner_card_text_size);
        mBigInfoTextSize = context.getResources().getDimension(R.dimen.lotteryview_inner_card_big_text_size);
        mOuterCircleWidth = (int) context.getResources().getDimension(R.dimen.lotteryview_outer_circle_width);
        mInnerCardSpace   = (int) context.getResources().getDimension(R.dimen.lotteryview_inner_card_blank);
        mInnerCardWidth   = (mSelfTotalWidth- getPaddingLeft() -getPaddingRight() - mOuterCircleWidth * 2 -  mInnerCardSpace * 4) / 3;
        mSmallCircleRadius = (int) context.getResources().getDimension(R.dimen.lotteryview_outer_small_circle_radius);

        mInnerCardTextColor = context.getResources().getColor(R.color.inner_card_text_color);
        mCenterCardBackgroundColor = context.getResources().getColor(R.color.center_card_bg_color);

        mOuterCircleBackgroundColor = context.getResources().getColor(R.color.outer_circle_bg_color);
        mOuterCirclePaint = new Paint();
        mOuterCirclePaint.setColor(mOuterCircleBackgroundColor);
        mOuterCirclePaint.setAntiAlias(true);
        mOuterCirclePaint.setStrokeWidth(mOuterCircleWidth);
        mOuterCirclePaint.setStyle(Paint.Style.FILL);

        mSmallCircleBlueColor = mSmallCircleBlueColor != 0 ? mSmallCircleBlueColor : context.getResources().getColor(R.color.small_circle_color_blue);
        mSmallCircleYellowColor = mSmallCircleYellowColor != 0 ? mSmallCircleYellowColor : context.getResources().getColor(R.color.small_circle_color_yellow);
        mSmallCirclePaint = new Paint();
        mSmallCirclePaint.setColor(mSmallCircleBlueColor);
        mSmallCirclePaint.setAntiAlias(true);
        mOuterCirclePaint.setStyle(Paint.Style.FILL);


        mInnerCircleBackgroundColor = context.getResources().getColor(R.color.inner_circle_bg_color);
        mInnerPaint = new Paint();
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setColor(mInnerCircleBackgroundColor);
        mInnerPaint.setStyle(Paint.Style.FILL);

        mCardPositionInfoList = new ArrayList<>();
        initResId();
    }

    private void acquireCustomAttValues(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LotteryView);
        mSmallCircleBlueColor   = ta.getColor(R.styleable.LotteryView_outer_small_circle_color_default, 0);
        mSmallCircleYellowColor = ta.getColor(R.styleable.LotteryView_outer_small_circle_color_active, 0);
        mLotteryInvalidateTimes = ta.getInt(R.styleable.LotteryView_lottery_invalidate_times, 0);
        DEFAULT_SIZE_FACTOR     = ta.getFloat(R.styleable.LotteryView_self_width_size_factor, DEFAULT_SIZE_FACTOR);
        mInnerCardDefaultColor  = ta.getColor(R.styleable.LotteryView_inner_round_card_color_default, Color.parseColor("#ffffff"));
        ta.recycle();
    }

    private void initResId() {
        mPicResId = new int[]{
                R.mipmap.icon_huawei_mobile, R.mipmap.icon_xiaomi_bracelet, R.mipmap.icon_qq_sport,
                R.mipmap.icon_gopro_camera, 0, R.mipmap.icon_misfit_flash,
                R.mipmap.icon_qq_sport, R.mipmap.icon_qq_gongzai, 0
        };

        mInfoResArray = mContext.getResources().getStringArray(R.array.jifeng_array_info);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mSelfTotalWidth, mSelfTotalWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        drawOuterRoundCircle(canvas);
        drawOuterDecorateSmallCircle(canvas);
        drawInnerBackground(canvas);
        drawInnerCards(canvas);
        loopSmallCircleAnimation();
        loopInnerRoundCardAnimation();
    }

    /** 外层带圆角矩形圆环 */
    private void drawOuterRoundCircle(Canvas canvas) {
        canvas.save();
        canvas.clipRect(
                mOuterCircleWidth + getPaddingLeft(),
                mOuterCircleWidth + getPaddingTop(),
                mSelfTotalWidth - mOuterCircleWidth - getPaddingRight(),
                mSelfTotalWidth - mOuterCircleWidth - getPaddingBottom(),
                Region.Op.DIFFERENCE);

        canvas.drawRoundRect(
                getPaddingLeft(),
                getPaddingTop(),
                mSelfTotalWidth - getPaddingRight(),
                mSelfTotalWidth - getPaddingBottom(),
                18, 18, mOuterCirclePaint);
        canvas.restore();
    }


    private void drawOuterDecorateSmallCircle(Canvas canvas) {
        int result = mInvalidateCircleCount % 2;

        // top
        int x = 0, y = 0;
        int sideSize = mSelfTotalWidth - mOuterCircleWidth * 2 - getPaddingLeft() - getPaddingRight(); // 除去最外边圆环后的边长
        for (int i = 0; i < 10; i++) {
            mSmallCirclePaint.setColor(i % 2 == result ? mSmallCircleYellowColor : mSmallCircleBlueColor);
            x = mOuterCircleWidth + (sideSize - mSmallCircleRadius * 2 * 9) / 9 * i + mSmallCircleRadius * 2 * i + getPaddingLeft();
            y = (mOuterCircleWidth - mSmallCircleRadius * 2) / 2 + mSmallCircleRadius + getPaddingTop();
            canvas.drawCircle(x, y, mSmallCircleRadius, mSmallCirclePaint);
        }

        // bottom
        for (int i = 0; i < 10; i++) {
            mSmallCirclePaint.setColor(i % 2 == result ? mSmallCircleYellowColor : mSmallCircleBlueColor);
            x = mOuterCircleWidth + (sideSize - mSmallCircleRadius * 2 * 9) / 9 * i + mSmallCircleRadius * 2 * i + getPaddingLeft();
            y = mSelfTotalWidth - mOuterCircleWidth + (mOuterCircleWidth - mSmallCircleRadius * 2) / 2 + mSmallCircleRadius - getPaddingBottom();
            canvas.drawCircle(x, y, mSmallCircleRadius, mSmallCirclePaint);
        }

        // left
        for(int i = 0; i < 9; i++) {
            mSmallCirclePaint.setColor(i % 2 == (result == 0 ? 1 : 0) ? mSmallCircleYellowColor : mSmallCircleBlueColor);
            x = mOuterCircleWidth / 2 + getPaddingLeft();
            y =  mOuterCircleWidth*2 + (sideSize - mSmallCircleRadius * 2 * 9) / 9 * i + mSmallCircleRadius * 2 * i + getPaddingTop();
            canvas.drawCircle(x, y, mSmallCircleRadius, mSmallCirclePaint);
        }

        // right
        for(int i = 0; i < 9; i++) {
            mSmallCirclePaint.setColor(i % 2 == result ? mSmallCircleYellowColor : mSmallCircleBlueColor);
            x = mSelfTotalWidth - mOuterCircleWidth / 2 - getPaddingRight();
            y =  mOuterCircleWidth*2 + (sideSize - mSmallCircleRadius * 2 * 9) / 9 * i + mSmallCircleRadius * 2 * i + getPaddingTop();
            canvas.drawCircle(x, y, mSmallCircleRadius, mSmallCirclePaint);
        }
    }

    private void drawInnerBackground(Canvas canvas) {
        canvas.drawRect(mOuterCircleWidth + getPaddingLeft(), mOuterCircleWidth + getPaddingTop(),
                mSelfTotalWidth - mOuterCircleWidth - getPaddingRight(),
                mSelfTotalWidth - mOuterCircleWidth - getPaddingBottom(), mInnerPaint);
    }

    private void drawInnerCards(Canvas canvas) {
        int left = 0, top = 0, right = 0, bottom = 0;
        int spaceNum = 0;
        for(int i = 0 ; i < 9 ; i++) {
            spaceNum = i % 3 + 1;
            left = mOuterCircleWidth + mInnerCardWidth * (i%3) + mInnerCardSpace * spaceNum + getPaddingLeft();
            top = mOuterCircleWidth + mInnerCardWidth * (i/3) +mInnerCardSpace * (i/3 + 1) + getPaddingTop();
            right = left + mInnerCardWidth;
            bottom = top + mInnerCardWidth;
            if(!mHadInitial) {
                mCardPositionInfoList.add(new Pair(new Pair(left, right), new Pair(top, bottom)));
            }
            drawInnerRoundCard(canvas, left, top, right, bottom, i);
        }
        mHadInitial = true;
    }

    private void drawInnerRoundCard(Canvas canvas, int left, int top, int right, int bottom, int index) {

        boolean need = switchCardColorIfNeed(index);

        mInnerPaint.setColor(mInnerCardDefaultColor);

        if(mStartAnimation && need) {
            mInnerPaint.setColor(mOuterCircleBackgroundColor);
        }

        // 绘制内部小卡片
        if(index == 4) {
            mInnerPaint.setColor(Color.parseColor("#73D7F8"));
        }

        canvas.drawRoundRect(left, top, right, bottom, 12, 12, mInnerPaint);

        if(index ==4) {
            mInnerPaint.setColor(mCenterCardBackgroundColor);
            int space = mInnerCardWidth / 9;
            canvas.drawRoundRect(left + space, top + space, right - space, bottom - space, 12, 12, mInnerPaint);
        }
        // 绘制卡片中的图片
        int picHeight = 0;
        if (mPicResId != null && mPicResId[index] != 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),mPicResId[index]);
            picHeight = bitmap.getHeight();
            int picLeft = left + (mInnerCardWidth - bitmap.getWidth()) / 2;
            int picTop  = top + mInnerCardWidth / 7;
            canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(),mPicResId[index]), picLeft, picTop, mInnerPaint);
        }
        // 绘制卡片说明文字
        if (mInfoResArray != null && !TextUtils.isEmpty(mInfoResArray[index])) {
            if(index == 4 || index == 8) { // center text
                mInnerPaint.setColor(index == 4 ? Color.parseColor("#ffffff") : mInnerCardTextColor);
                mInnerPaint.setTextSize(mBigInfoTextSize);
                int textX = left + (mInnerCardWidth - getTextWidth(mInfoResArray[index].substring(0, 2), mInnerPaint))/2;
                int textY_1 = top + (mInnerCardWidth/4 + getTextHeight(mInfoResArray[index].substring(0, 2), mInnerPaint)) - 8;
                int textY_2 = top + (mInnerCardWidth/4 + getTextHeight(mInfoResArray[index].substring(0, 2), mInnerPaint)*2 + mInnerCardWidth/10) -8;
                canvas.drawText(mInfoResArray[index], 0, 2, textX, textY_1, mInnerPaint);
                canvas.drawText(mInfoResArray[index], 2, 4, textX, textY_2, mInnerPaint);
                return;
            }
            mInnerPaint.setColor(mInnerCardTextColor);
            mInnerPaint.setTextSize(mSmallInfoTextSize);
            int textX = left + (mInnerCardWidth - getTextWidth(mInfoResArray[index], mInnerPaint))/2;
            int textY = top + picHeight + mInnerCardWidth / 4 + getTextHeight(mInfoResArray[index], mInnerPaint) -10;
            canvas.drawText(mInfoResArray[index],textX, textY, mInnerPaint);
        }
    }


    private boolean switchCardColorIfNeed(int index) {
        int result = mInvalidateInnerCardCount % 9;
        if((result == 0 && index == 0) || (result == 1 && index == 1) || (result == 2 && index == 2)
                || (result == 6 && index == 6)) {
            return true;
        }
        if((result == 3 && index == 5) || (result == 4 && index == 8) || (result == 5 && index == 7)
                || (result == 7 && index == 3)) {
            return true;
        }
        return false;
    }

    private void loopSmallCircleAnimation() {
        // not real animation, just like it.
        if(mStartAnimation){
            if(mInvalidateInnerCardCount % 8 == 0) {
                mInvalidateCircleCount++;
                //postInvalidate();
            }
        }else {
            mInvalidateCircleCount++;
            postInvalidateDelayed(800);
        }
    }

    private void loopInnerRoundCardAnimation() {
        if(!mStartAnimation || mLastEndSelected) return;

        if(mInvalidateInnerCardCount == mLotteryInvalidateTimes){
            //mStartAnimation = false;
            mLastEndSelected = true;
            postInvalidate();
            postDelayed(new ResultTask(mLotteryInvalidateTimes), 300);
            return;
        }

        mInvalidateInnerCardCount++;
        postInvalidateDelayed(50);

    }

    private class ResultTask implements Runnable{
        int times;
        public ResultTask(int times) {
            this.times = times;
        }
        @Override
        public void run() {
            mInvalidateInnerCardCount = 0;
            mLastEndSelected = false;
            String info = "";
            int i = times % 9;
            info = mInfoResArray[i];
            if(i == 3) info = mInfoResArray[5];
            if(i == 4) info = mInfoResArray[8];
            if(i == 5) info = mInfoResArray[7];
            if(i == 7) info = mInfoResArray[3];
            showResultDialog(mContext, info);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            break;

            case MotionEvent.ACTION_UP:
                int index = getTouchPositionInCardList(x, y);
                if(index == 5) {
                    if(mNeedRandomTimes || mLotteryInvalidateTimes == 0) {
                        mLotteryInvalidateTimes =  (new Random().nextInt(9) + 1) * 9 + new Random().nextInt(9);
                        mNeedRandomTimes = true;
                    }
                    showReminderDialog(mContext);
                }
                break;
        }

        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStartAnimation = false;
        mInvalidateCircleCount = 0;
        mInvalidateInnerCardCount = 0;
        mNeedRandomTimes = false;

    }

    private void showResultDialog(Context context, String result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.result_title)
                .setMessage(context.getString(R.string.result_message, result))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
       builder.create().show();
    }

    private void showReminderDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choujiang)
                .setMessage(R.string.choujiang_desc)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mAlertDialog != null) mAlertDialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mStartAnimation = true;
                        invalidate();
                    }
                });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private int getTouchPositionInCardList(int x, int y) {
        if(mCardPositionInfoList != null) {
            int index = 1;
            for (Pair<Pair<Integer, Integer>,Pair<Integer, Integer>> pair : mCardPositionInfoList) {
                if(x > pair.first.first && x < pair.first.second && y > pair.second.first && y < pair.second.second) {
                    return index;
                }
                index++;
            }
        }
        return 0;
    }


    private int getTextWidth(String str, Paint paint) {
        paint.getTextBounds(str, 0, str.length(), mBounds);
        return mBounds.width();
    }

    private int getTextHeight(String text,Paint paint){
        paint.getTextBounds(text,0,text.length(), mBounds);
        return mBounds.height();
    }
}
