package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.EigenvalueDecompose;
import com.concordia.patternproject.facerecognition.corelogic.Mat;

import java.util.*;


public class LDA extends Extraction {

    public LDA(ArrayList<Mat> trainingSet, ArrayList<String> labels,
               int numOfComponent) throws Exception {
        int n = trainingSet.size(); // sample size
        Set<String> temporarySet = new HashSet<String>(labels);
        int c = temporarySet.size(); // class size
        assert numOfComponent >= n - c : "the input components is smaller than n - c!";
        assert n >= 2 * c : "n is smaller than 2c!";

        PCA pca = new PCA(trainingSet, labels, n - c);

        Mat meanTotal = new Mat(n - c, 1);

        HashMap<String, ArrayList<Mat>> map = new HashMap<String, ArrayList<Mat>>();
        ArrayList<ProjectedTrainingMat> pcaTrain = pca
                .getProjectedTrainingSet();
        for (int i = 0; i < pcaTrain.size(); i++) {
            String key = pcaTrain.get(i).lbl;
            meanTotal.addEquals(pcaTrain.get(i).mat);
            if (!map.containsKey(key)) {
                ArrayList<Mat> temp = new ArrayList<Mat>();
                temp.add(pcaTrain.get(i).mat);
                map.put(key, temp);
            } else {
                ArrayList<Mat> temp = map.get(key);
                temp.add(pcaTrain.get(i).mat);
                map.put(key, temp);
            }
        }
        meanTotal.times((double) 1 / n);

        // calculate Sw, Sb
        Mat Sw = new Mat(n - c, n - c);
        Mat Sb = new Mat(n - c, n - c);

        temporarySet = map.keySet();
        Iterator<String> it = temporarySet.iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            ArrayList<Mat> matrixWithinThatClass = map.get(s);
            Mat meanOfCurrentClass = getMean(matrixWithinThatClass);
            for (int i = 0; i < matrixWithinThatClass.size(); i++) {
                Mat temp1 = matrixWithinThatClass.get(i).diff(
                        meanOfCurrentClass);
                temp1 = temp1.times(temp1.transpose());
                Sw.addEquals(temp1);
            }

            Mat temp = meanOfCurrentClass.diff(meanTotal);
            temp = temp.times(temp.transpose()).times(
                    matrixWithinThatClass.size());
            Sb.addEquals(temp);
        }
        Mat targetForEigen = Sw.inverse().times(Sb);
        EigenvalueDecompose feature = targetForEigen.eig();

        double[] d = feature.getd();
        assert d.length >= c - 1 : "Ensure that the number of eigenvalues is larger than c - 1";
        int[] indexes = getIndexesOfKEigenvalues(d, c - 1);

        Mat eigenVectors = feature.getV();
        Mat selectedEigenVectors = eigenVectors.getMat(0,
                eigenVectors.getRowDim() - 1, indexes);

        this.W = pca.getW().times(selectedEigenVectors);
        this.projectedTrainingSet = new ArrayList<ProjectedTrainingMat>();
        for (int i = 0; i < trainingSet.size(); i++) {
            ProjectedTrainingMat ptm = new ProjectedTrainingMat(this.W
                    .transpose()
                    .times(trainingSet.get(i).diff(pca.meanMat)),
                    labels.get(i));
            this.projectedTrainingSet.add(ptm);
        }
        this.meanMat = pca.meanMat;
    }

    private class mix implements Comparable {
        int index;
        double val;

        mix(int i, double v) {
            index = i;
            val = v;
        }

        public int compareTo(Object o) {
            double target = ((mix) o).val;
            if (val > target)
                return -1;
            else if (val < target)
                return 1;

            return 0;
        }
    }

    private int[] getIndexesOfKEigenvalues(double[] d, int k) {
        mix[] mixAll = new mix[d.length];
        int i;
        for (i = 0; i < d.length; i++)
            mixAll[i] = new mix(i, d[i]);

        Arrays.sort(mixAll);

        int[] res = new int[k];
        for (i = 0; i < k; i++)
            res[i] = mixAll[i].index;
        return res;
    }

    static Mat getMean(ArrayList<Mat> m) {
        int n = m.size();
        int row = m.get(0).getRowDim();
        int col = m.get(0).getColDim();

        assert col == 1 : "Column must be 1!";

        Mat mean = new Mat(row, col);
        for (int i = 0; i < n; i++) {
            mean.addEquals(m.get(i));
        }

        mean = mean.times((double) 1 / n);
        return mean;
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
}
