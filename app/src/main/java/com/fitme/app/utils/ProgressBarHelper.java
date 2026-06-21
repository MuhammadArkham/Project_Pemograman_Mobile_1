package com.fitme.app.utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

public class ProgressBarHelper {

    public static void updateBarWeight(View bar, float weight) {
        if (bar == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = weight;
        bar.setLayoutParams(params);

        LinearLayout parent = (LinearLayout) bar.getParent();
        if (parent != null && parent.getChildCount() > 1) {
            View empty = parent.getChildAt(1);
            if (empty != null) {
                LinearLayout.LayoutParams ep = (LinearLayout.LayoutParams) empty.getLayoutParams();
                ep.weight = 100f - weight;
                empty.setLayoutParams(ep);
            }
        }
    }

    public static void animateProgressBar(View bar, float start, float end, long duration, long delay) {
        if (bar == null) return;
        ValueAnimator anim = ValueAnimator.ofFloat(start, end);
        anim.setDuration(duration);
        anim.setStartDelay(delay);
        anim.setInterpolator(new DecelerateInterpolator(2.0f));
        anim.addUpdateListener(a -> updateBarWeight(bar, (float) a.getAnimatedValue()));
        anim.start();
    }
}