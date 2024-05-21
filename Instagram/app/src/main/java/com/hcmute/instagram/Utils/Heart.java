package com.hcmute.instagram.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

public class Heart {

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    public ImageView heartWhite, heartRed;

    public Heart(ImageView heartWhite, ImageView heartRed) {
        this.heartWhite = heartWhite;
        this.heartRed = heartRed;
    }

    private void animateHeart(final ImageView likeButton) {
        // Use ObjectAnimator to animate the scale of the heart icon
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 1.2f, 1f);

        // Set duration and interpolator for the animation
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());

        // Create AnimatorSet and play the animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.start();
    }

    public void toggleLike() {

        AnimatorSet animationSet = new AnimatorSet();

        if (heartRed.getVisibility() == View.VISIBLE) {
            animateHeart(heartWhite);

            heartRed.setVisibility(View.GONE);
            heartWhite.setVisibility(View.VISIBLE);

        } else if (heartRed.getVisibility() == View.GONE) {
            animateHeart(heartRed);

            heartRed.setVisibility(View.VISIBLE);
            heartWhite.setVisibility(View.GONE);

        }

        animationSet.start();

    }
}
