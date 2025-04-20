package com.example.cropmate;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;

public class SplashScreen extends AppCompatActivity {

    private ImageView ball, logo, designTopLeft, designTopRight, designBottomLeft, designBottomRight;
    private View bgView;
    private LottieAnimationView lottieAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        lottieAnimation = findViewById(R.id.lottie_animation);

        // Initialize UI components
        ball = findViewById(R.id.ball);
        logo = findViewById(R.id.logo);
        bgView = findViewById(R.id.bgView);
        designTopLeft = findViewById(R.id.design_top_left);
        designTopRight = findViewById(R.id.design_top_right);
        designBottomLeft = findViewById(R.id.design_bottom_left);
        designBottomRight = findViewById(R.id.design_bottom_right);

        startBallDrop();
        lottieAnimation.playAnimation();

    }

    private void startBallDrop() {
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float centerY = screenHeight / 1f;  // Position the ball at a better midpoint

        // Ball drops from the top to center
        ObjectAnimator ballDrop = ObjectAnimator.ofFloat(ball, "translationY", -100f, centerY);
        ballDrop.setDuration(1000);
        ballDrop.setInterpolator(new AccelerateInterpolator());

        ballDrop.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startBouncing(centerY);
            }

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        ballDrop.start();
    }

    private void startBouncing(float centerY) {
        // Bounce heights (gradually decreasing)
        float bounce1 = centerY - 200f;
        float bounce2 = centerY - 120f;
        float bounce3 = centerY - 180f;
        float bounce4 =centerY-130f;

        // Bounce animations
        ObjectAnimator bounceUp1 = ObjectAnimator.ofFloat(ball, "translationY", centerY, bounce1);
        bounceUp1.setDuration(300);
        bounceUp1.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator bounceDown1 = ObjectAnimator.ofFloat(ball, "translationY", bounce1, centerY);
        bounceDown1.setDuration(250);
        bounceDown1.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator bounceUp2 = ObjectAnimator.ofFloat(ball, "translationY", centerY, bounce2);
        bounceUp2.setDuration(250);
        bounceUp2.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator bounceDown2 = ObjectAnimator.ofFloat(ball, "translationY", bounce2, centerY);
        bounceDown2.setDuration(200);
        bounceDown2.setInterpolator(new AccelerateInterpolator());

        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.playSequentially(bounceUp1, bounceDown1, bounceUp2, bounceDown2);
        bounceSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                expandBall();
            }

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        bounceSet.start();
    }

    private void expandBall() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float maxScreenSize = Math.max(screenWidth, screenHeight) * 2.5f; // Expand beyond screen

        // Expanding animation
        ValueAnimator expandAnimator = ValueAnimator.ofFloat(1f, maxScreenSize / ball.getWidth());
        expandAnimator.setDuration(1000);
        expandAnimator.setInterpolator(new DecelerateInterpolator());

        expandAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            ball.setScaleX(scale);
            ball.setScaleY(scale);
        });

        expandAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ball.setVisibility(View.GONE);
                bgView.setVisibility(View.VISIBLE);
                bgView.setBackgroundResource(R.drawable.gradient_bg);
                designTopLeft.setVisibility(View.VISIBLE);
                designTopRight.setVisibility(View.VISIBLE);
                designBottomLeft.setVisibility(View.VISIBLE);
                designBottomRight.setVisibility(View.VISIBLE);
                startLogoAnimation();
            }

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        expandAnimator.start();
    }

    private void startLogoAnimation() {
        // Make all elements visible at the same time
        logo.setVisibility(View.VISIBLE);
        designTopLeft.setVisibility(View.VISIBLE);
        designTopRight.setVisibility(View.VISIBLE);
        designBottomLeft.setVisibility(View.VISIBLE);
        designBottomRight.setVisibility(View.VISIBLE);
        lottieAnimation.setVisibility(View.VISIBLE);

        // Logo drop animation (only for logo, not for designs)
        ObjectAnimator logoDrop = ObjectAnimator.ofFloat(logo, "translationY", -100f, 700f);
        logoDrop.setDuration(3000);
        logoDrop.setInterpolator(new DecelerateInterpolator());

        // Start animation for logo only
        logoDrop.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        });

        logoDrop.start();
    }

}
