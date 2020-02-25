package com.wwf.shrimp.application.opennotescanner.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.opennotescanner.OpenNoteScannerApplication;

import us.feras.mdv.MarkdownView;

/**
 * Created by allgood on 20/02/16.
 */
public class AboutFragment extends DialogFragment {

    private static final String APP_LINK = "https://goo.gl/2JwEPq";
    private Runnable mRunOnDetach;

    public AboutFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View aboutView = inflater.inflate(R.layout.about_view, container);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return aboutView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MarkdownView markdownView = (MarkdownView) view.findViewById(R.id.about_markdown);

        markdownView.loadMarkdownFile("file:///android_asset/" + getString(R.string.about_filename));

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);

        Window window = getDialog().getWindow();
        window.setLayout( (int) (size.x*0.9) , (int) (size.y*0.9) );
        window.setGravity(Gravity.CENTER);

        View about_shareapp = view.findViewById(R.id.about_shareapp);
        about_shareapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mRunOnDetach != null) {
            mRunOnDetach.run();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // TODO Tracking
        /**
        ((OpenNoteScannerApplication) activity.getApplication()).getTracker()
                .trackScreenView("/about", "About Dialog");
         */

    }


    public void setRunOnDetach( Runnable runOnDetach ) {
        mRunOnDetach = runOnDetach;
    }
}
