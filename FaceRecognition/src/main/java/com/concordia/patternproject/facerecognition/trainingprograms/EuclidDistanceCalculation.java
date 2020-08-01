package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

public class EuclidDistanceCalculation implements Metric {

	@Override
	public double calcDist(Mat x, Mat y) 
	{
		int s = x.getRowDim();
		double sum = 0;

		for (int i = 0; i < s; i++) {
			sum = sum + Math.pow(x.get(i, 0) - y.get(i, 0), 2);
		}
		double val = Math.sqrt(sum); 
		return val;
	}

	

}
