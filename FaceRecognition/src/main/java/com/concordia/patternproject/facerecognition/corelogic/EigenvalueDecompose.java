package com.concordia.patternproject.facerecognition.corelogic;

import com.concordia.patternproject.facerecognition.corelogic.util.Maths;


public class EigenvalueDecompose implements java.io.Serializable {

   private int n;

   private boolean checkSym;

   private double[] valuesd, valuese;

   private double[][] EigenVector;

   private double[][] Hessen;

   private double[] nonSymStorage;


   private void tred2  () {

      for (int j = 0; j < n; j++) {
         valuesd[j] = EigenVector[n-1][j];
      }

      for (int i = n-1; i > 0; i--) {
   
         double factorToBeScaled = 0.0;
         double ca = 0.0;
         for (int k = 0; k < i; k++) {
            factorToBeScaled = factorToBeScaled + Math.abs(valuesd[k]);
         }
         if (factorToBeScaled == 0.0) {
            valuese[i] = valuesd[i-1];
            for (int j = 0; j < i; j++) {
               valuesd[j] = EigenVector[i-1][j];
               EigenVector[i][j] = 0.0;
               EigenVector[j][i] = 0.0;
            }
         } else {
   
            for (int k = 0; k < i; k++) {
               valuesd[k] /= factorToBeScaled;
               ca += valuesd[k] * valuesd[k];
            }
            double za = valuesd[i-1];
            double xa = Math.sqrt(ca);
            if (za > 0) {
               xa = -xa;
            }
            valuese[i] = factorToBeScaled * xa;
            ca = ca - za * xa;
            valuesd[i-1] = za - xa;
            for (int j = 0; j < i; j++) {
               valuese[j] = 0.0;
            }
   
            // Apply similarity transformation to remaining columns.
   
            for (int j = 0; j < i; j++) {
               za = valuesd[j];
               EigenVector[j][i] = za;
               xa = valuese[j] + EigenVector[j][j] * za;
               for (int k = j+1; k <= i-1; k++) {
                  xa += EigenVector[k][j] * valuesd[k];
                  valuese[k] += EigenVector[k][j] * za;
               }
               valuese[j] = xa;
            }
            za = 0.0;
            for (int j = 0; j < i; j++) {
               valuese[j] /= ca;
               za += valuese[j] * valuesd[j];
            }
            double hh = za / (ca + ca);
            for (int j = 0; j < i; j++) {
               valuese[j] -= hh * valuesd[j];
            }
            for (int j = 0; j < i; j++) {
               za = valuesd[j];
               xa = valuese[j];
               for (int k = j; k <= i-1; k++) {
                  EigenVector[k][j] -= (za * valuese[k] + xa * valuesd[k]);
               }
               valuesd[j] = EigenVector[i-1][j];
               EigenVector[i][j] = 0.0;
            }
         }
         valuesd[i] = ca;
      }
   
      for (int i = 0; i < n-1; i++) {
         EigenVector[n-1][i] = EigenVector[i][i];
         EigenVector[i][i] = 1.0;
         double ca = valuesd[i+1];
         if (ca != 0.0) {
            for (int k = 0; k <= i; k++) {
               valuesd[k] = EigenVector[k][i+1] / ca;
            }
            for (int j = 0; j <= i; j++) {
               double xa = 0.0;
               for (int k = 0; k <= i; k++) {
                  xa += EigenVector[k][i+1] * EigenVector[k][j];
               }
               for (int k = 0; k <= i; k++) {
                  EigenVector[k][j] -= xa * valuesd[k];
               }
            }
         }
         for (int k = 0; k <= i; k++) {
            EigenVector[k][i+1] = 0.0;
         }
      }
      for (int j = 0; j < n; j++) {
         valuesd[j] = EigenVector[n-1][j];
         EigenVector[n-1][j] = 0.0;
      }
      EigenVector[n-1][n-1] = 1.0;
      valuese[0] = 0.0;
   } 

