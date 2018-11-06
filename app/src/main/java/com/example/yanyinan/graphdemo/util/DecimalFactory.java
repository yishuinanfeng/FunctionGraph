package com.example.yanyinan.graphdemo.util;

import java.math.MathContext;

/**
 * 创建时间： 2018/11/6
 * 作者：yanyinan
 * 功能描述：
 */
public class DecimalFactory {

    /**
     * round decimal value
     *
     * @param val - input
     * @param i   - number of decimal
     * @return - String
     */
    public static String round(double val, int i) {
        java.math.BigDecimal bigDecimal = new java.math.BigDecimal(val);
        String res = bigDecimal.round(new MathContext(i + 1)).toString();
        return res;
    }
}
