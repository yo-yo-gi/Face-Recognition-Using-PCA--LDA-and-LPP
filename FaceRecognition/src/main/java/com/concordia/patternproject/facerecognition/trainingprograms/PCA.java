package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.EigenvalueDecompose;
import com.concordia.patternproject.facerecognition.corelogic.Mat;

import java.util.ArrayList;
import java.util.Arrays;


public class PCA extends Extraction {

	public PCA(ArrayList<Mat> trainingSet, ArrayList<String> labels,
			   int numOfComponents) throws Exception {

		if(numOfComponents >= trainingSet.size()){
			throw new Exception("the expected dimensions could not be achieved!");
		}
		
		this.trainingSet = trainingSet;
		this.labels = labels;
		this.numOfComponents = numOfComponents;

		this.meanMat = getMean(this.trainingSet);
		this.W = getFeature(this.trainingSet, this.numOfComponents);

		// Construct projectedTrainingMatrix
		this.projectedTrainingSet = new ArrayList<ProjectedTrainingMat>();
		for (int i = 0; i < trainingSet.size(); i++) {
			ProjectedTrainingMat ptm = new ProjectedTrainingMat(this.W
					.transpose().times(trainingSet.get(i).diff(meanMat)),
					labels.get(i));
			this.projectedTrainingSet.add(ptm);
		}
	}

	// extract features, namely W
	private Mat getFeature(ArrayList<Mat> input, int K) {
		int i, j;

		int row = input.get(0).getRowDim();
		int column = input.size();
		Mat X = new Mat(row, column);

		for (i = 0; i < column; i++) {
			X.setMat(0, row - 1, i, i, input.get(i).diff(this.meanMat));
		}

		// get eigenvalues and eigenvectors
		Mat XT = X.transpose();
		Mat XTX = XT.times(X);
		EigenvalueDecompose feature = XTX.eig();
		double[] d = feature.getd();

		assert d.length >= K : "number of eigenvalues is less than K";
		int[] indexes = this.getIndexesOfKEigenvalues(d, K);

		Mat eigenVectors = X.times(feature.getV());
		Mat selectedEigenVectors = eigenVectors.getMat(0,
				eigenVectors.getRowDim() - 1, indexes);

		// normalize the eigenvectors
		row = selectedEigenVectors.getRowDim();
		column = selectedEigenVectors.getColDim();
		for (i = 0; i < column; i++) {
			double temp = 0;
			for (j = 0; j < row; j++)
				temp += Math.pow(selectedEigenVectors.get(j, i), 2);
			temp = Math.sqrt(temp);

			for (j = 0; j < row; j++) {
				selectedEigenVectors.set(j, i, selectedEigenVectors.get(j, i) / temp);
			}
		}

		return selectedEigenVectors;

	}

	// get the first K indexes with the highest eigenValues
	private class mix implements Comparable {
		int index;
		double value;

		mix(int i, double v) {
			index = i;
			value = v;
		}

		public int compareTo(Object o) {
			double target = ((mix) o).value;
			if (value > target)
				return -1;
			else if (value < target)
				return 1;

			return 0;
		}
	}

	private int[] getIndexesOfKEigenvalues(double[] d, int k) {
		mix[] mixes = new mix[d.length];
		int i;
		for (i = 0; i < d.length; i++)
			mixes[i] = new mix(i, d[i]);

		Arrays.sort(mixes);

		int[] result = new int[k];
		for (i = 0; i < k; i++)
			result[i] = mixes[i].index;
		return result;
	}

	// The matrix has already been vectorized
	private static Mat getMean(ArrayList<Mat> input) {
		int rows = input.get(0).getRowDim();
		int length = input.size();
		Mat all = new Mat(rows, 1);

		for (int i = 0; i < length; i++) {
			all.addEquals(input.get(i));
		}

		return all.times((double) 1 / length);
	}

	@Override
	public Mat getW() {
		return this.W;
	}

	@Override
	public ArrayList<ProjectedTrainingMat> getProjectedTrainingSet() {
		return this.projectedTrainingSet;
	}
	
	@Override
	public Mat getAverageMat() {
		// TODO Auto-generated method stub
		return meanMat;
	}

	@Override
	public int addFace(Mat face, String label) {
		// to be done
		return 0;
	}

	public ArrayList<Mat> getTrainingSet(){
		return this.trainingSet;
	}
	
	public Mat reconstruct(int whichImage, int dimensions) throws Exception{
		if(dimensions > this.numOfComponents)
			throw new Exception("dimensions should be smaller than the number of components");
		
		Mat afterPCA = this.projectedTrainingSet.get(whichImage).mat.getMat(0, dimensions-1, 0, 0);
		Mat eigen = this.getW().getMat(0, 10304-1, 0, dimensions - 1);

		return eigen.times(afterPCA).add(this.getAverageMat());
		
	}

}
