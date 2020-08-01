package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

public interface Metric {
	double calcDist(Mat a, Mat b);
}
