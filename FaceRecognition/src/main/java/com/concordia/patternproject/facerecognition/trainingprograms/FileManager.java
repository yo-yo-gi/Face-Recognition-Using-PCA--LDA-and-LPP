package com.concordia.patternproject.facerecognition.trainingprograms;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.concordia.patternproject.facerecognition.corelogic.Mat;


public class FileManager {
    
    

    public static Mat normalization(Mat input) {
        int r = input.getRowDim();

        for (int i = 0; i < r; i++) 
        {
            input.set(i, 0, 0 - input.get(i, 0));

        }

        double maximum = input.get(0, 0);
        double minimum = input.get(0, 0);

        for (int i = 1; i < r; i++) 
        {
            
            
            if (minimum > input.get(i, 0))
            {
            	minimum = input.get(i, 0);
            }
            
            if (maximum < input.get(i, 0))
            {
                maximum = input.get(i, 0);
            }
        }

        Mat result = new Mat(112, 92);
        
        for (int x = 0; x < 92; x++) 
        {
            for (int y = 0; y < 112; y++) 
            {
                double val = input.get(x * 112 + y, 0);
                val = (val - minimum) * 255 / (maximum - minimum);
                result.set(y, x, val);
            }
        }

        return result;

    }

    
    public static void convertToImage(Mat input, int name) throws IOException
    {
        File fileObj = new File(name + " dimensions.bmp");
        if (!fileObj.exists())
            fileObj.createNewFile();

        BufferedImage imageBuffer = new BufferedImage(92, 112, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster ras = imageBuffer.getRaster();

        for (int m = 0; m < 112; m++) {
            for (int n = 0; n < 92; n++) {
                int val = (int) input.get(n * 112 + m, 0);
                ras.setSample(n, m, 0, val);
            }
        }

        ImageIO.write(imageBuffer, "bmp", fileObj);

    }
    
    

    public static void imageMatrix(Mat x, int feature) throws IOException 
    {
        int r = x.getRowDim();
        int c = x.getColDim();

        for (int i = 0; i < c; i++) 
        {
            


            BufferedImage imageBuffer = new BufferedImage(92, 112, BufferedImage.TYPE_BYTE_GRAY);
            Mat Eigen = normalization(x.getMat(0, r - 1, i, i));
            WritableRaster ras = imageBuffer.getRaster();

            for (int m = 0; m < 112; m++) 
            {
                for (int n = 0; n < 92; n++) 
                {
                    int val = (int) Eigen.get(m, n);
                    ras.setSample(n, m, 0, val);
                }
            }

            File fileObj = null;
            if (feature == 0)
                fileObj = new File("Eigenface" + i + ".bmp");
            else if (feature == 1)
                fileObj = new File("Fisherface" + i + ".bmp");
            else if (feature == 2)
                fileObj = new File("Laplacianface" + i + ".bmp");

            if (!fileObj.exists())
            {
                fileObj.createNewFile();
            }
            ImageIO.write(imageBuffer, "bmp", fileObj);
        }

    }

    
    public static Mat pgmMatrixConversion(String address) throws IOException {
        
    	FileInputStream fis;
    	fis = new FileInputStream(address); 
    	Scanner scannerObject = new Scanner(fis);
    	
       

        scannerObject.nextLine();
        
        int width = scannerObject.nextInt();
        int height = scannerObject.nextInt();

        fis.close();

        fis = new FileInputStream(address);
        
        int lines = 3;
        DataInputStream dis = new DataInputStream(fis);

        
        
        while (lines > 0)
        {
            char c;
            do 
            {
                c = (char) (dis.readUnsignedByte());
            } while (c != '\n');
            
            lines = lines -1;
        }

        double[][] dataArray = new double[height][width];
        for (int r = 0; r < height; r++) {
            for (int col = 0; col < width; col++) {
                dataArray[r][col] = dis.readUnsignedByte();
            }
        }

        return new Mat(dataArray);
    }
   

}
