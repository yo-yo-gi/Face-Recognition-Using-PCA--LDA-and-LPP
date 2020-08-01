package com.concordia.patternproject.facerecognition;

import java.io.IOException;
import java.util.ArrayList;

public class CrossValidation {

  public static void main(String[] args) throws IOException {
    TestTraining tt = new TestTraining();
    tt.GridSearchCV();

  }
}
