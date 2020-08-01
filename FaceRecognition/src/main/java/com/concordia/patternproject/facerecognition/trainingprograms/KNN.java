package com.concordia.patternproject.facerecognition.trainingprograms;

import com.concordia.patternproject.facerecognition.corelogic.Mat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class KNN {

	public static String assignLabel(ProjectedTrainingMat[] trainingSet, Mat testFace, int K, Metric metrics) {
		ProjectedTrainingMat[] neighbors = findKNN(trainingSet, testFace, K, metrics);
		return classify(neighbors);
	}

	static ProjectedTrainingMat[] findKNN(ProjectedTrainingMat[] trainingSet, Mat testFace, int K, Metric metric) {
		int numOfTrainingSet = trainingSet.length;
		assert K <= numOfTrainingSet : "K is larger than the length of training Set!";

		ProjectedTrainingMat[] neighbors = new ProjectedTrainingMat[K];
		int i;
		for (i = 0; i < K; i++) {
			trainingSet[i].dist = metric.calcDist(trainingSet[i].mat,
					testFace);
			neighbors[i] = trainingSet[i];
		}
		for (i = K; i < numOfTrainingSet; i++) {
			trainingSet[i].dist = metric.calcDist(trainingSet[i].mat,
					testFace);
			int maxIndex = 0;
			for (int j = 0; j < K; j++) {
				if (neighbors[j].dist > neighbors[maxIndex].dist)
					maxIndex = j;
			}

			if (neighbors[maxIndex].dist > trainingSet[i].dist)
				neighbors[maxIndex] = trainingSet[i];
		}
		return neighbors;
	}

	static String classify(ProjectedTrainingMat[] neighbors) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		int num = neighbors.length;

		for (int index = 0; index < num; index++) {
			ProjectedTrainingMat temp = neighbors[index];
			String key = temp.lbl;
			if (!map.containsKey(key))
				map.put(key, 1 / temp.dist);
			else {
				double value = map.get(key);
				value += 1 / temp.dist;
				map.put(key, value);
			}
		}

		double maxSimilarity = 0;
		String returnLabel = "";
		Set<String> labelSet = map.keySet();
		Iterator<String> it = labelSet.iterator();
		while (it.hasNext()) {
			String label = it.next();
			double value = map.get(label);
			if (value > maxSimilarity) {
				maxSimilarity = value;
				returnLabel = label;
			}
		}

		return returnLabel;
	}
}
