package com.concordia.patternproject.facerecognition;

import com.concordia.patternproject.facerecognition.feature.FeatureType;
import com.concordia.patternproject.facerecognition.corelogic.Mat;
import com.concordia.patternproject.facerecognition.trainingprograms.*;
import com.google.common.base.Preconditions;
import lombok.experimental.Builder;

import java.util.ArrayList;
import java.util.Objects;

@Builder
public class Trainer {
    Metric metric;
    FeatureType featureType;
    Extraction featureExtraction;
    int numberOfComponents;
    int k; // k specifies the number of neighbour to consider

    ArrayList<Mat> trainingSet;
    ArrayList<String> trainingLabels;

    ArrayList<ProjectedTrainingMat> model;

    public void add(Mat matrix, String label) {
        if (Objects.isNull(trainingSet)) {
            trainingSet = new ArrayList<>();
            trainingLabels = new ArrayList<>();
        }

        trainingSet.add(matrix);
        trainingLabels.add(label);
    }

    public void addFaceAfterTraining(Mat matrix, String label) {
        featureExtraction.addFace(matrix, label);
    }

    public void train() throws Exception {
        Preconditions.checkNotNull(metric);
        Preconditions.checkNotNull(featureType);
        Preconditions.checkNotNull(numberOfComponents);
        Preconditions.checkNotNull(trainingSet);
        Preconditions.checkNotNull(trainingLabels);

        switch (featureType) {
            case PCA:
                featureExtraction = new PCA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LDA:
                featureExtraction = new LDA(trainingSet, trainingLabels, numberOfComponents);
                break;
            case LPP:
                featureExtraction = new LPP(trainingSet, trainingLabels, numberOfComponents);
                break;
        }

        model = featureExtraction.getProjectedTrainingSet();
    }

    public String recognize(Mat matrix) {
        Mat testCase = featureExtraction.getW().transpose().times(matrix.diff(featureExtraction.getAverageMat()));
        String result = KNN.assignLabel(model.toArray(new ProjectedTrainingMat[0]), testCase, k, metric);
        return result;
    }
}
