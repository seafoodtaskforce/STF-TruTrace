package com.wwf.shrimp.application.client.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.daimajia.androidanimations.library.Techniques;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;
import com.wwf.shrimp.application.client.android.notifications.NotificationService;
import com.wwf.shrimp.application.client.android.system.SessionData;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Splash Activity for the application.
 * @author AleaActaEst
 */
public class SplashActivity extends AwesomeSplash {

    // global session data access
    private SessionData globalVariable = null;

    @Override
    /**
     * Initialization routine for the splash activity
     */
    public void initSplash(ConfigSplash configSplash) {

		/*
		 * We don't have to override every property
		 */

        // create access to session
        globalVariable  = (SessionData) getApplicationContext();

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
        // Jump to the login activity if the user is not "remember me" logged in

        //
        // check shared preferences
        SharedPreferences sharedPreferences;
        sharedPreferences = getDefaultSharedPreferences(this);
        String rememberMeUserName = sharedPreferences.getString(SessionData.USER_REMEMBER_ME_KEY, "");
        String userToken;
        if(!rememberMeUserName.isEmpty()){
            //
            // get the user's token
            userToken = sharedPreferences.getString(SessionData.USER_TOKEN_REMEMBER_ME_KEY, "");
            //
            // the user already exists we can log them in
            globalVariable.setRememberMeIsOn(true);

        }
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(intent);
        finish();
    }


}
