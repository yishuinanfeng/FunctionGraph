package com.example.yanyinan.graphdemo.calculate;

import android.util.Log;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 创建时间： 2018/11/8
 * 作者：yanyinan
 * 功能描述：对输入的字符串进行计算
 */
public class StringCalculator {
    private static final String TAG = StringCalculator.class.getSimpleName();

    private static final char PLUS = '+';
    private static final char MINUS = '-';
    private static final char TIME = '*';
    private static final char DIVIDE = '/';
    private static final char POWER = '^';
    private static final char LEFT_BRACKET = '(';
    private static final char RIGHT_BRACKET = ')';
//    //替换三角函数的操作符
//    private static final char SIN = 's';
//    private static final char COS = 'c';
    // private static final char TAN = 't';

    private static Stack<Float> numStack = new Stack<>();
    private static Stack<Float> operatorStack = new Stack<>();
    //三角函数
    private static ArrayList<Float> triSymbolList = new ArrayList<>();

    static {
        triSymbolList.add(CalculateParser.SIN);
        triSymbolList.add(CalculateParser.COS);
        //    triSymbolList.add(TAN);
    }

//    //这个函数的作用就是使用空格分割字符串，以便后面使用分割函数使得将字符串分割成数组
//    public static String insetBlanks(String s) {
//        s = s.replace(" +","");
//        StringBuilder sb = new StringBuilder();
//        //第一位是“-”，补“0”
//        if (s.charAt(0) == '-'){
//            sb.append("0 ");
//        }
//
//        for (int i = 0; i < s.length(); i++) {
//            if (s.charAt(i) == '(' || s.charAt(i) == ')' ||
//                    s.charAt(i) == '+' ||
//                    //“s.charAt(i - 1) != '('”是为了使得（-1）的负号和数字不分开
//                    (i > 0 && s.charAt(i) == '-' && s.charAt(i - 1) != '(')
//                    || s.charAt(i) == '*'
//                    || s.charAt(i) == '/'
//                    || s.charAt(i) == POWER) {
//
//                sb.append(" ").append(s.charAt(i)).append(" ");
//            } else {
//                sb.append(s.charAt(i));
//            }
//        }
//        String result = sb.toString();
//        result = result.replace("sin", " " + String.valueOf(SIN) + " ");
//        result = result.replace("cos", " " + String.valueOf(COS) + " ");
//      //  result = result.replace("tan", " " + String.valueOf(TAN) + " "); //tan不是函数（非连续）
//        return result;
//    }

//    /**
//     * 遍历到计算符号，如果当前的符号的优先级小于等于操作符栈顶符号，则运算，大于则入栈
//     *
//     * @param expression
//     * @return
//     */
//    public static float evaluateExpression(String expression) {
//        numStack.clear();
//        operatorStack.clear();
//        //   expression = insetBlanks(expression);
//
//        Log.d(TAG + "calculate expression: ", expression);
//
//        long a = System.nanoTime();
//
//        String[] tokens = expression.split(" ");
//
//        Log.d(TAG + "calculate split time: ", System.nanoTime() - a + "");
//
//
//        long t = System.nanoTime();
//
//        for (String token : tokens) {
//            if (token.length() == 0) {
//                //如果是空格的话就继续循环，什么也不操作
//                continue;
//            }
//            //如果是加减的话，因为加减的优先级最低，因此这里的只要遇到加减号，无论操作符栈中的是什么运算符都要运算
//            else if (token.equals("+") || token.equals("-")) {
//                //当栈不是空的，并且栈中最上面的一个元素是加减乘除的人任意一个
//                while (!operatorStack.isEmpty() && (operatorStack.peek() == '-' || operatorStack.peek() == '+'
//                        || operatorStack.peek() == '/' || operatorStack.peek() == '*' || operatorStack.peek() == '^'
//                        || triSymbolList.contains(operatorStack.peek()))) {
//                    processAnOperator(numStack, operatorStack);   //开始运算
//                }
//                operatorStack.push(token.charAt(0));   //运算完之后将当前的运算符入栈
//            }
//            //当前运算符是乘除的时候，因为优先级高于加减，因此要判断最上面的是否是乘除，如果是乘除就运算，否则的话直接入栈
//            else if (token.equals("*") || token.equals("/")) {
//                while (!operatorStack.isEmpty() && (operatorStack.peek() == '/' || operatorStack.peek() == '*'
//                        || operatorStack.peek() == '^' || triSymbolList.contains(operatorStack.peek()))) {
//                    processAnOperator(numStack, operatorStack);
//                }
//                operatorStack.push(token.charAt(0));   //将当前操作符入栈
//            }
//
//            //如果是左括号的话直接入栈，什么也不用操作,trim()函数是用来去除空格的，由于上面的分割操作可能会令操作符带有空格
//            else if (token.trim().equals("(")) {
//                operatorStack.push('(');
//            }
//            //如果是右括号的话，清除栈中的运算符直至左括号
//            else if (token.trim().equals(")")) {
//                while (operatorStack.peek() != '(') {
//                    processAnOperator(numStack, operatorStack);    //开始运算
//                }
//                operatorStack.pop();   //这里的是运算完之后清除左括号
//            } else if (token.trim().equals("^")) {
//                operatorStack.push('^');
//
//            }
//            //三角函数符号
//            else if (triSymbolList.contains(token.trim().charAt(0))) {
//                operatorStack.push(token.trim().charAt(0));
//            }
//            //这里如果是数字的话直接入数据的栈
//            else {
//                numStack.push(Float.parseFloat(token));   //将数字字符串转换成数字然后压入栈中
//            }
//        }
//        //最后当栈中不是空的时候继续运算，知道栈中为空即可
//        while (!operatorStack.isEmpty()) {
//            processAnOperator(numStack, operatorStack);
//        }
//
//        Log.d(TAG + "calculate stack time:", System.nanoTime() - t + "");
//
//        return numStack.pop();    //此时数据栈中的数据就是运算的结果
//    }

