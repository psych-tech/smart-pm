package com.emolance.service;

public class Node {

	private static final int MAX_VALUE = 10315700;
	private static final int BASE_VALUE = 6717988;

    private int ST = 0;
    private int SO = 0;
    private int SC = 0;

    public Node(int ST, int SO, int SC){
        this.SC = SC;
        this.SO = SO;
        this.ST = ST;
    }

    public Node() {

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

	public double getRT() {
    	return (double) ST / (double) SO;
    }

    public double getRC() {
    	return (double) SC / (double) SO;
    }

    public double getScaledRT() {
    	double rt = getRT();
    	if (rt > 0.82) {
    		return 0.99;
    	}
    	if (rt > 0.8) {
    		return 0.9;
    	}
    	if (rt < 0.6) {
    		return 0.05;
    	}
    	if (rt < 0.68) {
    		return 0.11;
    	}
    	double level = 0.01;
    	rt = (rt - 0.68) / level / 10;
    	if (rt >= 0.9) rt = 0.9;
    	if (rt <= 0.1) rt = 0.1;
    	return rt;
    }

    public double getScaledRC() {
    	return ((double) SO - (double) SC) / ((double) MAX_VALUE - (double) SO);
    }
}
