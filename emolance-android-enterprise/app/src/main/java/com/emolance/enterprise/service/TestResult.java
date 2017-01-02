package com.emolance.enterprise.service;

/**
 * Created by yusun on 4/10/16.
 */
public class TestResult {

    private static final int MAX_VALUE = 10315700;
    private static final int BASE_VALUE = 6717988;

    private int ST = 0; // cortisol
    private int SO = 0; // white-background
    private int SC = 0; // DHEA

    public TestResult(int ST, int SO, int SC){
        this.SC = SC;
        this.SO = SO;
        this.ST = ST;
    }

    public TestResult() {

    }

    public int getST() {
        return ST;
    }

    public void setST(int sT) {
        ST = sT;
    }

    public int getSO() {
        return SO;
    }

    public void setSO(int sO) {
        SO = sO;
    }

    public int getSC() {
        return SC;
    }

    public void setSC(int sC) {
        SC = sC;
    }

    public double getCotisol() {
        return (double) ST / (double) SO;
    }

    public double getDHEA() {
        return (double) SC / (double) SO;
    }

    public double getScaledCortisol() {
        double rt = getCotisol();
//        if (rt > 0.82) {
//            return 0.99;
//        }
//        if (rt > 0.8) {
//            return 0.9;
//        }
//        if (rt < 0.6) {
//            return 0.05;
//        }
//        if (rt < 0.68) {
//            return 0.11;
//        }
//        double level = 0.007;
//        rt = (rt - 0.70) / level / 10;
        if (rt >= 0.9) rt = 0.9;
        if (rt <= 0.1) rt = 0.1;
        return rt;
    }

    public double getScaledDHEA() {
        double rt = getDHEA();
        if (rt >= 0.9) rt = 0.9;
        if (rt <= 0.1) rt = 0.1;
        return rt;
    }
}