package com.example.cobra;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.io.FileInputStream;


import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;



public class Process{
	private int bmpHeight = 0;
	private int bmpWidth = 0;
	public static String TAG = "cn";
	
	static final byte sNon = 0;
	static final byte sBlack = 1;
	static final byte sWhite = 2;
	static final byte sRed = 3;
	static final byte sGreen = 4;
	static final byte sBlue = 5;
	
	private Point[] pRect;	//TL, TR, BR, BL
	private int[] pixels;
	int curDOB = 0;
	int mVoteRange = 1; //(2*voteRange+1)
	boolean cornerDetected = false;	//indicate if use corner estimate
	boolean extractSucc = false;
	FileWriter mFWriter = null;
	int mFrameIndex = 287;	//index in terms of captured frames
	int mFrameIndexEnd = 287;
	int mLogFileIndex = 0;	//index in terms of extracted code
	
	private int numTimingWidth = 100;
	private int numTimingHeight = 100;
	
	//read bmps
	private String folderPath = Environment.getExternalStorageDirectory() + 
			"/Pictures/bar/new/";
	
	//save extracted info
	private String logFolderPath = Environment.getExternalStorageDirectory() + 
			"/Pictures/";
	

	Process()
	{
		System.out.println("Hello World!"); // Display the string
	}
	
	public void initProcess(int[] RGB,int mPreviewHeight,int mPreviewWidth)
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
		
