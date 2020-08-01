package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.EigenvalueDecompose;
import com.concordia.patternproject.facerecognition.corelogic.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class LPP extends Extraction {
    private PCA pca;

    public LPP(ArrayList<Mat> trainingSet, ArrayList<String> labels, int numOfComponents) throws Exception {
        int n = trainingSet.size(); // sample size
        Set<String> tempSet = new HashSet<String>(labels);
        int c = tempSet.size(); // class size

        // process in PCA
        this.pca = new PCA(trainingSet, labels, numOfComponents);

        //construct the nearest neighbor graph
        Mat S = constructNearestNeighborGraph(pca.projectedTrainingSet);
        Mat D = constructD(S);
        Mat L = D.diff(S);

        //reconstruct the trainingSet into required X;
        Mat X = constructTrainingMatrix(pca.getProjectedTrainingSet());
        Mat XLXT = X.times(L).times(X.transpose());
        Mat XDXT = X.times(D).times(X.transpose());

        //calculate the eignevalues and eigenvectors of (XDXT)^-1 * (XLXT)
        Mat targetForEigen = XDXT.inverse().times(XLXT);
        EigenvalueDecompose feature = targetForEigen.eig();

        double[] d = feature.getd();
        assert d.length >= c - 1 : "Ensure that the number of eigenvalues is larger than c - 1";
        int[] indexes = getIndexesOfKEigenvalues(d, d.length);

        Mat eigenVectors = feature.getV();
        Mat selectedEigenVectors = eigenVectors.getMat(0, eigenVectors.getRowDim() - 1, indexes);

        this.W = pca.getW().times(selectedEigenVectors);

        //Construct projectedTrainingMatrix
        this.projectedTrainingSet = new ArrayList<ProjectedTrainingMat>();
        for (int i = 0; i < trainingSet.size(); i++) {
            ProjectedTrainingMat ptm = new ProjectedTrainingMat(this.W.transpose().times(trainingSet.get(i).diff(pca.meanMat)), labels.get(i));
            this.projectedTrainingSet.add(ptm);
        }
        this.meanMat = pca.meanMat;
    }

    private Mat constructNearestNeighborGraph(ArrayList<ProjectedTrainingMat> input) {
        int size = input.size();
        Mat S = new Mat(size, size);

        Metric Euclidean = new EuclidDistanceCalculation();
        ProjectedTrainingMat[] trainArray = input.toArray(new ProjectedTrainingMat[input.size()]);

        for (int i = 0; i < size; i++) {
            ProjectedTrainingMat[] neighbors = KNN.findKNN(trainArray, input.get(i).mat, 3, Euclidean);
            for (int j = 0; j < neighbors.length; j++) {
                if (!neighbors[j].equals(input.get(i))) {
//					double distance = Euclidean.getDistance(neighbors[j].matrix, input.get(i).matrix);
//					double weight = Math.exp(0-distance*distance / 2);
                    int index = input.indexOf(neighbors[j]);
                    S.set(i, index, 1);
                    S.set(index, i, 1);
                }
            }

//			for(int j = 0; j < size; j ++){
//				if( i != j && input.get(i).label.equals(input.get(j).label)){
//					S.set(i, j, 1);
//				}
//			}
        }
        return S;
    }

    private Mat constructD(Mat S) {
        int size = S.getRowDim();
        Mat D = new Mat(size, size);

        for (int i = 0; i < size; i++) {
            double temp = 0;
            for (int j = 0; j < size; j++) {
                temp += S.get(j, i);
            }
            D.set(i, i, temp);
        }

        return D;
    }

    private Mat constructTrainingMatrix(ArrayList<ProjectedTrainingMat> input) {
        int row = input.get(0).mat.getRowDim();
        int column = input.size();
        Mat X = new Mat(row, column);

        for (int i = 0; i < column; i++) {
            X.setMat(0, row - 1, i, i, input.get(i).mat);
        }

        return X;
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
        return this.meanMat;
    }

    @Override
    public int addFace(Mat face, String label) {
        ProjectedTrainingMat projectedTrainingMatrix = new ProjectedTrainingMat(this.W.transpose().times(face.diff(pca.meanMat)), label);
        this.projectedTrainingSet.add(projectedTrainingMatrix);
        return this.projectedTrainingSet.size() - 1;
    }

}