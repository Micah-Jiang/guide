package com.micah.compass.view;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.micah.compass.R;

/**
 * @Author m.kong
 * @Date 2021/5/26 下午3:52
 * @Version 1
 * @Description
 */
public class DirectionView extends View {

    /**
     * 圆环使用
     * */
    private Paint mRingPaint;

    /**
     * 绘制中心实线的画布
     */
    private Paint mCententPaint;

    /**
     * 圆环半径 根据view的宽度计算
     * */
    private int mRadius = 200;

    /**
     * 圆环的中心点 -- 画圆环和旋转画布时需要使用
     * */
    private int x, y;

    /**
     * 圆环动画使用 -- 与mRingPaint唯一不同得方在于颜色
     * */
    private Paint mRingAnimPaint;

    /**
     * 圆环大小 矩形
     * */
    private RectF mRectf;

    private Context mContext;

    /**
     * 圆环 宽度
     * */
    private final int mHeartPaintWidth = 30;

    /**
     * 圆环动画开始时 画弧的偏移量
     * */
    private int mAnimAngle = -1;

    private Paint outCircle;

    //内心圆是一个颜色辐射渐变的圆
    private Shader mInnerShader;
    private Paint mInnerPaint;

    public DirectionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.mContext = context;
        init();
    }

    public DirectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.mContext = context;
        init();
    }

    public DirectionView(Context context) {
        this(context, null);
        this.mContext = context;
        init();
    }

    private void init(){
        mRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRingPaint.setStrokeWidth(mHeartPaintWidth);
        mRingPaint.setStyle(Paint.Style.STROKE);
        mRingAnimPaint = new Paint(mRingPaint);
        mRingAnimPaint.setColor(Color.WHITE);
        //初始化心跳曲线
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

        mCententPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCententPaint.setColor(Color.BLACK);

        mCententPaint.setStrokeWidth(3);

        mInnerPaint = new Paint();
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setAntiAlias(true);

        outCircle = new Paint();
        outCircle.setStyle(Paint.Style.STROKE);
        outCircle.setAntiAlias(true);
        //outCircle.setColor(getContext().getResources().getColor(R.color.lightGray));
        //outCircle.setStrokeWidth((float)5);
    }

    /**
     * canvas抗锯齿开启需要
     * */
    private DrawFilter mDrawFilter;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        x = w / 2;
        y = h / 2;
        mRadius = w / 2 - mHeartPaintWidth * 3; //因为制定了Paint的宽度，因此计算半径需要减去这个
        mRectf = new RectF(x - mRadius, y - mRadius, x + mRadius, y + mRadius);
    }
    public float rotate = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);//在canvas上抗锯齿
        //由于drawArc默认从x轴开始画，因此需要将画布旋转或者绘制角度旋转，2种方案
        //int level = canvas.save();

        //绘制中心线
        mCententPaint.setStrokeWidth(3);
        canvas.drawLine(x,y-50,x,y + 50, mCententPaint);
        canvas.drawLine(x - 50,y,x + 50, y, mCententPaint);
        //Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.ic_compass);
        //canvas.drawBitmap(bitmap,new Matrix(), new Paint());

        //绘制竖线
        drawAL(x,(int)mRectf.top + 30, x, (int) mRectf.top - 90, canvas);

        canvas.rotate(rotate, x, y);// 旋转的时候一定要指明中心

        for (int i = 0; i < 360; i += 3) {
            mRingPaint.setColor(getContext().getResources().getColor(R.color.theme_duck));
            canvas.drawArc(mRectf, i, 1, false, mRingPaint);
        }

        mCententPaint.setTextSize(50);
        mCententPaint.setColor(Color.RED);
        canvas.drawText("N",x,mRectf.top + mHeartPaintWidth  + 50,mCententPaint);

        mCententPaint.setColor(Color.BLACK);
        canvas.rotate(90, x, y);// 旋转的时候一定要指明中心
        canvas.drawText("E",x,mRectf.top + mHeartPaintWidth + 55,mCententPaint);
        canvas.rotate(90, x, y);// 旋转的时候一定要指明中心
        canvas.drawText("S",x,mRectf.top + mHeartPaintWidth + 55,mCententPaint);
        canvas.rotate(90, x, y);// 旋转的时候一定要指明中心
        canvas.drawText("W",x,mRectf.top + mHeartPaintWidth + 55,mCententPaint);
        canvas.rotate(90, x, y);// 旋转的时候一定要指明中心
        mCententPaint.setTextSize(30);

        for (int i = 0; i < 360; i += 3) {
            if(i == 30 || i == 60 || i == 90 || i == 120 || i == 150 || i == 180 || i == 210 || i == 240 || i == 270 || i == 300 || i == 330 || i == 0){
                mCententPaint.setFakeBoldText(true);
                canvas.drawText(""+i,x,mRectf.top - mHeartPaintWidth ,mCententPaint);
                canvas.rotate(30, x, y);// 旋转的时候一定要指明中心
            }
        }

        drawInnerCricle(canvas);
    }

    /**
     * 画中心渐变颜色圈
     */
    private void drawInnerCricle(Canvas canvas) {

        /*mInnerShader = new RadialGradient(x,y,415, Color.parseColor("#ffffff"),
                Color.parseColor("#000000"),Shader.TileMode.CLAMP);
        mInnerPaint.setShader(mInnerShader);
        canvas.drawCircle(x,y,415,mInnerPaint);*/

        outCircle.setStrokeWidth((float)5);
        outCircle.setColor(Color.BLACK);
        canvas.drawCircle(x, y, mRadius-30, outCircle);
        outCircle.setStrokeWidth((float)3);
        canvas.drawCircle(x, y, mRadius-115, outCircle);
    }

    /**
     * 画箭头
     */
    /**
     * 画箭头
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     */
    public void drawAL(int sx, int sy, int ex, int ey, Canvas canvas){
        // 箭头高度
        double H = 30;
        // 底边的一半
        double L = 15;
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        // 箭头角度
        double awrad = Math.atan(L / H);
        // 箭头的长度
        double arraow_len = Math.sqrt(L * L + H * H);
        double[] arrXY_1 = rotateVec(ex - sx, ey - sy, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(ex - sx, ey - sy, -awrad, true, arraow_len);
        // (x3,y3)是第一端点
        double x_3 = ex - arrXY_1[0];
        double y_3 = ey;
        // (x4,y4)是第二端点
        double x_4 = ex - arrXY_2[0];
        double y_4 = ey;
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();

        mCententPaint.setColor(Color.BLACK);
        mCententPaint.setStrokeWidth(3);
        // 画线
        canvas.drawLine(sx, sy, ex, ey,mCententPaint);
        // 画三角形
        Path triangle = new Path();
        triangle.moveTo(sx, sy-90);
        triangle.lineTo(x4, y4);
        triangle.lineTo(x3, y3);
        triangle.close();
        canvas.drawPath(triangle,mCententPaint);
    }

    /**
     * 计算
     */
    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen) {
        double mathStr[] = new double[2];
        // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathStr[0] = vx;
            mathStr[1] = vy;
        }
        return mathStr;
    }
}