   private void tql2  () {

      for (int i = 1; i < n; i++) {
         valuese[i-1] = valuese[i];
      }
      valuese[n-1] = 0.0;
   
      double za = 0.0;
      double va1 = 0.0;
      double cs = Math.pow(2.0,-52.0);
      for (int l = 0; l < n; l++) {
   
         va1 = Math.max(va1,Math.abs(valuesd[l]) + Math.abs(valuese[l]));
         int na = l;
         while (na < n) {
            if (Math.abs(valuese[na]) <= cs*va1) {
               break;
            }
            na++;
         }

         if (na > l) {
            int countIterations = 0;
            do {
               countIterations = countIterations + 1;  

               double xa = valuesd[l];
               double da = (valuesd[l+1] - xa) / (2.0 * valuese[l]);
               double ta = Maths.hypot(da, 1.0);
               if (da < 0) {
                  ta = -ta;
               }
               valuesd[l] = valuese[l] / (da + ta);
               valuesd[l+1] = valuese[l] * (da + ta);
               double dl1 = valuesd[l+1];
               double ca = xa - valuesd[l];
               for (int i = l+2; i < n; i++) {
                  valuesd[i] -= ca;
               }
               za = za + ca;
   
               da = valuesd[na];
               double pa = 1.0;
               double la = pa;
               double qa = pa;
               double eigen = valuese[l+1];
               double ha = 0.0;
               double wa = 0.0;
               for (int i = na-1; i >= l; i--) {
                  qa = la;
                  la = pa;
                  wa = ha;
                  xa = pa * valuese[i];
                  ca = pa * da;
                  ta = Maths.hypot(da,valuese[i]);
                  valuese[i+1] = ha * ta;
                  ha = valuese[i] / ta;
                  pa = da / ta;
                  da = pa * valuesd[i] - ha * xa;
                  valuesd[i+1] = ca + ha * (pa * xa + ha * valuesd[i]);
   
                  // Accumulate transformation.
   
                  for (int k = 0; k < n; k++) {
                     ca = EigenVector[k][i+1];
                     EigenVector[k][i+1] = ha * EigenVector[k][i] + pa * ca;
                     EigenVector[k][i] = pa * EigenVector[k][i] - ha * ca;
                  }
               }
               da = -ha * wa * qa * eigen * valuese[l] / dl1;
               valuese[l] = ha * da;
               valuesd[l] = pa * da;
   
               // Check for convergence.
   
            } while (Math.abs(valuese[l]) > cs*va1);
         }
         valuesd[l] = valuesd[l] + za;
         valuese[l] = 0.0;
      }
     
      // Sort eigenvalues and corresponding vectors.
   
      for (int i = 0; i < n-1; i++) {
         int k = i;
         double da = valuesd[i];
         for (int j = i+1; j < n; j++) {
            if (valuesd[j] < da) {
               k = j;
               da = valuesd[j];
            }
         }
         if (k != i) {
            valuesd[k] = valuesd[i];
            valuesd[i] = da;
            for (int j = 0; j < n; j++) {
               da = EigenVector[j][i];
               EigenVector[j][i] = EigenVector[j][k];
               EigenVector[j][k] = da;
            }
         }
      }
   }

   // Nonsymmetric reduction to Hessenberg form.

