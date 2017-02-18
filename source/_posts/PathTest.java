package com.centling.sweepingrobot.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.centling.sweepingrobot.util.ShowToast;

/**
 * PathTest
 * Created by fionera on 17-2-16 in sweeping_robot.
 */

public class PathTest
        extends View {

    private Paint paint;
    private int width;
    private int height;

    private Path searchPath;
    private Path circlePath;

    private PathMeasure pathMeasure;

    private float animPercent;
    private ValueAnimator searchStartAnim;
    private ValueAnimator circleStartAnim;

    private int drawingFlag = 1;

    public PathTest(Context context) {
        this(context, null);
    }

    public PathTest(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PathTest(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initPaint();
        initAnim();
        initAnimListener();
        startAnim();
    }

    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
    }

    private void initAnim() {
        long animDuration = 2000;
        searchStartAnim = ValueAnimator.ofFloat(0, 1).setDuration(animDuration);
        circleStartAnim = ValueAnimator.ofFloat(0, 1).setDuration(animDuration);
    }

    private void initAnimListener() {
        searchStartAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startBigAnim();
            }
        });
        circleStartAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reverseAnim();
            }
        });
        searchStartAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        circleStartAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    private void startAnim() {
        drawingFlag = 1;
        searchStartAnim.start();
        invalidate();
    }

    private void startBigAnim() {
        drawingFlag = 2;
        searchStartAnim.removeAllListeners();
        circleStartAnim.start();
        invalidate();
    }

    private void reverseAnim() {
        drawingFlag = 1;
        circleStartAnim.removeAllListeners();
        searchStartAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animPercent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        searchStartAnim.reverse();
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        initPath();
    }

    private void initPath() {
        searchPath = new Path();
        circlePath = new Path();

        float bigCircleWidth = Math.min(width, height);

        float smallRadius = bigCircleWidth / 8;
        RectF searchRect = new RectF(-smallRadius, -smallRadius, smallRadius, smallRadius);
        searchPath.addArc(searchRect, 45, 358);

        float bigRadius = smallRadius * 2;
        RectF circleRect = new RectF(-bigRadius, -bigRadius, bigRadius, bigRadius);
        circlePath.addArc(circleRect, 45, -359);

        pathMeasure = new PathMeasure(circlePath, false);
        float[] pos = new float[2];
        pathMeasure.getPosTan(0, pos, null);
        searchPath.lineTo(pos[0], pos[1]);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(width / 2, height / 2);
        if (1 == drawingFlag) {
            drawSearch(canvas);
        } else {
            drawCircle(canvas);
        }
    }

    private void drawSearch(Canvas canvas) {
        Path dst = new Path();
        pathMeasure.setPath(searchPath, false);
        pathMeasure.getSegment(pathMeasure.getLength() * animPercent, pathMeasure.getLength(), dst,
                true);
        canvas.drawPath(dst, paint);
    }

    private void drawCircle(Canvas canvas) {
        Path dst = new Path();
        pathMeasure.setPath(circlePath, false);
        float stop = pathMeasure.getLength() * animPercent;
        float start = (float) (stop - (0.5 - Math.abs(animPercent - 0.5)) * 200f);
        pathMeasure.getSegment(start, stop, dst, true);
        canvas.drawPath(dst, paint);
    }
}
