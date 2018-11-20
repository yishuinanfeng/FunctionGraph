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
        String c = "5+x";
        String d = "5+x*5";
        String f = "sin(x) + 10 + 5*x + x^2 + 5*x + x^4";
        String g = "cos(x) - 1000";
        String h = "tan(x)";
        String i = "-sin(x)";
        String j = "-sin(x) + cos(x)";
        String k = "7*(8+x)";

        setContentView(new FunctionGraph(this,screenHeightWidthRatio,f));
    }
}
