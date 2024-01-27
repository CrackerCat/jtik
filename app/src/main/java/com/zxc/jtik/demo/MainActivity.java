package com.zxc.jtik.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.zxc.jtik.Jtik;
import com.zxc.jtik.JtikConfig;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.listview);

        findViewById(R.id.init_btn).setOnClickListener(v -> {
            JtikConfig.needHookSystemClass = true;
            Jtik.init(MainActivity.this);
        });

        String[] names = new String[TestCase.sTestItems.length];
        for (int i = 0; i < TestCase.sTestItems.length; i++) {
            names[i] = TestCase.sTestItems[i].getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item,
                R.id.list_item_name, names);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TestCase.sTestItems[position].run();
        });
    }
}