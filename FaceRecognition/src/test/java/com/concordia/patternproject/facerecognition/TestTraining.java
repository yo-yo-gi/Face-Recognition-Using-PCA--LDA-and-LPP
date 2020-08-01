package com.concordia.patternproject.facerecognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.concordia.patternproject.facerecognition.feature.FeatureType;
import com.concordia.patternproject.facerecognition.corelogic.Mat;
import com.concordia.patternproject.facerecognition.trainingprograms.*;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;


public class TestTraining
{
	ClassLoader classLoader = getClass().getClassLoader();

	//@org.junit.Test
	public void GridSearchCV() throws IOException
	{
		int componentsRetained = 40;
		int trainNums = 7;

		int k_nn_value_1 = 2;
		int k_nn_value_2 = 3;
		int k_nn_value_3 = 4;


		ArrayList<Integer> knn_values = new ArrayList<>();
		ArrayList<Double> accuracy = new ArrayList<>();
		knn_values.add(k_nn_value_1);
		knn_values.add(k_nn_value_2);
		knn_values.add(k_nn_value_3);

		System.out.println("\n Grid search Cross validation results ");
		System.out.println(" componentsRetained = " + componentsRetained);
		System.out.println(" trainingSet = " + trainNums * 40 + " pictures");
		System.out.println(" testingSet = " + (10 - trainNums) * 40 + " pictures");

		System.out.println(" ---------------------------------------");
		System.out.println("    ");
		accuracy.add(test(2, componentsRetained, 0, trainNums, k_nn_value_1));
		accuracy.add(test(2, componentsRetained, 0, trainNums, k_nn_value_2));
		accuracy.add(test(2, componentsRetained, 0, trainNums, k_nn_value_3));
		Printresults(" PCA ", knn_values, accuracy);
		accuracy.clear();
		accuracy.add(test(2, componentsRetained, 1, trainNums, k_nn_value_1));
		accuracy.add(test(2, componentsRetained, 1, trainNums, k_nn_value_2));
		accuracy.add(test(2, componentsRetained, 1, trainNums, k_nn_value_3));
		Printresults(" LDA ", knn_values, accuracy);
		accuracy.clear();
		accuracy.add(test(2, componentsRetained, 2, trainNums, k_nn_value_1));
		accuracy.add(test(2, componentsRetained, 2, trainNums, k_nn_value_2));
		accuracy.add(test(2, componentsRetained, 2, trainNums, k_nn_value_3));
		Printresults(" LPP ", knn_values, accuracy);
	}

	public void Printresults(String method, ArrayList<Integer> knn_value, ArrayList<Double> accuracy)
	{

		System.out.println(method + " results ");
		System.out.println(" ------------------  ");
		for (int i = 0; i < knn_value.size(); i++)
		{
			System.out.println(" knn_value - " + knn_value.get(i) + " , accuracy - " + accuracy.get(i));
		}
		System.out.println(" ------------------  ");
	}


