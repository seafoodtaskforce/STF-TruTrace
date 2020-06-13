package com.wwf.shrimp.application.opennotescanner;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.piwik.sdk.PiwikApplication;
import org.piwik.sdk.Tracker;

/**
 * The Scanner Application main hub
 *
 * Created by allgood on 23/04/16.
 * Updated by AleaActaEst
 */
public class OpenNoteScannerApplication extends PiwikApplication {
    private SharedPreferences mSharedPref;
    private boolean mOptOut;

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            /**
            if (key.equals("usage_stats")) {
                mOptOut = !sharedPreferences.getBoolean("usage_stats", false);
                getPiwik().setOptOut(mOptOut);

                // when user opt-in, register the download
                if (!mOptOut) {
                    getTracker().trackAppDownload(OpenNoteScannerApplication.this, Tracker.ExtraIdentifier.APK_CHECKSUM);
                }
            }
             */
        }
    };

    @Override
    public String getTrackerUrl() {
        return "https://stats.todobom.com/";
    }

    @Override
    public Integer getSiteId() {
        return 2;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

}
