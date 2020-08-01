package com.concordia.patternproject.facerecognition.corelogic;

public class LUDecompose implements java.io.Serializable {

   private double[][] LU;

   private int m, n, pivotsign; 

   private int[] pivot;

   public LUDecompose (Mat A) {

      LU = A.getArrCpy();
      m = A.getRowDim();
      n = A.getColDim();
      pivot = new int[m];
      for (int i = 0; i < m; i++) {
         pivot[i] = i;
      }
      pivotsign = 1;
      double[] LUrowi;
      double[] LUcolj = new double[m];

      for (int j = 0; j < n; j++) {

         for (int i = 0; i < m; i++) {
            LUcolj[i] = LU[i][j];
         }

         for (int i = 0; i < m; i++) {
            LUrowi = LU[i];

            int kmax = Math.min(i,j);
            double s = 0.0;
            for (int k = 0; k < kmax; k++) {
               s += LUrowi[k]*LUcolj[k];
            }

            LUrowi[j] = LUcolj[i] -= s;
         }

         int p = j;
         for (int i = j+1; i < m; i++) {
            if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
               p = i;
            }
         }
         if (p != j) {
            for (int k = 0; k < n; k++) {
               double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
            }
            int k = pivot[p]; pivot[p] = pivot[j]; pivot[j] = k;
            pivotsign = -pivotsign;
         }
         
         if (j < m & LU[j][j] != 0.0) {
            for (int i = j+1; i < m; i++) {
               LU[i][j] /= LU[j][j];
            }
         }
      }
   }

   public boolean isNonsingular () {
      for (int j = 0; j < n; j++) {
         if (LU[j][j] == 0)
            return false;
      }
      return true;
   }

   public Mat getL () {
      Mat X = new Mat(m,n);
      double[][] L = X.getArr();
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            if (i > j) {
               L[i][j] = LU[i][j];
            } else if (i == j) {
               L[i][j] = 1.0;
            } else {
               L[i][j] = 0.0;
            }
         }
      }
      return X;
   }

   public Mat getU () {
      Mat X = new Mat(n,n);
      double[][] U = X.getArr();
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            if (i <= j) {
               U[i][j] = LU[i][j];
            } else {
               U[i][j] = 0.0;
            }
         }
      }
      return X;
   }

   public int[] getPivot () {
      int[] p = new int[m];
      for (int i = 0; i < m; i++) {
         p[i] = pivot[i];
      }
      return p;
   }
   
   public double[] getDoublePivot () {
      double[] vals = new double[m];
      for (int i = 0; i < m; i++) {
         vals[i] = (double) pivot[i];
      }
      return vals;
   }

   public double det () {
      if (m != n) {
         throw new IllegalArgumentException("Matrix must be square.");
      }
      double d = (double) pivotsign;
      for (int j = 0; j < n; j++) {
         d *= LU[j][j];
      }
      return d;
   }


   public Mat solve (Mat B) {
      if (B.getRowDim() != m) {
         throw new IllegalArgumentException("Matrix row dimensions must be of form m*n n*k.");
      }
      if (!this.isNonsingular()) {
         throw new RuntimeException("Singular matrix");
      }

      int nx = B.getColDim();
      Mat Xmat = B.getMat(pivot,0,nx-1);
      double[][] X = Xmat.getArr();

      for (int k = 0; k < n; k++) {
         for (int i = k+1; i < n; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      for (int k = n-1; k >= 0; k--) {
         for (int j = 0; j < nx; j++) {
            X[k][j] /= LU[k][k];
         }
         for (int i = 0; i < k; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      return Xmat;
   }
  private static final long serialVersionUID = 1;
}
