package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

import java.util.ArrayList;


public abstract class Extraction {
	
	
	int numOfComponents;
	ArrayList<ProjectedTrainingMat> projectedTrainingSet;

	ArrayList<Mat> trainingSet;
	ArrayList<String> labels;
	
	
	Mat meanMat;
	
	Mat W;
	
	
	public abstract Mat getW();

	public abstract ArrayList<ProjectedTrainingMat> getProjectedTrainingSet();

	public abstract Mat getAverageMat();

	public abstract int addFace(Mat face, String label);
}
