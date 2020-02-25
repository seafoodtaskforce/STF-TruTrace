package com.wwf.shrimp.application.client.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;
import com.wwf.shrimp.application.client.android.notifications.NotificationService;

/**
 * Splash Activity for the application.
 * @author AleaActaEst
 */
public class SplashActivity extends AwesomeSplash {

    @Override
    /**
     * Initialization routine for the splash activity
     */
    public void initSplash(ConfigSplash configSplash) {

		/*
		 * We don't have to override every property
		 */

       //Customize Circular Reveal
        // configSplash.setBackgroundColor(R.color.purple_background); //any color you want form colors.xml

        configSplash.setBackgroundColor(R.color.light_grey_custom);
        configSplash.setAnimCircularRevealDuration(1500); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        //Customize Logo
        //  configSplash.setLogoSplash(R.drawable.shrimp); //or any other drawable
        configSplash.setLogoSplash(R.drawable.truetrace_logo_1200_1200);
        configSplash.setAnimLogoSplashDuration(1000); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.FadeIn); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)


        //Customize Title
        configSplash.setTitleSplash("");
        configSplash.setTitleTextColor(R.color.login_hint_highlight);
        configSplash.setTitleTextSize(30f); //float value
        configSplash.setAnimTitleDuration(1000);
        configSplash.setAnimTitleTechnique(Techniques.Pulse);
        // configSplash.setTitleFont("fonts/myfont.ttf"); //provide string to your font located in assets/fonts/

    }

    @Override
    /**
     * Notification when the animation of the splash screen is done
     */
    public void animationsFinished() {

        //
        // Jump to the login activity
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(intent);
        finish();
    }


}
