package com.example.yanyinan.graphdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yanyinan.graphdemo.util.DisplayUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float screenHeightWidthRatio = DisplayUtil.getScreenHeightWidthRatio(this);
        setContentView(new FunctionGraph(this,screenHeightWidthRatio));
    }
}