	/*metricType:
	 * 	0: CosineDissimilarity
	 * 	1: L1Distance
	 * 	2: EuclideanDistance
	 *
	 * energyPercentage:
	 *  PCA: components = as * energyPercentage
	 *  LDA: components = (c-1) *energyPercentage
	 *  LLP: components = (c-1) *energyPercentage
	 *
	 * featureExtractionMode
	 * 	0: PCA
	 *	1: LDA
	 * knn_k: number of K for KNN algorithm
	 *
	 * */
	double test(int metricType, int componentsRetained, int featureExtractionMode, int trainNums, int knn_k) throws IOException
	{
		//determine which metric is used
		//metric
		Metric metric = null;
		if (metricType == 0)
			metric = new CosDissimilarity();
		else if (metricType == 1)
			metric = new L1Dist();
		else if (metricType == 2)
			metric = new EuclidDistanceCalculation();

		assert metric != null : "metricType is wrong!";

		//set trainSet and testSet
		HashMap<String, ArrayList<Integer>> trainMap = new HashMap();
		HashMap<String, ArrayList<Integer>> testMap = new HashMap();
		for (int i = 1; i <= 40; i++)
		{
			String label = "s" + i;
			ArrayList<Integer> train = generateTrainNums(trainNums);
			ArrayList<Integer> test = generateTestNums(train);
			trainMap.put(label, train);
			testMap.put(label, test);
		}

		//trainingSet & respective labels
		ArrayList<Mat> trainingSet = new ArrayList<Mat>();
		ArrayList<String> labels = new ArrayList<String>();

		Set<String> labelSet = trainMap.keySet();
		Iterator<String> it = labelSet.iterator();

		ClassLoader classLoader = getClass().getClassLoader();
		while (it.hasNext())
		{
			String label = it.next();
			ArrayList<Integer> cases = trainMap.get(label);
			for (int i = 0; i < cases.size(); i++)
			{
				String filePath = "/faces/" + label + "/" + cases.get(i) + ".pgm";
				InputStream inputStream = Resources.class.getResourceAsStream(filePath);
				File tempFile = File.createTempFile("pic", ".pgm");
				tempFile.deleteOnExit();

				ByteStreams.copy(inputStream, new FileOutputStream(tempFile));

				Mat temp;
				try
				{
					temp = FileManager.pgmMatrixConversion(tempFile.getAbsolutePath());
					trainingSet.add(convertToVector(temp));
					labels.add(label);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		}

		//testingSet & respective true labels
		ArrayList<Mat> testingSet = new ArrayList<Mat>();
		ArrayList<String> trueLabels = new ArrayList<String>();

		labelSet = testMap.keySet();
		it = labelSet.iterator();
		while (it.hasNext())
		{
			String label = it.next();
			ArrayList<Integer> cases = testMap.get(label);
			for (int i = 0; i < cases.size(); i++)
			{
				String filePath = "/faces/" + label + "/" + cases.get(i) + ".pgm";

				InputStream inputStream = Resources.class.getResourceAsStream(filePath);
				File tempFile = File.createTempFile("pic", ".pgm");
				tempFile.deleteOnExit();

				ByteStreams.copy(inputStream, new FileOutputStream(tempFile));

				Mat temp;
				try
				{
					temp = FileManager.pgmMatrixConversion(tempFile.getAbsolutePath());
					testingSet.add(convertToVector(temp));
					trueLabels.add(label);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		}

		//set featureExtraction
		try
		{
			Extraction fe = null;
			if (featureExtractionMode == 0)
				fe = new PCA(trainingSet, labels, componentsRetained);
			else if (featureExtractionMode == 1)
				fe = new LDA(trainingSet, labels, componentsRetained);
			else if (featureExtractionMode == 2)
				fe = new LPP(trainingSet, labels, componentsRetained);


			//use test cases to validate
			//testingSet   trueLables
			ArrayList<ProjectedTrainingMat> projectedTrainingSet = fe.getProjectedTrainingSet();
			int accurateNum = 0;
			for (int i = 0; i < testingSet.size(); i++)
			{
				Mat testCase = fe.getW().transpose().times(testingSet.get(i).diff(fe.getAverageMat()));
				String result = KNN.assignLabel(projectedTrainingSet.toArray(new ProjectedTrainingMat[0]), testCase, knn_k, metric);
				if (result.equals(trueLabels.get(i)))
					accurateNum++;
			}
			double accuracy = accurateNum / (double) testingSet.size();
			double error_rate = ((double) testingSet.size() - accurateNum) / (double) testingSet.size();
//			System.out.println("Accuracy is " + accuracy);
//			System.out.println("Error rate is " + error_rate);
//			System.out.println("  ");
			return accuracy;

		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}

		return -1;
	}

	static ArrayList<Integer> generateTrainNums(int trainNum)
	{
		Random random = new Random();
		ArrayList<Integer> result = new ArrayList<Integer>();

		while (result.size() < trainNum)
		{
			int temp = random.nextInt(10) + 1;
			while (result.contains(temp))
			{
				temp = random.nextInt(10) + 1;
			}
			result.add(temp);
		}

		return result;
	}

	static ArrayList<Integer> generateTestNums(ArrayList<Integer> trainSet)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 1; i <= 10; i++)
		{
			if (!trainSet.contains(i))
				result.add(i);
		}
		return result;
	}

	@org.junit.Test
	public void testTraining() throws Exception
	{
		// Build a trainer
		Trainer trainer = Trainer.builder().metric(new CosDissimilarity()).featureType(FeatureType.PCA).numberOfComponents(2).k(2)
				.build();

		String a1 = "train/s1/1.pgm";
		String a2 = "train/s1/2.pgm";
		String a3 = "train/s1/3.pgm";
		String a5 = "train/s1/5.pgm";
		String a6 = "train/s1/6.pgm";
		String a7 = "train/s1/7.pgm";
		String a8 = "train/s1/8.pgm";
		String a9 = "train/s1/9.pgm";
		String a10 = "train/s1/10.pgm";

		String b1 = "train/s2/1.pgm";
		String b2 = "train/s2/2.pgm";
		String b3 = "train/s2/3.pgm";
		String b4 = "train/s2/4.pgm";
		String b5 = "train/s2/5.pgm";
		String b6 = "train/s2/6.pgm";
		String b8 = "train/s2/8.pgm";
		String b9 = "train/s2/9.pgm";
		String b10 = "train/s2/10.pgm";

		String c1 = "train/s3/1.pgm";
		String c2 = "train/s3/2.pgm";
		String c3 = "train/s3/3.pgm";
		String c4 = "train/s3/4.pgm";
		String c6 = "train/s3/6.pgm";
		String c7 = "train/s3/7.pgm";
		String c8 = "train/s3/8.pgm";
		String c9 = "train/s3/9.pgm";
		String c10 = "train/s3/10.pgm";

		String d1 = "train/s4/1.pgm";
		String d2 = "train/s4/2.pgm";
		String d3 = "train/s4/3.pgm";
		String d4 = "train/s4/4.pgm";
		String d5 = "train/s4/5.pgm";
		String d6 = "train/s4/6.pgm";
		String d7 = "train/s4/7.pgm";
		String d8 = "train/s4/8.pgm";
		String d9 = "train/s4/9.pgm";

		//Test Sample
		String d10 = "test/s4/10.pgm";
		String c5 = "test/s3/5.pgm";
		String b7 = "test/s2/7.pgm";
		String a4 = "test/s1/4.pgm";



		// add training  data
		trainer.add(convertToMatrix(a1), "a");
		trainer.add(convertToMatrix(a2), "a");
		trainer.add(convertToMatrix(a3), "a");
		trainer.add(convertToMatrix(a5), "a");
		trainer.add(convertToMatrix(a6), "a");
		trainer.add(convertToMatrix(a7), "a");
		trainer.add(convertToMatrix(a8), "a");
		trainer.add(convertToMatrix(a9), "a");
		trainer.add(convertToMatrix(a10), "a");

		trainer.add(convertToMatrix(b1), "b");
		trainer.add(convertToMatrix(b2), "b");
		trainer.add(convertToMatrix(b3), "b");
		trainer.add(convertToMatrix(b4), "b");
		trainer.add(convertToMatrix(b5), "b");
		trainer.add(convertToMatrix(b6), "b");
		trainer.add(convertToMatrix(b8), "b");
		trainer.add(convertToMatrix(b9), "b");
		trainer.add(convertToMatrix(b10), "b");

		trainer.add(convertToMatrix(c1), "c");
		trainer.add(convertToMatrix(c2), "c");
		trainer.add(convertToMatrix(c3), "c");
		trainer.add(convertToMatrix(c4), "c");
		trainer.add(convertToMatrix(c6), "c");
		trainer.add(convertToMatrix(c7), "c");
		trainer.add(convertToMatrix(c8), "c");
		trainer.add(convertToMatrix(c9), "c");
		trainer.add(convertToMatrix(c10), "c");

		trainer.add(convertToMatrix(d1), "d");
		trainer.add(convertToMatrix(d2), "d");
		trainer.add(convertToMatrix(d3), "d");
		trainer.add(convertToMatrix(d4), "d");
		trainer.add(convertToMatrix(d5), "d");
		trainer.add(convertToMatrix(d6), "d");
		trainer.add(convertToMatrix(d7), "d");
		trainer.add(convertToMatrix(d8), "d");
		trainer.add(convertToMatrix(d9), "d");

		// train the model
		trainer.train();
		System.out.println(" Model Training is done ");
		System.out.println(" ");
		// Test the model
		System.out.println(" Testing  model  with test data");
		System.out.println("  ");

		System.out.println(" Actual Label - a");
		System.out.println(" Predicted Label - " + trainer.recognize(convertToMatrix(a4)));
		System.out.println(" Actual Label - b");
		System.out.println(" Predicted Label - " + trainer.recognize(convertToMatrix(b7)));
		System.out.println(" Actual Label - c");
		System.out.println(" Predicted Label - " + trainer.recognize(convertToMatrix(c5)));
		System.out.println(" Actual Label - d");
		System.out.println(" Predicted Label - " + trainer.recognize(convertToMatrix(d10)));

		assertEquals("a", trainer.recognize(convertToMatrix(a4)));

		assertEquals("b", trainer.recognize(convertToMatrix(b7)));
		assertEquals("c", trainer.recognize(convertToMatrix(c5)));
		assertEquals("d", trainer.recognize(convertToMatrix(d10)));

	}

	private Mat convertToMatrix(String fileAddress) throws IOException
	{
		File file = new File(classLoader.getResource(fileAddress).getFile());
		return convertToVector(FileManager.pgmMatrixConversion(file.getAbsolutePath()));
	}

	//Convert a m by n matrix into a m*n by 1 matrix
	static Mat convertToVector(Mat input)
	{
		int m = input.getRowDim();
		int n = input.getColDim();

		Mat result = new Mat(m * n, 1);
		for (int p = 0; p < n; p++)
		{
			for (int q = 0; q < m; q++)
			{
				result.set(p * m + q, 0, input.get(q, p));
			}
		}
		return result;
	}


}