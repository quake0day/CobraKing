package com.example.cobra;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;


public class Process {
	private int bmpHeight = 0;
	private int bmpWidth = 0;
	public static String TAG = "cn";
	private Point[] pRect;	//TL, TR, BR, BL
	private int[] pixels;
	int curDOB = 0;
	
	Process()
	{
		System.out.println("Hello World!"); // Display the string
	}
	
	public void initProcess(byte[] RGB,int mPreviewHeight,int mPreviewWidth)
	{
		Log.i(TAG, "HELLO I'm in process");
		/*
		String imgName = Integer.toString(0)+".bmp" ;
		
		//Create file for the source  
		File input = new File(folderPath+imgName);  
		  
		//Read the file to a BufferedImage  
		try {
			mImageOrig = ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		*/
		//bmpHeight = mImageOrig.getHeight();
		//bmpWidth = mImageOrig.getWidth();
		bmpHeight = mPreviewHeight;
		bmpWidth = mPreviewWidth;
		Log.i(TAG, "H:" + Integer.toString(bmpHeight)+", W:" + Integer.toString(bmpWidth));
		pixels = new int [bmpHeight*bmpWidth];
		pRect = new Point[4];
		for(int i=0; i<4; i++)
			pRect[i] = new Point(0,0);			
	}
	
	public boolean processImage(int index)
	{	
		
		if(!loadPixels(index))
			return false;
		
		
    	//********************************************/
    	//**************PROCESS IMAGE*****************/
    	long startTimeNano = System.nanoTime();
    	
		curDOB = polarizeImageHSV(pixels);      	
		System.out.println("DOB: " + Integer.toString(curDOB));
    	/*
		EXTRACT_CODE();	    
	
    	
		/*
		//System.out.println("W,H: " + Integer.toString(bmpWidth)+","+Integer.toString(bmpHeight));
		*/
		return true;
	
	}
	
	
	
    private boolean loadPixels(int index)
    {
    	String imgName = Integer.toString(index)+".bmp" ;
    	String filePath = Environment.getExternalStorageDirectory() + 
    			"/Pictures/testPhoto/"+imgName;
		
		//Create file for the source  
		//File input = new File(folderPath+imgName);
		/*
		//Read the file to a BufferedImage  
		try {
			mImageOrig = ImageIO.read(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if(mImageOrig==null)
			return false;
		
		mImageOrig.getRGB(0, 0, bmpWidth, bmpHeight, pixels, 0, bmpWidth);
		
		System.out.println("ReadBmp: " + Integer.toString(index));
		*/
    	Bitmap bitmap = getDiskBitmap(filePath);
    	int picw = bitmap.getWidth();
    	int pich = bitmap.getHeight();
        int[] pix = new int[picw * pich];
        bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich);

        int R, G, B,Y;

        for (int y = 0; y < pich; y++){
        for (int x = 0; x < picw; x++)
            {
            int index_pix = y * picw + x;
            R = (pix[index_pix] >> 16) & 0xff;     //bitwise shifting
            G = (pix[index_pix] >> 8) & 0xff;
            B = pix[index_pix] & 0xff;

            //R,G.B - Red, Green, Blue
             //to restore the values after RGB modification, use 
//next statement
            pix[index_pix] = 0xff000000 | (R << 16) | (G << 8) | B;
            if(index_pix < 1000){
            Log.i(TAG+"index:"," "+index_pix);
            Log.i(TAG+"Value:;",""+pix[index_pix]);
            }
            }}
        
        
		return true;
    }
    

	private Bitmap getDiskBitmap(String pathString)
	{
		Bitmap bitmap = null;
		try
		{
			File file = new File(pathString);
			if(file.exists())
			{
				bitmap = BitmapFactory.decodeFile(pathString);
			}
		} catch (Exception e)
		{
			// TODO: handle exception
		}
		
		
		return bitmap;
	}
	
    /**
     * color enhancement based on HSV model, returns DOB
     * @param orgBmp
     * @param pixels : store pixel colors after enhanced
     */
    private int polarizeImageHSV(int[] pixels)
    {
    	int[] buf = pixels;
    	
    	//HSV----------------
    	int c;
    	float[] hsv = {0, 0, 0};
    	
    	//int index = 0;
    	int len = buf.length;
    	
    	
    	int black = ColorConverter.BLACK;
    	int white = ColorConverter.WHITE;
    	int red = ColorConverter.RED;
    	int green = ColorConverter.GREEN;
    	int blue = ColorConverter.BLUE;
    	
    	int r,g,b;
    	
    	//dob----------------
    	int sum = 0;
    	int cnt = 0;
    	int t;
    	
    	int W = bmpWidth;
    	int H = bmpHeight;
    	
    	//while(index<len)
    	for(int index = 0; index<len; index++)
    	{	
			c = buf[index];	
    		
    		//**************DOB******************
			t=0;
			
			r = ColorConverter.red(c);
			g = ColorConverter.green(c);
			b = ColorConverter.blue(c);
			
			if(Math.abs(255-r)>r)
				t+=r;
			else
				t+=Math.abs(255-r);
			
			if(Math.abs(255-g)>g)
				t+=g;
			else
				t+=Math.abs(255-g);
			
			if(Math.abs(255-b)>b)
				t+=b;
			else
				t+=Math.abs(255-b);

			sum+=t;
			cnt++;
			
			//**************HSV******************
    		ColorConverter.RGBtoHSV(c, hsv);
    		
    		float valBlack = 0.4f;
        	float valBlack1 = 0.5f;
        	float satBlack1 = 0.5f;
        	
        	float satWhite = 0.6f;
			
			if(hsv[2]<valBlack || (hsv[2]<valBlack1)&&hsv[1]<0.5f)
				buf[index] = black;
			else if(hsv[2]>=valBlack1 && hsv[1]<satWhite)
				buf[index] = white;
			else
			{
				if(hsv[0]<60 || hsv[0]>=300)
					buf[index] = red;
				else if (hsv[0]>=60 && hsv[0]<180)
					buf[index] = green;
				else
					buf[index] = blue;
			}		
														
			
    	}//end Fors
    	
    	//saveImage(buf);
    	
    	
    	return (int)((sum/3)/cnt);
    	//return 0;
    }

}
