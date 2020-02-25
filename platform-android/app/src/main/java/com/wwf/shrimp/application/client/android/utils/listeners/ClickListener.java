package com.wwf.shrimp.application.client.android.utils.listeners;

import android.view.View;

/**
 * Simple interface defined for clicking actions
 * @author AleaActaEst
 */
public interface ClickListener {
    void onClick(View view,int position);
    void onLongClick(View view, int position);
}
