package com.lhy.swipe;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

public class MainActivity extends Activity {


    private android.widget.ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        this.listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new myAdapter(this, Cheeses.NAMES));
    }
}
