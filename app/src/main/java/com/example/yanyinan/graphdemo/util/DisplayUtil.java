package com.example.yanyinan.graphdemo.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * 创建时间： 2018/11/6
 * 作者：yanyinan
 * 功能描述：
 */
public class DisplayUtil {
    public static float spTpPx(Context context,int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }

    public static float dpTpPx(Context context,int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }
}
