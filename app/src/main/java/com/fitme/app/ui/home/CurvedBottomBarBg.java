package com.fitme.app.ui.home;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.graphics.Outline;
import android.view.ViewOutlineProvider;
import android.view.View;

public class CurvedBottomBarBg extends View {

    private final Paint paintBg     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path  path        = new Path();
    private float       cutoutX;

    private static final float CUTOUT_RADIUS = 38f;
    private static final float CUTOUT_DEPTH  = 34f;
    private static final float BAR_TOP       = 14f;

    public CurvedBottomBarBg(Context ctx) { super(ctx); init(); }
    public CurvedBottomBarBg(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public CurvedBottomBarBg(Context ctx, AttributeSet a, int d) { super(ctx, a, d); init(); }

    private void init() {
        paintBg.setColor(Color.WHITE);
        paintBg.setStyle(Paint.Style.FILL);

        // Hardware-accelerated native shadow
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (!path.isEmpty()) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        outline.setPath(path);
                    } else {
                        // Fallback for older API versions, though most modern devices support path outline
                        // But since this is a complex path, outline.setConvexPath might be required.
                        // Actually setPath works on Q+ and for convex paths on older APIs.
                        // To be absolutely safe and since we want a subtle shadow, we just use the path.
                        try {
                            outline.setConvexPath(path);
                        } catch (IllegalArgumentException e) {
                            // Ignore if path is not convex
                        }
                    }
                }
            }
        });
        setClipToOutline(false);
    }

    public void setCutoutX(float x) {
        cutoutX = x;
        invalidate();
    }

    public float getCutoutX() { return cutoutX; }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        // Default ke posisi Home = 1/6 layar (untuk 3 item nav)
        if (cutoutX == 0f) cutoutX = w / 6f;
    }

    private void buildPath(float w, float h, float den) {
        float cx = cutoutX;
        float r  = CUTOUT_RADIUS * den;
        float d  = CUTOUT_DEPTH  * den;
        float t  = BAR_TOP       * den;

        float leftEdge  = Math.max(0f, cx - r * 1.6f);
        float rightEdge = Math.min(w,  cx + r * 1.6f);

        path.reset();
        path.moveTo(0, t);
        path.lineTo(leftEdge, t);

        path.cubicTo(cx - r * 0.6f, t, cx - r * 0.9f, t + d, cx, t + d);
        path.cubicTo(cx + r * 0.9f, t + d, cx + r * 0.6f, t, rightEdge, t);

        path.lineTo(w, t);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        
        // Invalidate outline so shadow updates when path changes
        invalidateOutline();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w   = getWidth();
        float h   = getHeight();
        float den = getResources().getDisplayMetrics().density;
        buildPath(w, h, den);
        canvas.drawPath(path, paintBg);
    }
}