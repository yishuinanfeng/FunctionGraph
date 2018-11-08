package com.example.yanyinan.graphdemo.calculate;

import android.util.Log;

import java.util.Stack;

/**
 * 创建时间： 2018/11/8
 * 作者：yanyinan
 * 功能描述：对输入的字符串进行计算
 */
public class StringCalculator {
    private static final String TAG = StringCalculator.class.getSimpleName();

    //这个函数的作用就是使用空格分割字符串，以便后面使用分割函数使得将字符串分割成数组
    public static String insetBlanks(String s) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(' || s.charAt(i) == ')' ||
                    s.charAt(i) == '+' || (i > 0 && s.charAt(i) == '-' && s.charAt(i - 1) != '(')
                    || s.charAt(i) == '*' || s.charAt(i) == '/')
                result += " " + s.charAt(i) + " ";
            else
                result += s.charAt(i);
        }
        return result;
    }

    public static double evaluateExpression(String expression) {
        Stack<Double> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        //   expression = insetBlanks(expression);

        Log.d(TAG + "calculate expression: ", expression);

        long a = System.nanoTime();

        String[] tokens = expression.split(" ");

        Log.d(TAG + "calculate split time: ", System.nanoTime() - a + "");


        long t = System.nanoTime();

        for (String token : tokens) {
            if (token.length() == 0) {
                //如果是空格的话就继续循环，什么也不操作
                continue;
            }
            //如果是加减的话，因为加减的优先级最低，因此这里的只要遇到加减号，无论操作符栈中的是什么运算符都要运算
            else if (token.equals("+") || token.equals("-")) {
                //当栈不是空的，并且栈中最上面的一个元素是加减乘除的人任意一个
                while (!operatorStack.isEmpty() && (operatorStack.peek() == '-' || operatorStack.peek() == '+' || operatorStack.peek() == '/' || operatorStack.peek() == '*')) {
                    processAnOperator(operandStack, operatorStack);   //开始运算
                }
                operatorStack.push(token.charAt(0));   //运算完之后将当前的运算符入栈
            }
            //当前运算符是乘除的时候，因为优先级高于加减，因此要判断最上面的是否是乘除，如果是乘除就运算，否则的话直接入栈
            else if (token.equals("*") || token.equals("/")) {
                while (!operatorStack.isEmpty() && (operatorStack.peek() == '/' || operatorStack.peek() == '*')) {
                    processAnOperator(operandStack, operatorStack);
                }
                operatorStack.push(token.charAt(0));   //将当前操作符入栈
            }
            //如果是左括号的话直接入栈，什么也不用操作,trim()函数是用来去除空格的，由于上面的分割操作可能会令操作符带有空格
            else if (token.trim().equals("(")) {
                operatorStack.push('(');
            }
            //如果是右括号的话，清除栈中的运算符直至左括号
            else if (token.trim().equals(")")) {
                while (operatorStack.peek() != '(') {
                    processAnOperator(operandStack, operatorStack);    //开始运算
                }
                operatorStack.pop();   //这里的是运算完之后清除左括号
            }
            //这里如果是数字的话直接入数据的栈
            else {
                operandStack.push(Double.parseDouble(token));   //将数字字符串转换成数字然后压入栈中
            }
        }
        //最后当栈中不是空的时候继续运算，知道栈中为空即可
        while (!operatorStack.isEmpty()) {
            processAnOperator(operandStack, operatorStack);
        }

        Log.d(TAG + "calculate stack time:", System.nanoTime() - t + "");

        return operandStack.pop();    //此时数据栈中的数据就是运算的结果
    }

    //这个函数的作用就是处理栈中的两个数据，然后将栈中的两个数据运算之后将结果存储在栈中
    public static void processAnOperator(Stack<Double> operandStack, Stack<Character> operatorStack) {

        long t = System.nanoTime();

        char op = operatorStack.pop();  //弹出一个操作符
        double op1 = operandStack.pop();  //从存储数据的栈中弹出连个两个数用来和操作符op运算
        double op2 = operandStack.pop();
        if (op == '+')  //如果操作符为+就执行加运算
            operandStack.push(op1 + op2);
        else if (op == '-')
            operandStack.push(op2 - op1);   //因为这个是栈的结构，自然是上面的数字是后面的，因此用op2-op1
        else if (op == '*')
            operandStack.push(op1 * op2);
        else if (op == '/')
            operandStack.push(op2 / op1);


        Log.d(TAG + "calculate processAnOperator time:", System.nanoTime() - t + "");
    }

}
