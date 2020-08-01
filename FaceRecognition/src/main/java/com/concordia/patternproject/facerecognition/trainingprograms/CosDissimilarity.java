package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

public class CosDissimilarity implements Metric {

	@Override
	public double calcDist(Mat x, Mat y) 
	{
		assert x.getRowDim() == y.getRowDim();
		int s = x.getRowDim();
		double cos, SNormal, ENormal, se;
		int i;

		
		se = 0;
		for (i = 0; i < s; i++) {
			se += x.get(i, 0) * y.get(i, 0);
		}

		
		SNormal = 0;
		for (i = 0; i < s; i++) 
		{
			SNormal = SNormal +  Math.pow(x.get(i, 0), 2);
		}
		SNormal = Math.sqrt(SNormal);

		
		ENormal = 0;
		for (i = 0; i < s; i++)
		{
			ENormal =ENormal + Math.pow(y.get(i, 0), 2);
		}
		ENormal = Math.sqrt(ENormal);
		
		if(0 > se)
			se = 0 - se;
		
		cos = se / (ENormal * SNormal);

		
		if (cos == 0.0)
			return Double.MAX_VALUE;
		
		return 1 / cos;
	}

}
