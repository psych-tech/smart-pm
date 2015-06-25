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
    	return ((double) SO - (double) ST) / ((double) MAX_VALUE - (double) SO);
    }

    public double getScaledRC() {
    	return ((double) SO - (double) SC) / ((double) MAX_VALUE - (double) SO);
    }
}
