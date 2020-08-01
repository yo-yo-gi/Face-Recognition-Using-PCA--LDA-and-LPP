package com.concordia.patternproject.facerecognition.corelogic;



public class CDecomposition implements java.io.Serializable {

   private double[][] L;

   private int n;

   private boolean issymetricposdefinite;

   public CDecomposition (Mat Arg) {


      double[][] A = Arg.getArr();
      n = Arg.getRowDim();
      L = new double[n][n];
      issymetricposdefinite = (Arg.getColDim() == n);
      for (int j = 0; j < n; j++) {
         double[] Lrowj = L[j];
         double d = 0.0;
         for (int k = 0; k < j; k++) {
            double[] Lrowk = L[k];
            double s = 0.0;
            for (int i = 0; i < k; i++) {
               s += Lrowk[i]*Lrowj[i];
            }
            Lrowj[k] = s = (A[j][k] - s)/L[k][k];
            d = d + s*s;
            issymetricposdefinite = issymetricposdefinite & (A[k][j] == A[j][k]); 
         }
         d = A[j][j] - d;
         issymetricposdefinite = issymetricposdefinite & (d > 0.0);
         L[j][j] = Math.sqrt(Math.max(d,0.0));
         for (int k = j+1; k < n; k++) {
            L[j][k] = 0.0;
         }
      }
   }


   public boolean isSPD () {
      return issymetricposdefinite;
   }


   public Mat getL () {
      return new Mat(L,n,n);
   }

   public Mat solve (Mat B) {
      if (B.getRowDim() != n) {
         throw new IllegalArgumentException("Matrix row dimensions must be in form n*m and m*k.");
      }
      if (!issymetricposdefinite) {
         throw new RuntimeException("Matrix is not symmetric positive");
      }

      double[][] X = B.getArrCpy();
      int nx = B.getColDim();

	      for (int k = 0; k < n; k++) {
	        for (int j = 0; j < nx; j++) {
	           for (int i = 0; i < k ; i++) {
	               X[k][j] -= X[i][j]*L[k][i];
	           }
	           X[k][j] /= L[k][k];
	        }
	      }
	
	      for (int k = n-1; k >= 0; k--) {
	        for (int j = 0; j < nx; j++) {
	           for (int i = k+1; i < n ; i++) {
	               X[k][j] -= X[i][j]*L[i][k];
	           }
	           X[k][j] /= L[k][k];
	        }
	      }
      
      
      return new Mat(X,n,nx);
   }
  private static final long serialVersionUID = 1;

}

