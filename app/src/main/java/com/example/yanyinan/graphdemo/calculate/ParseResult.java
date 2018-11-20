package com.example.yanyinan.graphdemo.calculate;

import java.util.ArrayList;

/**
 * 创建时间： 2018/11/19
 * 作者：yanyinan
 * 功能描述：
 */
public class ParseResult {
    /**
     * 解析得到的数字编码后的表达式
     */
    private ArrayList<Float> mFormulaList = new ArrayList<>();
    private float[] mFormulaArray;
    /**
     * 记录数字编码后的表达式中操作符的index
     */
    private ArrayList<Integer> unknownVariableIndex = new ArrayList<>();
    private int[] unknownVariableArray;
    /**
     * 记录数字编码后的表达式中未知数的index
     */
    private ArrayList<Integer> operatorIndex = new ArrayList<>();
    private int[] operatorArray;

    public void addNum(Float num) {
        mFormulaList.add(num);
    }

    public void addOperator(Float operator) {
        operatorIndex.add(mFormulaList.size());
        mFormulaList.add(operator);
    }

    public void addUnknownVariable() {
        unknownVariableIndex.add(mFormulaList.size());
        mFormulaList.add(0f);
    }

    /**
     * 将未知数替换为具体的值
     *
     * @param input
     */
    public void replaceUnknownVariable(Float input) {
        for (int index : getUnknownVariableIndex()) {
            float[] formulaArray = getFormulaList();
            formulaArray[index] = input;
        }
    }

    public int[] getUnknownVariableIndex() {
        if (unknownVariableArray == null){
            unknownVariableArray = new int[unknownVariableIndex.size()];
            for (int i = 0; i < unknownVariableIndex.size(); i++) {
                unknownVariableArray[i] = unknownVariableIndex.get(i);
            }
        }
        return unknownVariableArray;
    }

    public float[] getFormulaList() {
        if (mFormulaArray == null) {
            mFormulaArray = new float[mFormulaList.size()];
            for (int i = 0; i < mFormulaList.size(); i++) {
                mFormulaArray[i] = mFormulaList.get(i);
            }
        }
        return mFormulaArray;
    }

    public boolean isOperator(int index) {
        int[] array = getOperatorIndexArray();
        for (int anArray : array) {
            if (anArray == index) {
                return true;
            }
        }
        return false;
    }

    private int[] getOperatorIndexArray() {
        if (operatorArray == null) {
            operatorArray = new int[operatorIndex.size()];
            for (int i = 0; i < operatorIndex.size(); i++) {
                operatorArray[i] = operatorIndex.get(i);
            }

        }
        return operatorArray;
    }



    @Override
    public String toString() {
        return "ParseResult{" +
                "mFormulaList=" + mFormulaList +
                ", unknownVariableIndex=" + unknownVariableIndex +
                ", operatorIndex=" + operatorIndex +
                '}';
    }
}