    /**
     * 遍历到计算符号，如果当前的符号的优先级小于等于操作符栈顶符号，则运算，大于则入栈
     *
     * @param parseResult
     * @return
     */
    public static float evaluateExpression(ParseResult parseResult) {
        numStack.clear();
        operatorStack.clear();
        //   expression = insetBlanks(expression);

      //  Log.d(TAG + "calculate expression: ", parseResult.toString());

        long t = System.nanoTime();

        float[] formulas = parseResult.getFormulaList();

        int size = formulas.length;
        for (int i = 0; i < size; i++) {
            float token = formulas[i];

            if (parseResult.isOperator(i)) {
                handleOperator(token);
            } else {
                //这里如果是数字的话直接入数据的栈
                numStack.push(token);   //将数字字符串转换成数字然后压入栈中
            }

        }

        //最后当栈中不是空的时候继续运算，知道栈中为空即可
        while (!operatorStack.isEmpty()) {
            processAnOperator(numStack, operatorStack);
        }

        Log.d(TAG + "calculate stack time:", System.nanoTime() - t + "");

        return numStack.pop();    //此时数据栈中的数据就是运算的结果
    }

    private static void handleOperator(float token) {
        //如果是加减的话，因为加减的优先级最低，因此这里的只要遇到加减号，无论操作符栈中的是什么运算符都要运算
        if (token < CalculateParser.FIRST_PRIORITY) {
            //当栈不是空的，并且栈中最上面的一个元素是加减乘除的人任意一个
            while (!operatorStack.isEmpty()) {
                processAnOperator(numStack, operatorStack);   //开始运算
            }
            operatorStack.push(token);   //运算完之后将当前的运算符入栈
        }
        //当前运算符是乘除的时候，因为优先级高于加减，因此要判断最上面的是否是乘除，如果是乘除就运算，否则的话直接入栈
        else if (token == CalculateParser.TIMES || token == CalculateParser.DIVIDE) {
            while (!operatorStack.isEmpty() && operatorStack.peek() >= CalculateParser.FIRST_PRIORITY) {
                processAnOperator(numStack, operatorStack);
            }
            operatorStack.push(token);   //将当前操作符入栈
        }

//        //如果是左括号的话直接入栈，什么也不用操作,trim()函数是用来去除空格的，由于上面的分割操作可能会令操作符带有空格
//        else if (token == CalculateParser.LEFT_BRACKET) {
//            operatorStack.push(token);
//        }
        //如果是右括号的话，清除栈中的运算符直至左括号
        else if (token == CalculateParser.RIGHT_BRACKET) {
            while (operatorStack.peek() != CalculateParser.LEFT_BRACKET) {
                processAnOperator(numStack, operatorStack);    //开始运算
            }
            operatorStack.pop();   //这里的是运算完之后清除左括号
        }
//        else if (token == CalculateParser.POWER) {
//            operatorStack.push(token);
//        }
        else {
            operatorStack.push(token);
        }
        //三角函数符号
//        else if (triSymbolList.contains(token)) {
//            operatorStack.push(token);
//        }
    }

    //这个函数的作用就是处理栈中的两个数据，然后将栈中的两个数据运算之后将结果存储在栈中
    private static void processAnOperator(Stack<Float> numStack, Stack<Float> operatorStack) {

        long t = System.nanoTime();

        float op = operatorStack.pop();  //弹出一个操作符
        float op1 = numStack.pop();
        float op2 = 0;//从存储数据的栈中弹出连个两个数用来和操作符op运算
        if (!triSymbolList.contains(op)) {
            op2 = numStack.pop();
        }

        Log.d(TAG + "processAnOperator:", "op2 " + op2 + " op " + op + " op1 " + op1);

        if (op == CalculateParser.PLUS)  //如果操作符为+就执行加运算
        {
            numStack.push(op1 + op2);
        } else if (op == CalculateParser.MINUS) {
            numStack.push(op2 - op1);   //因为这个是栈的结构，自然是上面的数字是后面的，因此用op2-op1
        } else if (op == CalculateParser.TIMES) {
            numStack.push(op1 * op2);

        } else if (op == CalculateParser.DIVIDE) {
            numStack.push(op2 / op1);

        } else if (op == CalculateParser.POWER) {
            numStack.push((float) Math.pow(op2, op1));
        } else if (op == CalculateParser.SIN) {
            numStack.push((float) Math.sin(op1));
        } else if (op == CalculateParser.COS) {
            numStack.push((float) Math.cos(op1));
        }
//        else if (op == TAN) {
//            numStack.push((float)Math.tan(op1));
//        }


        Log.d(TAG + "calculate processAnOperator time:", System.nanoTime() - t + "");
    }

}
