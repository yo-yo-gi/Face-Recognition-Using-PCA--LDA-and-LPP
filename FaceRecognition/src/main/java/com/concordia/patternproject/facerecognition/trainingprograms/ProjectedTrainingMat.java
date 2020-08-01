package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

public class ProjectedTrainingMat {
	Mat mat;
	String lbl;
	double dist = 0;

	public ProjectedTrainingMat(Mat m, String l) {
		this.mat = m;
		this.lbl = l;
	}
}
