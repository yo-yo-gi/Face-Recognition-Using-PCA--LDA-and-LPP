package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

public class L1Dist implements Metric {

	@Override
	public double calcDist(Mat a, Mat b) {
		int size = a.getRowDim();
		double sum = 0;

		for (int i = 0; i < size; i++) {
			sum += Math.abs(a.get(i, 0) - b.get(i, 0));
		}

		return sum;
	}

}