		pixels = RGB;
		//processRealTime();
	}
	public boolean processRealTime()
	{
		boolean extractDone = false;
		long startTimeNano = System.nanoTime();
		curDOB = polarizeImageHSV(pixels);
		Log.i(TAG,"DOB:" + Integer.toString(curDOB));
		
		extractDone = EXTRACT_CODE();
		
		return extractSucc;
		
		
	}
	public boolean processImage(int index)
	{	
		boolean extractDone = false;
		if(!loadPixels(index))
			return false;
		
		
    	//********************************************/
    	//**************PROCESS IMAGE*****************/
    	long startTimeNano = System.nanoTime();
    	
		curDOB = polarizeImageHSV(pixels);      
		Log.i(TAG, "DOB: " + Integer.toString(curDOB));
    	
		EXTRACT_CODE();	    
	
		return extractSucc;
		/*
		//System.out.println("W,H: " + Integer.toString(bmpWidth)+","+Integer.toString(bmpHeight));
		*/
		//return true;
	
	}
	
	
    private boolean EXTRACT_CODE()
    {   
    	Log.i(TAG, "I'm in extract_CODE");
    	if(cornerDetected)
    		cornerEstimate(pixels, pRect);
    	else
    		cornerDetectData(pixels, pRect);
    	
    	if(!extractCode(pixels, bmpWidth, bmpHeight, pRect))
    		cornerDetected = false;
   
    	return true;
    }
    /**
     * extract the code in code area given corner locations
     * including the process of compute timing blocks
     * including the process of code scan
     * @param buf
     * @param W
     * @param H
     * @param rect
     */
    private boolean extractCode(int[] buf, int W, int H, Point[] rect)
    {
    	int minBlock = 4;
    	
    	boolean showTiming = false;
    	
    	byte sNon = 0;
    	byte sBlack = 1;
    	byte sWhite = 2;
    	byte sRed = 3;
    	byte sGreen = 4;
    	byte sBlue = 5;
    	
    	int black = ColorConverter.BLACK;
    	int white = ColorConverter.WHITE;
    	int red = ColorConverter.RED;
    	int green = ColorConverter.GREEN;
    	int blue = ColorConverter.BLUE;
    	
    	Point pTL = new Point(rect[0].x, rect[0].y);
    	Point pTR = new Point(rect[1].x, rect[1].y);
    	Point pBR = new Point(rect[2].x, rect[2].y);
    	Point pBL = new Point(rect[3].x, rect[3].y);
    	
    	
    	Point[] upBlocks = new Point[numTimingHeight];
    	int upIndex = 0;
    	Point[] dnBlocks = new Point[numTimingHeight];
    	int dnIndex = 0;
    	Point[] leftBlocks = new Point[numTimingWidth];
    	int leftIndex = 0;
    	Point[] rightBlocks = new Point[numTimingWidth];
    	int rightIndex = 0;
    	
    	float xDist, yDist;
    	int c,step,ix,iy,stepUnit,xFrom, yFrom, xTo, yTo;
    	boolean isBlack;
    	boolean inBlock;
    	
    	Point startP = new Point();
    	Point endP = new Point();
    	
    	//*****************************************************
    	//------------------up timing--------------------------
    	//*****************************************************
    	isBlack = true;
    	step=0;
    	xFrom = pTL.x;
    	yFrom = pTL.y;
    	xTo = pTR.x;
    	yTo = pTR.y;
    	inBlock = false;
    	
    	if(yFrom-yTo==0)
    	{
    		//up side on screen_width
    		System.out.println("Timing Up: divided by 0");
    	}
    	else
    	{
    		yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (yTo-yFrom)/Math.abs(yTo-yFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
	    	while(iy != yTo)
	    	{
	    		step += stepUnit;
	    		ix = xFrom + Math.round( (xDist*((float)step/yDist)) );
	    		iy = yFrom + step;	//increase y by +/-1 pixel
	    		
	    		//???check pixels nearby as well
	    		c = buf[W*iy+ix];
	    		
	    		//--------------check black block-------------------
	    		if(c==black)
	    		{
	    			if(isBlack == false)
	    			{//check pre-color to tell if entering block
	    				isBlack = true;	//already entering the black block
	    				startP.x = ix;
	    				startP.y = iy;
	    				inBlock = true;
	    				//markPos(buf, ix, iy, H, W, Color.RED, 1);//@@@@@@@@@@@
	    			}
	    		}    		
	    		else
	    		{
	    			if(isBlack == true && inBlock == true)
	    			{//check pre-color to tell if exiting block
	    				isBlack = false;
	    				endP.x = ix;
	    				endP.y = iy;
	    				inBlock = false;
	    				//markPos(buf, ix, iy, H, W, Color.GREEN, 1);//@@@@@@@@@@@
	    				//compute block center
	    				if(Math.abs(endP.x-startP.x)+Math.abs(endP.y-startP.y)>minBlock)
	    				{
	    					Point origP = new Point((endP.x+startP.x)/2, (endP.y+startP.y)/2);
	    					Point centerP = new Point();
	    					//markPos(buf, origP.x, origP.y, H, W, Color.RED, 1);//@@@@@@@@@@@
	    					
	    					//accurately locate the black block center
	    					searchBlackBlock(buf, origP, centerP, H, W);
	    					
	    					
	    					if(upBlocks[0]==null)
	    						upIndex=0;	    					
	    					else
	    						upIndex+=2;	    					
	    					upBlocks[upIndex]=new Point(centerP.x, centerP.y);	  
	    				}
	    			}
	    			else
	    				isBlack = false;
	    		}//END check black block
	    		//---------------------------------------------------
	    		
	    	}//endWhile
    	}//END else
    	
    	//*****************************************************
    	//------------------down timing------------------------
    	//*****************************************************
    	isBlack = true;
    	step=0;
    	xFrom = pBL.x;
    	yFrom = pBL.y;
    	xTo = pBR.x;
    	yTo = pBR.y;
    	inBlock = false;
    	
    	if(yFrom-yTo==0)
    	{
    		//up side on screen_width
    		System.out.println("Timing dn: divided by 0");
    	}
    	else
    	{
    		yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (yTo-yFrom)/Math.abs(yTo-yFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
	    	while(iy != yTo)
	    	{
	    		step += stepUnit;
	    		ix = xFrom + Math.round( (xDist*((float)step/yDist)) );
	    		iy = yFrom + step;	//increase y by +/-1 pixel
	    		
	    		//???check pixels nearby as well
	    		c = buf[W*iy+ix];
	    		
	    		//--------------check black block-------------------
	    		if(c==black)
	    		{
	    			if(isBlack == false)
	    			{//check pre-color to tell if entering block
	    				isBlack = true;
	    				startP.x = ix;
	    				startP.y = iy;
	    				inBlock = true;
	    				//markPos(buf, ix, iy, H, W, Color.RED, 1);//@@@@@@@@@@@
	    			}
	    		}    		
	    		else
	    		{
	    			if(isBlack == true && inBlock == true)
	    			{//check pre-color to tell if exiting block
	    				isBlack = false;
	    				endP.x = ix;
	    				endP.y = iy;
	    				inBlock = false;
	    				//markPos(buf, ix, iy, H, W, Color.GREEN, 1);//@@@@@@@@@@@
	    				//compute block center
	    				if(Math.abs(endP.x-startP.x)+Math.abs(endP.y-startP.y)>minBlock)
	    				{
	    					Point origP = new Point((endP.x+startP.x)/2, (endP.y+startP.y)/2);
	    					Point centerP = new Point();
		
	    					searchBlackBlock(buf, origP, centerP, H, W);
	    					
	    					if(dnBlocks[0]==null)
	    						dnIndex=0;
	    					else
	    						dnIndex+=2;	
	    					dnBlocks[dnIndex]=new Point(centerP.x, centerP.y);
	    					
	    				}
	    			}
	    			else
	    				isBlack = false;
	    		}//END check black block
	    		//---------------------------------------------------
	    		
	    	}//end While
    	}//END else
    	
    	//*****************************************************
    	//------------------Left timing------------------------
    	//*****************************************************
    	isBlack = true;
    	step=0;
    	xFrom = pTL.x;
    	yFrom = pTL.y;
    	xTo = pBL.x;
    	yTo = pBL.y;
    	inBlock = false;
    	
    	if(xFrom-xTo==0)
    	{
    		//up side on screen_width
    		System.out.println("Timing left : divided by 0");
    	}
    	else
    	{
    		yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (xTo-xFrom)/Math.abs(xTo-xFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
	    	while(ix != xTo)
	    	{
	    		step += stepUnit;
	    		iy = yFrom + Math.round( (yDist*((float)step/xDist)) );
	    		ix = xFrom + step;	//increase x by +/-1 pixel
	    		
	    		//???check pixels nearby as well
	    		c = buf[W*iy+ix];
	    		
	    		//--------------check black block-------------------
	    		if(c==black)
	    		{
	    			if(isBlack == false)
	    			{//check pre-color to tell if entering block
	    				isBlack = true;
	    				startP.x = ix;
	    				startP.y = iy;
	    				inBlock = true;
	    				//markPos(buf, ix, iy, H, W, Color.RED, 1);//@@@@@@@@@@@
	    			}
	    		}    		
	    		else
	    		{
	    			if(isBlack == true && inBlock == true)
	    			{//check pre-color to tell if exiting block
	    				isBlack = false;
	    				endP.x = ix;
	    				endP.y = iy;
	    				inBlock = false;
	    				//markPos(buf, ix, iy, H, W, Color.GREEN, 1);//@@@@@@@@@@@
	    				//compute block center
	    				if(Math.abs(endP.x-startP.x)+Math.abs(endP.y-startP.y)>minBlock)
	    				{
	    					//markPos(buf, startP.x, startP.y, H, W, Color.RED, 1);//@@@@@@@@@@@
	    					//markPos(buf, endP.x, endP.y, H, W, Color.GREEN, 1);//@@@@@@@@@@@
	    					
	    					Point origP = new Point((endP.x+startP.x)/2, (endP.y+startP.y)/2);
	    					Point centerP = new Point();
	    					//markPos(buf, origP.x, origP.y, H, W, Color.RED, 1);//@@@@@@@@@@@
	    					
	    					searchBlackBlock(buf, origP, centerP, H, W);
	    					
	    					if(leftBlocks[0]==null)
	    						leftIndex=0;
	    					else
	    						leftIndex+=2;
	    					leftBlocks[leftIndex]=new Point(centerP.x, centerP.y);	
	    					
	    				}
	    			}
	    			else
	    				isBlack = false;
	    		}//END check black block
	    		//---------------------------------------------------
	    		
	    	}//end While
    	}//END else
    	
    	//*****************************************************
    	//------------------right timing------------------------
    	//*****************************************************
    	isBlack = true;
    	step=0;
    	xFrom = pTR.x;
    	yFrom = pTR.y;
    	xTo = pBR.x;
    	yTo = pBR.y;
    	inBlock = false;
    	
    	if(xFrom-xTo==0)
    	{
    		//up side on screen_width
    		System.out.println("Timing right : divided by 0");
    	}
    	else
    	{
    		yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (xTo-xFrom)/Math.abs(xTo-xFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
	    	while(ix != xTo)
	    	{
	    		step += stepUnit;
	    		iy = yFrom + Math.round( (yDist*((float)step/xDist)) );
	    		ix = xFrom + step;	//increase x by +/-1 pixel
	    		
	    		//???check pixels nearby as well
	    		c = buf[W*iy+ix];
	    		
	    		//--------------check black block-------------------
	    		if(c==black)
	    		{
	    			if(isBlack == false)
	    			{//check pre-color to tell if entering block
	    				isBlack = true;
	    				startP.x = ix;
	    				startP.y = iy;
	    				inBlock = true;
	    				//markPos(buf, ix, iy, H, W, Color.RED, 1);//@@@@@@@@@@@
	    			}
	    		}    		
	    		else
	    		{
	    			if(isBlack == true && inBlock == true)
	    			{//check pre-color to tell if exiting block
	    				isBlack = false;
	    				endP.x = ix;
	    				endP.y = iy;
	    				inBlock = false;
	    				//markPos(buf, ix, iy, H, W, Color.GREEN, 1);//@@@@@@@@@@@
	    				//compute block center
	    				if(Math.abs(endP.x-startP.x)+Math.abs(endP.y-startP.y)>minBlock)
	    				{	    					
	    					Point origP = new Point((endP.x+startP.x)/2, (endP.y+startP.y)/2);
	    					Point centerP = new Point();
	    					//markPos(buf, origP.x, origP.y, H, W, Color.RED, 1);//@@@@@@@@@@@
	    					
	    					searchBlackBlock(buf, origP, centerP, H, W);
	    					
	    					if(rightBlocks[0]==null)
	    						rightIndex=0;
	    					else
	    						rightIndex+=2;
	    					rightBlocks[rightIndex]=new Point(centerP.x, centerP.y);
	    				}
	    			}
	    			else
	    				isBlack = false;
	    		}//END check black block
	    		//---------------------------------------------------
	    		
	    	}//end While
    	}//END else
    	
    	
    	//***************************************************************
    	//-----------------------compute white block---------------------
    	//***************************************************************
    	if(upIndex!=dnIndex || leftIndex!=rightIndex)
    	{
    		System.out.println("!!!Timing Comp : unequal black timing blocks");
    		System.out.println("CODE SIZE" + Integer.toString(upIndex+1)+";"+
        			Integer.toString(dnIndex+1)+";"+
        			Integer.toString(leftIndex+1)+";"+
        			Integer.toString(rightIndex+1));
    		return false;
    	}
    	else
    	{	
    		int i,x,y;
        	for(i=1; i<upIndex; i+=2)
        	{	
        		x = Math.round((float)(upBlocks[i-1].x+upBlocks[i+1].x)/(float)2);
        		y = Math.round((float)(upBlocks[i-1].y+upBlocks[i+1].y)/(float)2);
        		upBlocks[i]=new Point(x,y);
        		
        		
        		x = Math.round((float)(dnBlocks[i-1].x+dnBlocks[i+1].x)/(float)2);
        		y = Math.round((float)(dnBlocks[i-1].y+dnBlocks[i+1].y)/(float)2);
        		dnBlocks[i]=new Point(x,y);
        		
        	}
        	
        	for(i=1; i<leftIndex; i+=2)
        	{	
        		x = Math.round((float)(leftBlocks[i-1].x+leftBlocks[i+1].x)/(float)2);
        		y = Math.round((float)(leftBlocks[i-1].y+leftBlocks[i+1].y)/(float)2);
        		leftBlocks[i]=new Point(x,y);
        		
        		
        		x = Math.round((float)(rightBlocks[i-1].x+rightBlocks[i+1].x)/(float)2);
        		y = Math.round((float)(rightBlocks[i-1].y+rightBlocks[i+1].y)/(float)2);
        		rightBlocks[i]=new Point(x,y);
        		
        	}
    	}    
  
    	//@@@@@@@@@@@@print code size info
    	System.out.println("CODE SIZE" + Integer.toString(upIndex+1)+";"+
    			Integer.toString(dnIndex+1)+";"+
    			Integer.toString(leftIndex+1)+";"+
    			Integer.toString(rightIndex+1));
    	
    	//****************************************************************
    	//-----------------------EXTRACT CODE-----------------------------
    	//****************************************************************
    	byte[][] flagArr = new byte[W][H];
    	int codeHeight = leftIndex+1;
    	int codeWidth = dnIndex+1;
    	byte [] colorCodeBuf = new byte[codeWidth*codeHeight];
    	
    	int codeBufLen = colorCodeBuf.length;
    	int codeBufPos = 0;
    	
    	for(int x=0; x<bmpWidth; x++)
    		for(int y=0; y<bmpHeight; y++)
    			flagArr[x][y]=0;	//set 0 as background value
    	
    	//-----------draw vertical lines--------------
    	for(int i=0; i<=upIndex; i++)
    	{  
    		if(upBlocks[i]==null || dnBlocks[i]==null)
			{
				System.out.println("Timing Error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return false;
			}  				
    		xFrom = upBlocks[i].x;
        	yFrom = upBlocks[i].y;
        	xTo = dnBlocks[i].x;
        	yTo = dnBlocks[i].y;
        	step = 0;
        	
        	yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (xTo-xFrom)/Math.abs(xTo-xFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
	    	while(ix != xTo)
	    	{
	    		step += stepUnit;
	    		iy = yFrom + Math.round( (yDist*((float)step/xDist)) );
	    		ix = xFrom + step;	//increase x by +/-1 pixel
	    		
	    		flagArr[ix][iy] = (byte)(i+1);	//set flag
	    		//markPos(buf, ix, iy, H, W, Color.YELLOW, 1);//@@@@@@@@@@@
	    	}//end While
    	}//END vertical lines-------------------------
    	
    	int[] voteArr = new int[4];
    	int voteArrLen = voteArr.length;
    	
    	//-----------draw Horizontal lines--------------
    	for(int i=0; i<=leftIndex; i++)
    	{	
    		if(leftBlocks[i]==null || rightBlocks[i]==null)
    		{
    			System.out.println("Timing Error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    			return false;
    		}
    		xFrom = leftBlocks[i].x;
        	yFrom = leftBlocks[i].y;
        	xTo = rightBlocks[i].x;
        	yTo = rightBlocks[i].y;
        	step = 0;
        	
        	yDist = (float)(yTo - yFrom);
    		xDist = (float)(xTo - xFrom);
    		stepUnit = (yTo-yFrom)/Math.abs(yTo-yFrom);
    		ix = xFrom;	//start point
        	iy = yFrom;
        	
        	//--------------scan one timing pair--------------
	    	while(iy != yTo)
	    	{
	    		step += stepUnit;
	    		ix = xFrom + Math.round( (xDist*((float)step/yDist)) );
	    		iy = yFrom + step;	//increase y by +/-1 pixel
	    		
	    		byte flag = flagArr[ix][iy];
	    		//markPos(buf, ix, iy, H, W, Color.YELLOW, 1);//@@@@@@@@@@@
	    		
	    		//detect code lock
	    		if(flag>0)
	    		{
	    			//markPos(buf, ix, iy, H, W, Color.YELLOW, 1);//@@@@@@@@@@@
	    			voteArr[0] = 0;	//Red
	    			voteArr[1] = 0;	//Green
	    			voteArr[2] = 0;	//Blue
	    			voteArr[3] = 0;	//White
	    			
	    			//------voting to decide block color------
	    			int voteRange = mVoteRange; //(2*voteRange+1)
	    			for(int x = ix-voteRange; x<=ix+voteRange; x++)
	    				for(int y = iy-voteRange; y<=iy+voteRange; y++)
	    				{
	    					//Log.e("buf[y*W+x]",Integer.toString(buf[y*W+x]));
	    					switch(buf[y*W+x])
	    					{
	    					case ColorConverter.RED:
	    						voteArr[0]++;
	    						break;
	    					case ColorConverter.GREEN:
	    						voteArr[1]++;
	    						break;
	    					case ColorConverter.BLUE:
	    					case ColorConverter.BLACK:
	    						voteArr[2]++;
	    						break;
	    					case ColorConverter.WHITE:
	    						voteArr[3]++;
	    						break;
	    					}
	    				}
	    			
	    			//--------elect best vote--------
	    			int maxVoteIndex = 0;
	    			int maxVoteValue = 0;
	    			for(int v=0; v<voteArrLen; v++)
	    			{
	    				if(voteArr[v]>maxVoteIndex)
	    				{
	    					maxVoteIndex = v;
	    					maxVoteValue = voteArr[v];
	    				}
	    			}
	    			
	    			//---------set color---------
	    			byte codeColor = sNon;
	    			switch(maxVoteIndex)
					{
					case 0:
						codeColor = sRed;
						break;
					case 1:
						codeColor = sGreen;
						break;
					case 2:
						codeColor = sBlue;
						break;
					case 3:
						codeColor = sWhite;
						break;
					}
	    			
	    			if(codeBufPos<codeBufLen)
	    			{
	    				colorCodeBuf[codeBufPos] = codeColor;
	    				codeBufPos++;
	    			}
	    			else
	    				System.out.println("extract code : codeBufPos exceed");
	    			
	    		}//END if flag>0 
	    		
	    	}//END While #scan one horizontal timing pair#
	    	
    	}//END vertical lines-------------------------
    	
    	
    	//***************************************************************
    	//----------------------------Decoding--------------------------
    	//***************************************************************
    	byte tmpB = 0;
    	int curPos = 0;
    	byte[] colorArr = new byte[4];
    	
    	//-------------extract FLAG---------------
    	byte frameFlag = 0;
    	for(int n=0; n<4; n++)
		{
    		colorArr[n] = colorCodeBuf[curPos];
    		curPos++;
		}
    	frameFlag = color2Byte(colorArr);
    	System.out.println("Flag" + Byte.toString(frameFlag));	//######################
    	
    	//------------extract SERIAL #-------------
    	for(int n=0; n<4; n++)
		{
    		colorArr[n] = colorCodeBuf[curPos];
    		//Log.e("1-b", Byte.toString(colorCodeBuf[curPos]));	//#####################
    		curPos++;
		}
    	byte mSerial_1 = color2Byte(colorArr);
    	
    	for(int n=0; n<4; n++)
		{
    		colorArr[n] = colorCodeBuf[curPos];
    		//Log.e("2-b", Byte.toString(colorCodeBuf[curPos]));	//#####################
    		curPos++;
		}
    	byte mSerial_2 = color2Byte(colorArr);
    	
    	for(int n=0; n<4; n++)
		{
    		colorArr[n] = colorCodeBuf[curPos];
    		//Log.e("3-b", Byte.toString(colorCodeBuf[curPos]));	//#####################
    		curPos++;
		}
    	byte mSerial_3 = color2Byte(colorArr);
    	//Log.e("Serial_3", Byte.toString(mSerial_3));	//#####################
		
		long reverseLong = 0;
		long tmp = 0;
		
		tmp = mSerial_1;
		if(tmp<0)
			tmp+=256;
		reverseLong = (reverseLong|tmp)<<8;
		
		tmp = mSerial_2;
		if(tmp<0)
			tmp+=256;
		reverseLong = (reverseLong|tmp)<<8;
		
		tmp = mSerial_3;
		if(tmp<0)
			tmp+=256;
		reverseLong = (reverseLong|tmp);
		Log.i(TAG,"serial #" + Long.toString(reverseLong));


		//setSerial(Long.toString(reverseLong));
		System.out.println("serial #" + Long.toString(reverseLong));	//#####################
		for (byte bClr : colorCodeBuf)
		{				
			System.out.print("CODE##:");
			System.out.println(Byte.toString((bClr)));					      	  				    		
		}
		
    	//------------extract CODE #-------------
		while (curPos < colorCodeBuf.length -1 ){
    	for(int n=0; n<4; n++)
		{
    		colorArr[n] = colorCodeBuf[curPos];
    		//Log.e("1-b", Byte.toString(colorCodeBuf[curPos]));	//#####################
    		curPos++;
		}


    	byte extract_code = color2Byte(colorArr);
    	
		Log.i("PAYLOAD",Byte.toString((extract_code)));
		}
		extractSucc = true;
		/*
		  writeFile("k.txt",k+"");
		  String a;
		  a = readFile("k.txt");
		  Log.e("data1",a);
		  */
		
    
		
		//*******************************************************
		//---------------Log frame encoding into txt-----------
		if(false)//true for sure:)
		{
			//initialize writer and string
			try
	        {
				mLogFileIndex++;
	        	mFWriter = new FileWriter(logFolderPath + "/bar/new/"+ Long.toString(mLogFileIndex)+".txt");	
			}
	        catch(Exception e)
	        {
			    e.printStackTrace();
			}
			
			try
	    	{
				//Frame Index
				Log.i(TAG+" mFrameIndex", Integer.toString( mFrameIndex ));
				mFWriter.write(Integer.toString( mFrameIndex ));
	    		mFWriter.write('\n');
	    		//serial num
				mFWriter.write(Long.toString( reverseLong ));
	    		mFWriter.write('\n');
	    		//DOB
	    		mFWriter.write(Integer.toString( curDOB ));
	    		mFWriter.write('\n');
	    		//len
	    		mFWriter.write(Integer.toString( codeBufLen ));
	    		mFWriter.write('\n');
	    		
	    		//entire code area
				for (byte bClr : colorCodeBuf)
				{					
		    		mFWriter.write(Byte.toString((bClr)));
		    		mFWriter.write('\n'); 						      	  				    		
				}
	    	}
			catch(Exception e)
	        {
			      e.printStackTrace();
			}
			
			try 
			{
				mFWriter.flush();
				mFWriter.close();
				mFWriter = null;
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//END Log
		
		
    	
		return true;
    }//END FUNCTION

	 /**
     * read bitmaps in folder (bmpFolderPath) one by one
     */
    public void readBmps()
    {
    	
    	int codeSerial = 0;
    	
    	//mFrameIndex = 180;
    	  	
    	boolean bContinue = true;
    	
    	while(mFrameIndex<=mFrameIndexEnd)
    	{	
    		if(!loadPixels(mFrameIndex))
    			break;
    		
    		System.out.println("Processing bmp #" + Integer.toString(mFrameIndex)+"--------------------------");
    		
	    	//********************************************/
	    	//**************PROCESS IMAGE*****************/
	    	long startTimeNano = System.nanoTime();
	    	
	    	curDOB = polarizeImageHSV(pixels);      	
	    	
    		EXTRACT_CODE();	    
   	
	    	//********************************************/
	    		
	    	//save2File(curDOB);
	    	
    		mFrameIndex++;    	
    	}//END While
    	
    }
    /**
     * convert color to byte
     * when decode color code
     * @param colorArr	: color array containing 4 colors
     * @return
     */
    static private byte color2Byte(byte[] colorArr)
    {
    	byte b = 0;
    	byte resultB = 0;
    	
    	int tmpInt = 0;
    	
    	for(int n=0; n<4; n++)
		{
    		tmpInt = tmpInt<<2;
    		
    		switch(colorArr[n])
    		{	    		
	    		case sRed:
	    			tmpInt = tmpInt | 0x00;
	    			break;
	    		case sGreen:
	    			tmpInt = tmpInt | 0x01;
	    			break;
	    		case sBlue:
	    			tmpInt = tmpInt | 0x02;
	    			break;
	    		case sWhite:
	    			tmpInt = tmpInt | 0x03;
	    			break;
	    		case sNon:	    			
	    			break;
	    		case sBlack:
	    			break;
    		}//END switch
		}
    	
    	if(tmpInt>127)
    		tmpInt = tmpInt-256;   	
    	resultB = (byte)(tmpInt);
    	
    	return resultB;
    }
    /**
     * search for black block when scanning between corner points
     * for detecting timing blocks
     * called when encountering black pixels
     * @param buf
     * @param p	: black pix location
     * @param resultP : black block center location as result
     * @param H
     * @param W
     */
    private void searchBlackBlock(int[] buf, Point p, Point resultP, int H, int W)
    {
    	 
		Point sumP = new Point(p.x, p.y);
		Point origP = new Point(p.x, p.y);
		int blackCnt = 1;
		int dist = 0;
		int x,y;
		boolean hasBlackPixel = true;
		
		int black = ColorConverter.BLACK;
    	int white = ColorConverter.WHITE;
    	int red = ColorConverter.RED;
    	int green = ColorConverter.GREEN;
    	int blue = ColorConverter.BLUE;
		
		while(hasBlackPixel)
		{
			hasBlackPixel = false;
			dist++;
			
			//check if out of bmp border
			if(origP.x-dist<0 ||
					origP.x+dist>=W ||
					origP.y+dist>=H ||
					origP.y-dist<0)
				break;
			
			y = origP.y-dist;	//up
			for(x=origP.x-dist; x<(origP.x+dist); x++)
			{
				if(buf[W*y+x]==black)
				{
					sumP.x+=x;
					sumP.y+=y;
					blackCnt++;
					hasBlackPixel = true;
					//buf[W*y+x] = Color.RED; //@@@@@@@@@@@@@@@@@@@@
				}
			}
			
			y = origP.y+dist;	//down
			for(x=origP.x+dist; x>(origP.x-dist); x--)
			{
				if(buf[W*y+x]==black)
				{
					sumP.x+=x;
					sumP.y+=y;
					blackCnt++;
					hasBlackPixel = true;
					//buf[W*y+x] = Color.RED; //@@@@@@@@@@@@@@@@@@@@
				}
			}
			
			x = origP.x-dist;	//left
			for(y=origP.y+dist; y>(origP.y-dist); y--)
			{
				if(buf[W*y+x]==black)
				{
					sumP.x+=x;
					sumP.y+=y;
					blackCnt++;
					hasBlackPixel = true;
					//buf[W*y+x] = Color.RED; //@@@@@@@@@@@@@@@@@@@@
				}
			}
			
			x = origP.x+dist;	//right
			for(y=origP.y-dist; y<(origP.y+dist); y++)
			{
				if(buf[W*y+x]==black)
				{
					sumP.x+=x;
					sumP.y+=y;
					blackCnt++;
					hasBlackPixel = true;
					//buf[W*y+x] = Color.RED; //@@@@@@@@@@@@@@@@@@@@
				}
			}
			
		}//END while (hasBlack)
		
		resultP.x = Math.round((float)sumP.x / (float)blackCnt);
		resultP.y = Math.round((float)sumP.y / (float)blackCnt);
		//mTextView.append("@"+Integer.toString(resultP.x)+"|"+Integer.toString(resultP.y));
    }//END Func
    
    
    private boolean loadPixels(int index)
    {
    	String imgName = Integer.toString(index)+".bmp" ;
    	String filePath = Environment.getExternalStorageDirectory() + 
    			"/Pictures/bar/new/"+imgName;
    	Log.e(TAG, "I'm in loadPixels:"+filePath);
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
    	Log.i(TAG, "picw:"+picw+"pich"+pich);
       //int[] pixels = new int[picw * pich];
        bitmap.getPixels(pixels, 0, picw, 0, 0, picw, pich);

        int R, G, B,Y;

        for (int y = 0; y < pich; y++){
        for (int x = 0; x < picw; x++)
            {
            int index_pix = y * picw + x;
            R = (pixels[index_pix] >> 16) & 0xff;     //bitwise shifting
            G = (pixels[index_pix] >> 8) & 0xff;
            B = pixels[index_pix] & 0xff;

            //R,G.B - Red, Green, Blue
             //to restore the values after RGB modification, use 
//next statement
            pixels[index_pix] = 0xff000000 | (R << 16) | (G << 8) | B;
             //for test only
            if(index_pix < 10){
            Log.i(TAG+"index:"," "+index_pix);
            Log.i(TAG+"Value:;",""+pixels[index_pix]);
            }
            
            }
       // System.out.println("ReadBmp: " + Integer.toString(index));
        }
        
        
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
     * Fast corner detection using corner estimation
     * @param buf : pixel buffer of the image
     * @param rect : corner locations of the barcode
     * @param cpybmp : image object for display purpose
     */
    private void cornerEstimate(int[] buf, Point[] rect)
    {
    	Log.i(TAG, "I'm in cornerEstimate");
    	int range = 25;
    	int startX = 0;
    	int startY = 0;
    	int endX = 0;
    	int endY = 0;
    	
    	Point pTL = new Point(rect[0].x, rect[0].y);
    	Point pTR = new Point(rect[1].x, rect[1].y);
    	Point pBR = new Point(rect[2].x, rect[2].y);
    	Point pBL = new Point(rect[3].x, rect[3].y);
    	
    	int c = 0;
    	int H = bmpHeight;
    	int W = bmpWidth;
    	
    	Point centerP = new Point(0,0);
    	 
    	
    	//*************************************************************
    	//-------------------------top left--------------------------
    	if(pTL.x-range<0)
    		startX = 0;
    	else
    		startX = pTL.x-range;
    	
    	if(pTL.y-range<0)
    		startY = 0;
    	else
    		startY = pTL.y-range;
    	
    	if(pTL.x+range>=W)
    		endX = W-1;
    	else
    		endX = pTL.x+range;
    	
    	if(pTL.y+range>=H)
    		endY = H-1;
    	else
    		endY = pTL.y+range;
    	
    	int greenPixSum = searchCornerInPatch(buf, startX, startY, endX, endY, ColorConverter.GREEN, centerP);
    	rect[0].x = centerP.x;
    	rect[0].y = centerP.y;
    	
    	//*************************************************************
    	//-------------------------top right--------------------------
    	if(pTR.x-range<0)
    		startX = 0;
    	else
    		startX = pTR.x-range;
    	
    	if(pTR.y-range<0)
    		startY = 0;
    	else
    		startY = pTR.y-range;
    	
    	if(pTR.x+range>=W)
    		endX = W-1;
    	else
    		endX = pTR.x+range;
    	
    	if(pTR.y+range>=H)
    		endY = H-1;
    	else
    		endY = pTR.y+range;
    	
    	int bluePixSum = searchCornerInPatch(buf, startX, startY, endX, endY, ColorConverter.BLUE, centerP);
    	rect[1].x = centerP.x;
    	rect[1].y = centerP.y;
    	
    	//*************************************************************
    	//-------------------------bottom right--------------------------
    	if(pBR.x-range<0)
    		startX = 0;
    	else
    		startX = pBR.x-range;
    	
    	if(pBR.y-range<0)
    		startY = 0;
    	else
    		startY = pBR.y-range;
    	
    	if(pBR.x+range>=W)
    		endX = W-1;
    	else
    		endX = pBR.x+range;
    	
    	if(pBR.y+range>=H)
    		endY = H-1;
    	else
    		endY = pBR.y+range;
    	
    	int redPixSum = searchCornerInPatch(buf, startX, startY, endX, endY, ColorConverter.RED, centerP);
    	rect[2].x = centerP.x;
    	rect[2].y = centerP.y;
    	
    	//*************************************************************
    	//-------------------------bottom left--------------------------
    	if(pBL.x-range<0)
    		startX = 0;
    	else
    		startX = pBL.x-range;
    	
    	if(pBL.y-range<0)
    		startY = 0;
    	else
    		startY = pBL.y-range;
    	
    	if(pBL.x+range>=W)
    		endX = W-1;
    	else
    		endX = pBL.x+range;
    	
    	if(pBL.y+range>=H)
    		endY = H-1;
    	else
    		endY = pBL.y+range;
    	
    	int blue1PixSum = searchCornerInPatch(buf, startX, startY, endX, endY, ColorConverter.BLUE, centerP);
    	rect[3].x = centerP.x;
    	rect[3].y = centerP.y;
    	
    	estimateVoteRange(redPixSum+greenPixSum+bluePixSum+blue1PixSum);
    	
    }
    
    /**
     * normal corner detection
     * @param buf : pixel buffer of the image
     * @param rect : corner locations as result
     * @param cpybmp : for display use
     */
    private boolean cornerDetectData(int[] buf, Point[] rect)
    {
    	Log.i(TAG, "I'm in cornerDetectData");
    	int minBlockWid = 5;
    	boolean markCorner = false;
    	
    	byte[] flagArr = new byte[bmpHeight*bmpWidth];
    	for(byte b : flagArr)
    		b=sNon;
    	
    	int black = ColorConverter.BLACK;
    	int white = ColorConverter.WHITE;
    	int red = ColorConverter.RED;
    	int green = ColorConverter.GREEN;
    	int blue = ColorConverter.BLUE;
    	
    	Point pR = null;
    	Point pG = null;
    	Point pB = null;
    	Point pW = null;
    	
    	int c = 0;
    	int H = bmpHeight;
    	int W = bmpWidth;
    	
    	for(int y = 0; y<H; y++)
    	{
    		for(int x = 0; x<W; x++)
    		{
    			c = buf[y*W+x]; 
    			
    			if(c == black)
    			{
    				detectPixelInCorner(buf, flagArr, H, W, x, y, minBlockWid);
    			}
    			
    		}//END For (x)
    	}//END For (y)
    	
    	//------------compute corner point------------
    	int len = buf.length;
    	Point pSumRed = new Point(0,0);
    	Point pSumGreen = new Point(0,0);
    	Point pSumBlue = new Point(0,0);
    	Point pSumBlueTopRight = new Point(0,0);
    	
    	int cntRed = 0;
    	int cntBlue = 0;
    	int cntGreen = 0;
    	int cntBlueTR = 0;
    	
    	Point pBlueSmp = new Point(0,0);
    	Point pBlueTRSmp = new Point(0,0);
    	
    	int t;
    	Point pTemp = new Point(0,0);
    	
    	for(int y = 0; y<H; y++)
    	{
    		for(int x = 0; x<W; x++)
	    	{
    			t = y*W+x;
	    		switch (flagArr[t])
	    		{
	    			case sNon:
		    			//buf[t] = Color.BLACK;	//SHOW CORNER#########
		    			break;
	    			case sRed:
						//buf[t] = Color.RED;		//SHOW CORNER#########
						pSumRed.x+=x;
						pSumRed.y+=y;
						cntRed++;
						//markPos(buf, x, y, H, W, Color.YELLOW, 2);//@@@@@@@@@@@
		    			break;	
	    			case sGreen:
	    				//buf[t] = Color.GREEN;	//SHOW CORNER#########
	    				pSumGreen.x+=x;
	    				pSumGreen.y+=y;
	    				cntGreen++;
	        			break;
	    			case sBlue:
	    				if(cntBlue==0 && cntBlueTR == 0)
	    				{
	    					pSumBlue.x = x;
		    				pSumBlue.y = y;
		    				cntBlue = 1;
		    				pBlueSmp.x = x;
		    				pBlueSmp.y = y;
		    				//buf[t] = Color.BLUE;	//SHOW CORNER#########
	    				}
	    				else if(cntBlue!=0)
	    				{
	    					if(Math.abs(pBlueSmp.x-x)<50)
	    					{
	    						pSumBlue.x += x;
			    				pSumBlue.y += y;
			    				cntBlue++;
			    				//buf[t] = Color.BLUE;	//SHOW CORNER#########
	    					}
	    					else
	    					{
	    						pSumBlueTopRight.x += x;
	    						pSumBlueTopRight.y += y;
			    				cntBlueTR += 1;
			    				//buf[t] = Color.YELLOW;	//SHOW CORNER#########
	    					}
	    				}
	        			break;  				
	    		}
	    		
	    	}//END FOR (x)
    	}//END FOR (y)
    	
    	//!!!RULE OUT single miss detected point in code area
    	//[possible]check distance to current average point when add new point
    	
    	if(cntRed>0 && cntGreen>0 && cntBlue>0 && cntBlueTR>0)
    	{
    		pSumRed.x/=cntRed;
        	pSumRed.y/=cntRed;
        	
        	pSumGreen.x/=cntGreen;
        	pSumGreen.y/=cntGreen;
        	
        	pSumBlue.x/=cntBlue;
        	pSumBlue.y/=cntBlue;
        	
        	pSumBlueTopRight.x/=cntBlueTR;
        	pSumBlueTopRight.y/=cntBlueTR;

        	
    	} 	
    	else
    		return false;
    	
    	//detect top right corner
    	int distBlue = Math.abs(pSumBlue.x-pSumGreen.x)+Math.abs(pSumBlue.y-pSumGreen.y);
    	int distBlueTR = Math.abs(pSumBlueTopRight.x-pSumGreen.x)+Math.abs(pSumBlueTopRight.y-pSumGreen.y);
    	
    	if(distBlueTR>distBlue)
    	{
    		pTemp.x = pSumBlue.x;
    		pTemp.y = pSumBlue.y;

    		pSumBlue.x = pSumBlueTopRight.x;
    		pSumBlue.y = pSumBlueTopRight.y;
    		
    		pSumBlueTopRight.x = pTemp.x;
    		pSumBlueTopRight.y = pTemp.y;
    	}
    	
    	//---------------check goodness---------------
    	/*double ax = pSumGreen.x - pSumBlue.x;
    	double ay = pSumGreen.y - pSumBlue.y;   	
    	double bx = pSumRed.x - pSumBlue.x;
    	double by = pSumRed.y - pSumBlue.y;
    	
    	double angle = Math.acos((ax*bx+ay*by)/(Math.sqrt(ax*ax+ay*ay)*Math.sqrt(bx*bx+by*by)));
    	if(angle<75 || angle>105)
    	{
    		mTextView.append("@corner angle check wrong");
    	}*/
    	
    	if(rect[0]==null)
    		System.out.println("!!!");
    	
    	rect[0].set(pSumGreen.x, pSumGreen.y);	//top left
    	rect[1].set(pSumBlueTopRight.x, pSumBlueTopRight.y);	//top right
    	rect[2].set(pSumRed.x, pSumRed.y);	//bottom right
    	rect[3].set(pSumBlue.x, pSumBlue.y);	//bottom left */
    	
    	//enable corner estimation
    	cornerDetected = true;
    	
    	estimateVoteRange(cntRed+cntGreen+cntBlue+cntBlueTR);
    	Log.i(TAG, "I'm in cornerDetectData END");
    	return true;  
    }
    
    /**
     * tell if (x,y) is a pixel inside black corner block
     * if it is, mark (x,y) in flagArr using sColor
     * @param buf
     * @param flagArr 
     * @param H
     * @param W
     * @param x
     * @param y
     */
    private void detectPixelInCorner(int[] buf, byte[] flagArr, int H, int W, int x, int y, int minB)
    {
    	int minBlockWid = minB;
    	int maxBlockWid = 4*minBlockWid;
    	int c;
    	Boolean bContinue = true;
    	
    	int black = ColorConverter.BLACK;
    	int white = ColorConverter.WHITE;
    	int red = ColorConverter.RED;
    	int green = ColorConverter.GREEN;
    	int blue = ColorConverter.BLUE;
    	
    	int blkBoundaryNum = 0;	//filter out fake corner tracker in code area, MUST>=2
    	
    	if(flagArr[W*y+x] != sNon)
    	{
    		//confirmed pixel, sign pixels around it the same
    		byte flag = flagArr[W*y+x];
    		byte f = sNon;
    		int step = 1;
    		
    		//------UP------
    		while (true)
    		{
    			if(y-step<0)
    				break;    			
    			
    			f = flagArr[W*(y-step)+x];
    			
    			if (f == sNon && buf[(y-step)*W+x] == black)
    				flagArr[W*(y-step)+x] = flag;
    			else
    				break;
  				
    			step++;
    		}
    		
    		//------DOWN------
    		step = 1;
    		while (true)
    		{
    			if(y+step >= H)
    				break;    			
    			
    			f = flagArr[W*(y+step)+x];
    			
    			if (f == sNon && buf[(y+step)*W+x] == black)
    				flagArr[W*(y+step)+x] = flag;
    			else
    				break;
  				
    			step++;
    		}
    		
    		//------LEFT------
    		step = 1;
    		while (true)
    		{
    			if(x-step < 0)
    				break;    			
    			
    			f = flagArr[W*y+x-step];
    			
    			if (f == sNon && buf[y*W+x-step] == black)
    				flagArr[W*y+x-step] = flag;
    			else
    				break;
  				
    			step++;
    		}
    		
    		//------RIGHT------
    		step = 1;
    		while (true)
    		{
    			if(x+step >= W)
    				break;    			
    			
    			f = flagArr[W*y+x+step];
    			
    			if (f == sNon && buf[y*W+x+step] == black)
    				flagArr[W*y+x+step] = flag;
    			else
    				break;
  				
    			step++;
    		}
    		
    		//skip search part below
    		return;
    	}
    	
    	//***************************************************************
    	//search surrounded colors in 4 directions
    	//to confirm:
    	//(1) compare if they are the same color 
    	//(2) examine the black and color pixels covered in 4 direction 
    	
    	//--------------UP SEARCH--------------
    	int upBlackNum = 0;
    	int upClrNum = 0;
    	byte upClr = sNon;
    	int upCnt = 0;
    	
    	while(bContinue)
    	{
    		if(upBlackNum>maxBlockWid)
    			break;
    		
    		upCnt++;
    		if(y-upCnt<0)
    		{
    			upCnt--;
    			break;
    		}
    		
    		c = buf[(y-upCnt)*W+x];
		
    		switch (c)
    		{
	    		case ColorConverter.BLACK:
	    			if(upClrNum>0)
	    			{
	    				//------confirm on corner edge-------
	    				int temp = upCnt;
	    				int cnt = 0;
	    				while(true)
	    				{
	    					if(y-temp<0)
	    						break;
	    					
	    					c = buf[(y-temp)*W+x];
	    					
	    					if(c != black)
	    						break;
	    					else
	    					{
	    						cnt++;
	    						temp++;
	    					}
	    				}
	    				if(cnt>minBlockWid)
	    					blkBoundaryNum++;
	    				//------------------------------------
	    				
	    				upCnt--;
	    				bContinue = false;	    				
	    			}
	    			else
	    				upBlackNum++;
	    			break;
	    		case ColorConverter.WHITE:
	    			upCnt--;
	    			bContinue = false;
	    			break;
	    		case ColorConverter.RED:
	    			if(upClrNum == 0 || upClr == sRed)
	    			{
	    				upClrNum++;
	    				upClr = sRed;
	    			}
	    			else
	    			{
	    				upCnt--;
	    				bContinue = false;
	    			}
	    			break;
	    		case ColorConverter.GREEN:
	    			if(upClrNum == 0 || upClr == sGreen)
	    			{
	    				upClrNum++;
	    				upClr = sGreen;
	    			}
	    			else
	    			{
	    				upCnt--;
	    				bContinue = false;     			
	    			}
	    			break;
	    		case ColorConverter.BLUE:
	    			if(upClrNum == 0 || upClr == sBlue)
	    			{
	    				upClrNum++;
	    				upClr = sBlue;
	    			}
	    			else
	    			{
	    				upCnt--;
	    				bContinue = false;       			
	    			}
	    			break;
    		}//END SWITCH		
    	}//END WHILE
    	
    	//--------------DOWN SEARCH--------------
    	int dnBlackNum = 0;
    	int dnClrNum = 0;
    	byte dnClr = sNon;
    	int dnCnt = 0;
    	bContinue = true;
    	
    	while(bContinue)
    	{
    		if(dnBlackNum>maxBlockWid)
    			break;
    		
    		dnCnt++;
    		if(y+dnCnt>=H)
    		{
    			dnCnt--;
    			break;
    		}
    		
    		c = buf[(y+dnCnt)*W+x];
		
    		switch (c)
    		{
	    		case ColorConverter.BLACK:
	    			if(dnClrNum>0)
	    			{
	    				//------confirm on corner edge-------
	    				int temp = dnCnt;
	    				int cnt = 0;
	    				while(true)
	    				{
	    					if(y+temp>=H)
	    						break;
	    					
	    					c = buf[(y+temp)*W+x];
	    					
	    					if(c != black)
	    						break;
	    					else
	    					{
	    						cnt++;
	    						temp++;
	    					}
	    				}
	    				if(cnt>minBlockWid)
	    					blkBoundaryNum++;
	    				//------------------------------------
	    				
	    				dnCnt--;
	    				bContinue = false;
	    			}
	    			else
	    				dnBlackNum++;
	    			break;
	    		case ColorConverter.WHITE:
	    			dnCnt--;
	    			bContinue = false;
	    			break;
	    		case ColorConverter.RED:
	    			if(dnClrNum == 0 || dnClr == sRed)
	    			{
	    				dnClrNum++;
	    				dnClr = sRed;
	    			}
	    			else
	    			{
	    				dnCnt--;
	    				bContinue = false;
	    			}
	    			break;
	    		case ColorConverter.GREEN:
	    			if(dnClrNum == 0 || dnClr == sGreen)
	    			{
	    				dnClrNum++;
	    				dnClr = sGreen;
	    			}
	    			else
	    			{
	    				dnCnt--;
	    				bContinue = false;     			
	    			}
	    			break;
	    		case ColorConverter.BLUE:
	    			if(dnClrNum == 0 || dnClr == sBlue)
	    			{
	    				dnClrNum++;
	    				dnClr = sBlue;
	    			}
	    			else
	    			{
	    				dnCnt--;
	    				bContinue = false;       			
	    			}
	    			break;
    		}//END SWITCH		
    	}//END WHILE
    	
    	//--------------LEFT SEARCH--------------
    	int lBlackNum = 0;
    	int lClrNum = 0;
    	byte lClr = sNon;
    	int lCnt = 0;
    	bContinue = true;
    	
    	while(bContinue)
    	{
    		if(lBlackNum>maxBlockWid)
    			break;
    		
    		lCnt++;
    		if(x-lCnt<0)
    		{
    			lCnt--;
    			break;
    		}
    		
    		c = buf[y*W+x-lCnt];
		
    		switch (c)
    		{
	    		case ColorConverter.BLACK:
	    			if(lClrNum>0)
	    			{

	    				//------confirm on corner edge-------
	    				int temp = lCnt;
	    				int cnt = 0;
	    				while(true)
	    				{
	    					if(x-temp<0)
	    						break;
	    					
	    					c = buf[y*W+x-temp];
	    					
	    					if(c != black)
	    						break;
	    					else
	    					{
	    						cnt++;
	    						temp++;
	    					}
	    				}
	    				if(cnt>minBlockWid)
	    					blkBoundaryNum++;
	    				//------------------------------------
	    				
	    				lCnt--;
	    				bContinue = false;
	    				
	    			}
	    			else
	    				lBlackNum++;
	    			break;
	    		case ColorConverter.WHITE:
	    			lCnt--;
	    			bContinue = false;
	    			break;
	    		case ColorConverter.RED:
	    			if(lClrNum == 0 || lClr == sRed)
	    			{
	    				lClrNum++;
	    				lClr = sRed;
	    			}
	    			else
	    			{
	    				lCnt--;
	    				bContinue = false;
	    			}
	    			break;
	    		case ColorConverter.GREEN:
	    			if(lClrNum == 0 || lClr == sGreen)
	    			{
	    				lClrNum++;
	    				lClr = sGreen;
	    			}
	    			else
	    			{
	    				lCnt--;
	    				bContinue = false;     			
	    			}
	    			break;
	    		case ColorConverter.BLUE:
	    			if(lClrNum == 0 || lClr == sBlue)
	    			{
	    				lClrNum++;
	    				lClr = sBlue;
	    			}
	    			else
	    			{
	    				lCnt--;
	    				bContinue = false;       			
	    			}
	    			break;
    		}//END SWITCH		
    	}//END WHILE
    	
    	//--------------RIGHT SEARCH--------------
    	int rBlackNum = 0;
    	int rClrNum = 0;
    	byte rClr = sNon;
    	int rCnt = 0;
    	bContinue = true;
    	
    	while(bContinue)
    	{
    		if(rBlackNum>maxBlockWid)
    			break;
    		
    		rCnt++;
    		if(x+rCnt>=W)
    		{
    			rCnt--;
    			break;
    		}
    		
    		c = buf[y*W+x+rCnt];
		
    		switch (c)
    		{
	    		case ColorConverter.BLACK:
	    			if(rClrNum>0)
	    			{    				
	    				//------confirm on corner edge-------
	    				int temp = rCnt;
	    				int cnt = 0;
	    				while(true)
	    				{
	    					if(x+temp>=W)
	    						break;
	    					
	    					c = buf[y*W+x+temp];
	    					
	    					if(c != black)
	    						break;
	    					else
	    					{
	    						cnt++;
	    						temp++;
	    					}
	    				}
	    				if(cnt>minBlockWid)
	    					blkBoundaryNum++;
	    				//------------------------------------
	    				
	    				rCnt--;
	    				bContinue = false;
	    				
	    			}
	    			else
	    				rBlackNum++;
	    			break;
	    		case ColorConverter.WHITE:
	    			rCnt--;
	    			bContinue = false;
	    			break;
	    		case ColorConverter.RED:
	    			if(rClrNum == 0 || rClr == sRed)
	    			{
	    				rClrNum++;
	    				rClr = sRed;
	    			}
	    			else
	    			{
	    				rCnt--;
	    				bContinue = false;
	    			}
	    			break;
	    		case ColorConverter.GREEN:
	    			if(rClrNum == 0 || rClr == sGreen)
	    			{
	    				rClrNum++;
	    				rClr = sGreen;
	    			}
	    			else
	    			{
	    				rCnt--;
	    				bContinue = false;     			
	    			}
	    			break;
	    		case ColorConverter.BLUE:
	    			if(rClrNum == 0 || rClr == sBlue)
	    			{
	    				rClrNum++;
	    				rClr = sBlue;
	    			}
	    			else
	    			{
	    				rCnt--;
	    				bContinue = false;       			
	    			}
	    			break;
    		}//END SWITCH		
    	}//END WHILE
    	
    	
    	//-----------------tell if (x,y) is in corner-----------------
    	boolean bSameClr = false;
    	boolean bClrSizeFit = false;
    	boolean bBlkSizeFit = false;
    	boolean bAtCornerEdge = false;
    	
    	if(dnClr!=sNon && dnClr==upClr && dnClr==lClr && dnClr==rClr)
    		bSameClr = true;
    	if(upClrNum>minBlockWid && dnClrNum>minBlockWid && rClrNum>minBlockWid && lClrNum>minBlockWid)
    		bClrSizeFit = true;
    	if(upBlackNum+dnBlackNum>minBlockWid && lBlackNum+rBlackNum>minBlockWid)
    		bBlkSizeFit = true;
    	if(blkBoundaryNum>1)
    		bAtCornerEdge = true;
    	
    	//confirm multi-pixels in each function
    	if(bSameClr && bClrSizeFit && bBlkSizeFit && bAtCornerEdge)
    	{
    		//mark confirmed pixels
    		flagArr[W*y+x] = dnClr;
    		int d = 1;
    		
    		for(d = 1; d<=upBlackNum; d++)
    			flagArr[W*(y-d)+x] = dnClr;
    		
    		for(d = 1; d<=dnBlackNum; d++)
    			flagArr[W*(y+d)+x] = dnClr;
    		
    		for(d = 1; d<=lBlackNum; d++)
    			flagArr[W*y+x-d] = dnClr;
    		
    		for(d = 1; d<=rBlackNum; d++)
    			flagArr[W*y+x+d] = dnClr;  		
    	}
    	
    	//confirm single pixel in each function
    	//if(bSameClr && bClrSizeFit && bBlkSizeFit)
    		//flagArr[W*y+x] = dnClr;
  		
    }

    /**
     * scan the given area in image buffer 
     * for the black block surrounded by 
     * certain color of blocks
     * @param buf	: image buffer
     * @param startX 
     * @param startY
     * @param endX
     * @param endY
     * @param color :  color of surrounding blocks
     * @param p : the black block center as result
     */
    private int searchCornerInPatch(int[] buf, int startX, int startY, int endX, int endY, int color, Point p)
    {
    	int H = bmpHeight;
    	int W = bmpWidth;
    	
    	boolean markCorner = false;
    	
    	Point sumP = new Point(0,0);
    	int sumCnt = 0;
    	
    	int preColor = 0;
    	
    	int c, exitIndex, enterIndex;
    	
    	for(int y = startY; y<=endY; y++)
    	{
    		preColor = 999;
        	enterIndex = -1;
        	exitIndex = -1;
  		
    		for(int x = startX; x<=endX; x++)
    		{
    			c = buf[W*y+x];
    			
    			if(c == color && preColor==ColorConverter.BLACK && enterIndex>=0)
    			{ 			
    				//exitting black block
    				exitIndex = x-1;
    				break;
    			}   
    			else if(c == ColorConverter.BLACK && preColor==color && exitIndex<0)
    			{
    				//entering black block
					enterIndex = x;
    			}  
    			
    			preColor = c;
    			
    		}//END for x
    		
    		//check if scan black blocks in this line
    		if(enterIndex>0 && exitIndex>0 && exitIndex>enterIndex)
    		{
    			//sum black pixels if passed check
    			for(int x = enterIndex; x<=exitIndex; x++)
    			{
    				sumP.x+=x;
    				sumP.y+=y;
    				sumCnt++;   				
    			}
    		}  		
    	}//END for y
    	
    	p.x = Math.round( (float)sumP.x/(float)sumCnt );
    	p.y = Math.round( (float)sumP.y/(float)sumCnt );
    	
    	return (int)sumCnt;
    }
    
    /**
     * estimate the voting range used during code scan according to the black blocks inside corner trackers
     * @param sum: the sum of all the black pixels in 4 trackers
     */
  	private void estimateVoteRange(int sum)
  	{
  		int avgBlockSum = sum/4;
  		int estBlockWidth = (int)Math.sqrt(avgBlockSum);
  		int vr = (int)Math.floor(estBlockWidth*0.5*0.5);
  		
  		if(vr>5)
  			mVoteRange = 5;		
  		else if(vr<0)
  			mVoteRange = 1;
  		
  		System.out.println("Vote Range: " + Integer.toString(mVoteRange));
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
