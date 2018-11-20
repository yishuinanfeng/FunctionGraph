package com.example.yanyinan.graphdemo.calculate;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * 创建时间： 2018/11/19
 * 作者：yanyinan
 * 功能描述：
 */
public class CalculateParser {
    private static final String TAG = CalculateParser.class.getSimpleName();

    public static final float
            PLUS = 1, MINUS = 2,FIRST_PRIORITY = 3, TIMES = 3,
            DIVIDE = 4, POWER = 5,
            SIN = 6, COS = 7,
            LEFT_BRACKET = 8, RIGHT_BRACKET = 9;

    private static final ArrayList<String> OPERATOR = new ArrayList<>();

    public static final String UNKNOWN_VARIABLE = "x";

    static {
        OPERATOR.add("(");
        OPERATOR.add(")");
        OPERATOR.add("+");
        OPERATOR.add("-");
        OPERATOR.add("*");
        OPERATOR.add("/");
        OPERATOR.add("^");
        OPERATOR.add("sin");
        OPERATOR.add("cos");
    }

    //这个函数的作用就是使用空格分割字符串，以便后面使用分割函数使得将字符串分割成数组
    public static ParseResult parseFormula(String formula) {

        ParseResult parseResult = new ParseResult();

        String numAndOperators[] = getSplitArray(formula);

        for (String str : numAndOperators) {
            Log.d(TAG + "numAndOperators: ", str);

        }


        for (String str : numAndOperators) {
            if (TextUtils.isEmpty(str)) {
                continue;
            }

            if (OPERATOR.contains(str)) {
                parseResult.addOperator(getOperatorCode(str));
            } else if (UNKNOWN_VARIABLE.equals(str)) {
                parseResult.addUnknownVariable();
            } else {
                parseResult.addNum(Float.parseFloat(str));
            }
        }

        return parseResult;
    }

    //这个函数的作用就是使用空格分割字符串，以便后面使用分割函数使得将字符串分割成数组
    private static String[] getSplitArray(String s) {
        s = s.replaceAll(" +", "");
        StringBuilder sb = new StringBuilder();
        //第一位是“-”，补“0”
        if (s.charAt(0) == '-') {
            sb.append("0 ");
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(' || s.charAt(i) == ')' ||
                    s.charAt(i) == '+' ||
                    //“s.charAt(i - 1) != '('”是为了使得（-1）的负号和数字不分开
                    (i > 0 && s.charAt(i) == '-' && s.charAt(i - 1) != '(')
                    || s.charAt(i) == '*'
                    || s.charAt(i) == '/'
                    || s.charAt(i) == '^') {

                sb.append(" ").append(s.charAt(i)).append(" ");
            } else {
                //未知数和数字
                sb.append(s.charAt(i));
            }
        }
        String result = sb.toString();
        result = result.replace("sin", " sin ");
        result = result.replace("cos", " cos ");
        //  result = result.replace("tan", " " + String.valueOf(TAN) + " "); //tan不是函数（非连续）
        return result.split(" +");
    }

    private static Float getOperatorCode(String operator) {
        switch (operator) {
            case "(":
                return LEFT_BRACKET;
            case ")":
                return RIGHT_BRACKET;
            case "+":
                return PLUS;
            case "-":
                return MINUS;
            case "*":
                return TIMES;
            case "/":
                return DIVIDE;
            case "^":
                return POWER;
            case "sin":
                return SIN;
            case "cos":
                return COS;
            default:
                throw new RuntimeException("Not support operator!");
        }
    }
}
