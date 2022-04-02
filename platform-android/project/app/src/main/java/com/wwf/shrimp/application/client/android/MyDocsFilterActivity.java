package com.wwf.shrimp.application.client.android;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.viethoa.RecyclerViewFastScroller;
import com.wwf.shrimp.application.client.android.R;
import com.wwf.shrimp.application.client.android.models.view.FilterDTO;
import com.wwf.shrimp.application.client.android.system.SessionData;

public class MyDocsFilterActivity extends AppCompatActivity {

    public static final int MY_DOCUMENTS_FILTER_ACTIVITY_ID = 7;

    //
    // UI Elements
    Button buttonCancelFilter;
    Button buttonSetFilter;
    Button buttonClearFilter;

    // global session data
    private SessionData globalVariable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydocs_filter);

        //
        // get session data
        globalVariable  = (SessionData) getApplicationContext();

        //
        // set UI elements
        buttonCancelFilter = (Button)findViewById(R.id.buttonCancelMyDocsFilter);
        buttonSetFilter = (Button)findViewById(R.id.buttonSetMyDocsFilter);
        buttonClearFilter = (Button)findViewById(R.id.buttonClearMyDocsFilter);

        //
        // Dialog Button Handlers
        buttonCancelFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // cancel the operation by going back
                onBackPressed();
            }
        });
        buttonSetFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalVariable.getMyDocsFilter().setStatus(FilterDTO.FILTER_STATUS_SET);
                // cancel the operation by going back
                onBackPressed();
            }
        });
        buttonClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                globalVariable.getMyDocsFilter().setStatus(FilterDTO.FILTER_STATUS_CLEAR);
                // cancel the operation by going back
                onBackPressed();
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
