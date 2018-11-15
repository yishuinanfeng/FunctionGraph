package com.example.yanyinan.graphdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yanyinan.graphdemo.util.DisplayUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        float screenHeightWidthRatio = DisplayUtil.getScreenHeightWidthRatio(this);

        String a = "sin(x)";
        String b = "x^10+400";
        String c = "-(x^2+100)";
        String d = "x^5";
        String f = "sin(x) + 1000";
        String g = "cos(x) + 1000";
        String h = "tan(x)";

        setContentView(new FunctionGraph(this,screenHeightWidthRatio,f));

    }
}