   private void orthes  () {
      int wl = 0;
      int gih = n-1;
   
      for (int na = wl+1; na <= gih-1; na++) {
   
         double factorToBeScaled = 0.0;
         for (int i = na; i <= gih; i++) {
            factorToBeScaled = factorToBeScaled + Math.abs(Hessen[i][na-1]);
         }
         if (factorToBeScaled != 0.0) {
   
            double ca = 0.0;
            for (int i = gih; i >= na; i--) {
               nonSymStorage[i] = Hessen[i][na-1]/factorToBeScaled;
               ca += nonSymStorage[i] * nonSymStorage[i];
            }
            double xa = Math.sqrt(ca);
            if (nonSymStorage[na] > 0) {
               xa = -xa;
            }
            ca = ca - nonSymStorage[na] * xa;
            nonSymStorage[na] = nonSymStorage[na] - xa;
   
            for (int j = na; j < n; j++) {
               double za = 0.0;
               for (int i = gih; i >= na; i--) {
                  za += nonSymStorage[i]*Hessen[i][j];
               }
               za = za/ca;
               for (int i = na; i <= gih; i++) {
                  Hessen[i][j] -= za*nonSymStorage[i];
               }
           }
   
           for (int i = 0; i <= gih; i++) {
               double za = 0.0;
               for (int j = gih; j >= na; j--) {
                  za += nonSymStorage[j]*Hessen[i][j];
               }
               za = za/ca;
               for (int j = na; j <= gih; j++) {
                  Hessen[i][j] -= za*nonSymStorage[j];
               }
            }
            nonSymStorage[na] = factorToBeScaled*nonSymStorage[na];
            Hessen[na][na-1] = factorToBeScaled*xa;
         }
      }
   
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            EigenVector[i][j] = (i == j ? 1.0 : 0.0);
         }
      }

      for (int na = gih-1; na >= wl+1; na--) {
         if (Hessen[na][na-1] != 0.0) {
            for (int i = na+1; i <= gih; i++) {
               nonSymStorage[i] = Hessen[i][na-1];
            }
            for (int j = na; j <= gih; j++) {
               double xa = 0.0;
               for (int i = na; i <= gih; i++) {
                  xa += nonSymStorage[i] * EigenVector[i][j];
               }
               // Double division avoids possible underflow
               xa = (xa / nonSymStorage[na]) / Hessen[na][na-1];
               for (int i = na; i <= gih; i++) {
                  EigenVector[i][j] += xa * nonSymStorage[i];
               }
            }
         }
      }
   }

   private transient double bvadc, bnadc;
   private void dcvi(double rx, double ix, double ry, double iy) {
      double ta,valuesd;
      if (Math.abs(ry) > Math.abs(iy)) {
         ta = iy/ry;
         valuesd = ry + ta*iy;
         bvadc = (rx + ta*ix)/valuesd;
         bnadc = (ix - ta*rx)/valuesd;
      } else {
         ta = ry/iy;
         valuesd = iy + ta*ry;
         bvadc = (ta*rx + ix)/valuesd;
         bnadc = (ta*ix - rx)/valuesd;
      }
   }

   private void hqr2 () {
   
      int nn = this.n;
      int n = nn-1;
      int wl = 0;
      int gih = nn-1;
      double cs = Math.pow(2.0,-52.0);
      double xe = 0.0;
      double da=0,mp=0,ta=0,ha=0,lpa=0,opt,kjg,tpl,kpl;
   
      double norm = 0.0;
      for (int i = 0; i < nn; i++) {
         if (i < wl | i > gih) {
            valuesd[i] = Hessen[i][i];
            valuese[i] = 0.0;
         }
         for (int j = Math.max(i-1,0); j < nn; j++) {
            norm = norm + Math.abs(Hessen[i][j]);
         }
      }
   
      int countIterations = 0;
      while (n >= wl) {
   
         // Look for single small sub-diagonal element
   
         int l = n;
         while (l > wl) {
            ha = Math.abs(Hessen[l-1][l-1]) + Math.abs(Hessen[l][l]);
            if (ha == 0.0) {
               ha = norm;
            }
            if (Math.abs(Hessen[l][l-1]) < cs * ha) {
               break;
            }
            l--;
         }
       
         if (l == n) {
            Hessen[n][n] = Hessen[n][n] + xe;
            valuesd[n] = Hessen[n][n];
            valuese[n] = 0.0;
            n--;
            countIterations = 0;
   
   
         } else if (l == n-1) {
            kjg = Hessen[n][n-1] * Hessen[n-1][n];
            da = (Hessen[n-1][n-1] - Hessen[n][n]) / 2.0;
            mp = da * da + kjg;
            lpa = Math.sqrt(Math.abs(mp));
            Hessen[n][n] = Hessen[n][n] + xe;
            Hessen[n-1][n-1] = Hessen[n-1][n-1] + xe;
            tpl = Hessen[n][n];
   
            // Real pair
   
            if (mp >= 0) {
               if (da >= 0) {
                  lpa = da + lpa;
               } else {
                  lpa = da - lpa;
               }
               valuesd[n-1] = tpl + lpa;
               valuesd[n] = valuesd[n-1];
               if (lpa != 0.0) {
                  valuesd[n] = tpl - kjg / lpa;
               }
               valuese[n-1] = 0.0;
               valuese[n] = 0.0;
               tpl = Hessen[n][n-1];
               ha = Math.abs(tpl) + Math.abs(lpa);
               da = tpl / ha;
               mp = lpa / ha;
               ta = Math.sqrt(da * da+mp * mp);
               da = da / ta;
               mp = mp / ta;
   
   
               for (int j = n-1; j < nn; j++) {
                  lpa = Hessen[n-1][j];
                  Hessen[n-1][j] = mp * lpa + da * Hessen[n][j];
                  Hessen[n][j] = mp * Hessen[n][j] - da * lpa;
               }
   
   
               for (int i = 0; i <= n; i++) {
                  lpa = Hessen[i][n-1];
                  Hessen[i][n-1] = mp * lpa + da * Hessen[i][n];
                  Hessen[i][n] = mp * Hessen[i][n] - da * lpa;
               }
   
   
               for (int i = wl; i <= gih; i++) {
                  lpa = EigenVector[i][n-1];
                  EigenVector[i][n-1] = mp * lpa + da * EigenVector[i][n];
                  EigenVector[i][n] = mp * EigenVector[i][n] - da * lpa;
               }
   
            } else {
               valuesd[n-1] = tpl + da;
               valuesd[n] = tpl + da;
               valuese[n-1] = lpa;
               valuese[n] = -lpa;
            }
            n = n - 2;
            countIterations = 0;
   
         } else {
   
            tpl = Hessen[n][n];
            kpl = 0.0;
            kjg = 0.0;
            if (l < n) {
               kpl = Hessen[n-1][n-1];
               kjg = Hessen[n][n-1] * Hessen[n-1][n];
            }
   
            if (countIterations == 10) {
               xe += tpl;
               for (int i = wl; i <= n; i++) {
                  Hessen[i][i] -= tpl;
               }
               ha = Math.abs(Hessen[n][n-1]) + Math.abs(Hessen[n-1][n-2]);
               tpl = kpl = 0.75 * ha;
               kjg = -0.4375 * ha * ha;
            }

            if (countIterations == 30) {
                ha = (kpl - tpl) / 2.0;
                ha = ha * ha + kjg;
                if (ha > 0) {
                    ha = Math.sqrt(ha);
                    if (kpl < tpl) {
                       ha = -ha;
                    }
                    ha = tpl - kjg / ((kpl - tpl) / 2.0 + ha);
                    for (int i = wl; i <= n; i++) {
                       Hessen[i][i] -= ha;
                    }
                    xe += ha;
                    tpl = kpl = kjg = 0.964;
                }
            }
   
            countIterations = countIterations + 1;   // (Could check iteration count here.)
   
            int na = n-2;
            while (na >= l) {
               lpa = Hessen[na][na];
               ta = tpl - lpa;
               ha = kpl - lpa;
               da = (ta * ha - kjg) / Hessen[na+1][na] + Hessen[na][na+1];
               mp = Hessen[na+1][na+1] - lpa - ta - ha;
               ta = Hessen[na+2][na+1];
               ha = Math.abs(da) + Math.abs(mp) + Math.abs(ta);
               da = da / ha;
               mp = mp / ha;
               ta = ta / ha;
               if (na == l) {
                  break;
               }
               if (Math.abs(Hessen[na][na-1]) * (Math.abs(mp) + Math.abs(ta)) <
                  cs * (Math.abs(da) * (Math.abs(Hessen[na-1][na-1]) + Math.abs(lpa) +
                  Math.abs(Hessen[na+1][na+1])))) {
                     break;
               }
               na--;
            }
   
            for (int i = na+2; i <= n; i++) {
               Hessen[i][i-2] = 0.0;
               if (i > na+2) {
                  Hessen[i][i-3] = 0.0;
               }
            }
   

            for (int k = na; k <= n-1; k++) {
               boolean notlast = (k != n-1);
               if (k != na) {
                  da = Hessen[k][k-1];
                  mp = Hessen[k+1][k-1];
                  ta = (notlast ? Hessen[k+2][k-1] : 0.0);
                  tpl = Math.abs(da) + Math.abs(mp) + Math.abs(ta);
                  if (tpl == 0.0) {
                      continue;
                  }
                  da = da / tpl;
                  mp = mp / tpl;
                  ta = ta / tpl;
               }

               ha = Math.sqrt(da * da + mp * mp + ta * ta);
               if (da < 0) {
                  ha = -ha;
               }
               if (ha != 0) {
                  if (k != na) {
                     Hessen[k][k-1] = -ha * tpl;
                  } else if (l != na) {
                     Hessen[k][k-1] = -Hessen[k][k-1];
                  }
                  da = da + ha;
                  tpl = da / ha;
                  kpl = mp / ha;
                  lpa = ta / ha;
                  mp = mp / da;
                  ta = ta / da;
   
                  for (int j = k; j < nn; j++) {
                     da = Hessen[k][j] + mp * Hessen[k+1][j];
                     if (notlast) {
                        da = da + ta * Hessen[k+2][j];
                        Hessen[k+2][j] = Hessen[k+2][j] - da * lpa;
                     }
                     Hessen[k][j] = Hessen[k][j] - da * tpl;
                     Hessen[k+1][j] = Hessen[k+1][j] - da * kpl;
                  }
   
                  for (int i = 0; i <= Math.min(n,k+3); i++) {
                     da = tpl * Hessen[i][k] + kpl * Hessen[i][k+1];
                     if (notlast) {
                        da = da + lpa * Hessen[i][k+2];
                        Hessen[i][k+2] = Hessen[i][k+2] - da * ta;
                     }
                     Hessen[i][k] = Hessen[i][k] - da;
                     Hessen[i][k+1] = Hessen[i][k+1] - da * mp;
                  }
   
                  for (int i = wl; i <= gih; i++) {
                     da = tpl * EigenVector[i][k] + kpl * EigenVector[i][k+1];
                     if (notlast) {
                        da = da + lpa * EigenVector[i][k+2];
                        EigenVector[i][k+2] = EigenVector[i][k+2] - da * ta;
                     }
                     EigenVector[i][k] = EigenVector[i][k] - da;
                     EigenVector[i][k+1] = EigenVector[i][k+1] - da * mp;
                  }
               }  
            }  
         }  
      } 
      
      if (norm == 0.0) {
         return;
      }
   
      for (n = nn-1; n >= 0; n--) {
         da = valuesd[n];
         mp = valuese[n];
   
         if (mp == 0) {
            int l = n;
            Hessen[n][n] = 1.0;
            for (int i = n-1; i >= 0; i--) {
               kjg = Hessen[i][i] - da;
               ta = 0.0;
               for (int j = l; j <= n; j++) {
                  ta = ta + Hessen[i][j] * Hessen[j][n];
               }
               if (valuese[i] < 0.0) {
                  lpa = kjg;
                  ha = ta;
               } else {
                  l = i;
                  if (valuese[i] == 0.0) {
                     if (kjg != 0.0) {
                        Hessen[i][n] = -ta / kjg;
                     } else {
                        Hessen[i][n] = -ta / (cs * norm);
                     }
   
                  } else {
                     tpl = Hessen[i][i+1];
                     kpl = Hessen[i+1][i];
                     mp = (valuesd[i] - da) * (valuesd[i] - da) + valuese[i] * valuese[i];
                     opt = (tpl * ha - lpa * ta) / mp;
                     Hessen[i][n] = opt;
                     if (Math.abs(tpl) > Math.abs(lpa)) {
                        Hessen[i+1][n] = (-ta - kjg * opt) / tpl;
                     } else {
                        Hessen[i+1][n] = (-ha - kpl * opt) / lpa;
                     }
                  }
   
                  opt = Math.abs(Hessen[i][n]);
                  if ((cs * opt) * opt > 1) {
                     for (int j = i; j <= n; j++) {
                        Hessen[j][n] = Hessen[j][n] / opt;
                     }
                  }
               }
            }
   
         } else if (mp < 0) {
            int l = n-1;

            if (Math.abs(Hessen[n][n-1]) > Math.abs(Hessen[n-1][n])) {
               Hessen[n-1][n-1] = mp / Hessen[n][n-1];
               Hessen[n-1][n] = -(Hessen[n][n] - da) / Hessen[n][n-1];
            } else {
               dcvi(0.0,-Hessen[n-1][n],Hessen[n-1][n-1]-da,mp);
               Hessen[n-1][n-1] = bvadc;
               Hessen[n-1][n] = bnadc;
            }
            Hessen[n][n-1] = 0.0;
            Hessen[n][n] = 1.0;
            for (int i = n-2; i >= 0; i--) {
               double ra,sa,vr,vi;
               ra = 0.0;
               sa = 0.0;
               for (int j = l; j <= n; j++) {
                  ra = ra + Hessen[i][j] * Hessen[j][n-1];
                  sa = sa + Hessen[i][j] * Hessen[j][n];
               }
               kjg = Hessen[i][i] - da;
   
               if (valuese[i] < 0.0) {
                  lpa = kjg;
                  ta = ra;
                  ha = sa;
               } else {
                  l = i;
                  if (valuese[i] == 0) {
                     dcvi(-ra,-sa,kjg,mp);
                     Hessen[i][n-1] = bvadc;
                     Hessen[i][n] = bnadc;
                  } else {
   
                     tpl = Hessen[i][i+1];
                     kpl = Hessen[i+1][i];
                     vr = (valuesd[i] - da) * (valuesd[i] - da) + valuese[i] * valuese[i] - mp * mp;
                     vi = (valuesd[i] - da) * 2.0 * mp;
                     if (vr == 0.0 & vi == 0.0) {
                        vr = cs * norm * (Math.abs(kjg) + Math.abs(mp) +
                        Math.abs(tpl) + Math.abs(kpl) + Math.abs(lpa));
                     }
                     dcvi(tpl*ta-lpa*ra+mp*sa,tpl*ha-lpa*sa-mp*ra,vr,vi);
                     Hessen[i][n-1] = bvadc;
                     Hessen[i][n] = bnadc;
                     if (Math.abs(tpl) > (Math.abs(lpa) + Math.abs(mp))) {
                        Hessen[i+1][n-1] = (-ra - kjg * Hessen[i][n-1] + mp * Hessen[i][n]) / tpl;
                        Hessen[i+1][n] = (-sa - kjg * Hessen[i][n] - mp * Hessen[i][n-1]) / tpl;
                     } else {
                        dcvi(-ta-kpl*Hessen[i][n-1],-ha-kpl*Hessen[i][n],lpa,mp);
                        Hessen[i+1][n-1] = bvadc;
                        Hessen[i+1][n] = bnadc;
                     }
                  }
   
                  opt = Math.max(Math.abs(Hessen[i][n-1]),Math.abs(Hessen[i][n]));
                  if ((cs * opt) * opt > 1) {
                     for (int j = i; j <= n; j++) {
                        Hessen[j][n-1] = Hessen[j][n-1] / opt;
                        Hessen[j][n] = Hessen[j][n] / opt;
                     }
                  }
               }
            }
         }
      }
   
      for (int i = 0; i < nn; i++) {
         if (i < wl | i > gih) {
            for (int j = i; j < nn; j++) {
               EigenVector[i][j] = Hessen[i][j];
            }
         }
      }
   
      for (int j = nn-1; j >= wl; j--) {
         for (int i = wl; i <= gih; i++) {
            lpa = 0.0;
            for (int k = wl; k <= Math.min(j,gih); k++) {
               lpa = lpa + EigenVector[i][k] * Hessen[k][j];
            }
            EigenVector[i][j] = lpa;
         }
      }
   }


   public EigenvalueDecompose (Mat Arg) {
      double[][] A = Arg.getArr();
      n = Arg.getColDim();
      EigenVector = new double[n][n];
      valuesd = new double[n];
      valuese = new double[n];

      checkSym = true;
      for (int j = 0; (j < n) & checkSym; j++) {
         for (int i = 0; (i < n) & checkSym; i++) {
            checkSym = (A[i][j] == A[j][i]);
         }
      }

      if (checkSym) {
         for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
               EigenVector[i][j] = A[i][j];
            }
         }
   
         tred2 ();
   
         tql2 ();

      } else {
         Hessen = new double[n][n];
         nonSymStorage = new double[n];
         
         for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
               Hessen[i][j] = A[i][j];
            }
         }
   
         orthes ();
   
         hqr2();
      }
   }


   public Mat getV () {
      return new Mat(EigenVector,n,n);
   }

 
   public double[] getRealEigenvalues () {
      return valuesd;
   }


   public double[] getImagEigenvalues () {
      return valuese;
   }


   public Mat getD () {
      Mat X = new Mat(n,n);
      double[][] D = X.getArr();
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            D[i][j] = 0.0;
         }
         D[i][i] = valuesd[i];
         if (valuese[i] > 0) {
            D[i][i+1] = valuese[i];
         } else if (valuese[i] < 0) {
            D[i][i-1] = valuese[i];
         }
      }
      return X;
   }
   
   public double[] getd(){
	   return valuesd;
   }
   
  private static final long serialVersionUID = 1;
}
