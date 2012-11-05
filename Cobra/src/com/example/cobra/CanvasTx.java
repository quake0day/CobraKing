package com.example.cobra;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.http.util.EncodingUtils;

import android.content.Intent;





import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;



public class CanvasTx extends Activity 
{
	
	private PowerManager.WakeLock wl;
	
	private SensorManager mSensorManager;
	
	private SurView mSurView;
	
	private Bitmap[] bmpShowArr = null;
	private int CODE_SIZE = 10;
	
	// Define single block that you wanna transmit
	byte[] Single_block = {(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15,(byte) 0x15 };
	byte[] Single_block_1 = {(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10,(byte) 0x10 };
	byte[] Single_block_2 = {(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11,(byte) 0x11 };

	public EditText mEditText = null;
	
	//Pixnet
	RectF dst ;
	int iBmp = 1;
	int jBmp = 1;
	boolean isShowBmps = false;
	String data = null;
	private Glue glue;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
     // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        Process.setThreadPriority(-20);
		

        //requestWindowFeature(Window.FEATURE_NO_TITLE);\
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        mSurView = new SurView(this);
        setContentView(mSurView);       
        
        //initial();
        //PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
	   	//wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
	   	//wl.acquire();
   	   
        //PixNet
        dst = new RectF(0,0,480,800);
     // 设置全屏模式
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
           //WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        // 去除标题栏
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
       
		//String S;
  		//Glue glue ;
		//glue = (Glue) getApplication();
		//S = glue.getSerial();
		//Log.e("data",S);

		


        

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        
        mSurView.startSensor();
        
    }
    
    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    	
    	mSurView.stopSensor();
    	
    	//wl.release();
    }
    
    
    public void writeFile(String fileName, String writestr) throws IOException{
   	 try{
   		 FileOutputStream fout = openFileOutput(fileName,MODE_PRIVATE);
   		 
   		 byte[] bytes = writestr.getBytes();
   		 fout.write(bytes);
   		 fout.close();
   		 
   	 }
   	 catch(Exception e){
   		 e.printStackTrace();
   	 }
    }
      
    public String readFile(String fileName) throws IOException{
   	
   	 String res = "";
   	 try{
   		 FileInputStream fin = openFileInput(fileName);
   		 int length = fin.available();
   		 byte [] buffer = new byte[length];
   		 fin.read(buffer);
   		 res = EncodingUtils.getString(buffer,"UTF-8");
   		 fin.close();
   	 }
   	 catch(Exception e){
   		 e.printStackTrace();
   	 }
   	 
   	 return res;
   	 
    }
    


    
    public class SurView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener
    {

        private ToneGenerator toneGenerator;
        
    	private Sensor mAccelerometer;
        
    	private byte[] mBufArray =null;	//bytes read from file to send
    	
        int[] mAccList = null;
        int curAccIndex = 0;
        int accListLen = 10;
        int sumAccList = 0;
        float sumAccThreshold = (float) (98*accListLen*0.2);
        float sumAccStable = (float) (98*accListLen);
        int highAccCnt = 0;
        int lowAccCnt = 0;
        int neededAccCnt = 10;
        
        
    	private DrawThread _thread;
    	private long lLastTimeStamp = 0;
    	
    	private int mFps = 20;	//!!!!!!!!
    	
    	
    	//------------FPS Control---------------
    	private long mStandardIntervalMs;
    	private long mMaxSleep = 0;
    	private long mSleepMs = 20;	
    	
    	private long defaultSleep = 67;
    	
    	private double mSleepSumMs = 0;
    	private byte mSleepIntervalSumCnt = 0;
    	private byte mSleepIntervalSumNum = 10;	
    	//---------------------------------------
    	
    	//--------switches----------
    	boolean isLogEncoding = false;   	
    	boolean is24Coding = false;   	
    	boolean isDifPlacing = true;
    	int mStrideNum = 20;    	
    	int blockWidth = 16;
    	//---------------------------
    	
    	int originBlockWidth = blockWidth;
    	int maxBlockWidth = blockWidth+5;
    	
    	
    	String logFolderName = "codeLog.txt";
    	
    	int newBlockWidth = 10;
    	
    	int mHeight;
    	int mWidth;
    	int top;
    	int bottom;
    	int left;
    	int right;	
    	int mCodeAreaWidth;
    	int mCodeAreaHeight;
    	int hBlocks;
    	int wBlocks;
    	
    	int xCodeBlocks;
    	int yCodeBlocks;
    	int codeBlockLen;
    	
    	int trackerWidth = 4;
    	
    	boolean bConfigChanged = false;
    	
    	int starterCnt = 0;
    	int starterNum = 4*mFps;
    	boolean bSendStarter = true;
    	boolean bFinished = false;
    	
    	boolean bFileOpened = false;
    	BufferedInputStream inStream = null;
    	File inFile = null;
    	
    	private byte[][] mDataArray = null;
    	private byte[] mCodeArray = null;	//colors of code to display on screen
    	
    	private byte[] mByte2ColorArr = null;	//bytes read from file to send
    	int bufLen = 0;
    	long mSerial = 0; //start from 1, 0 indicate the last frame
    	
    	int flagLen = 1;
    	int headerLen = 3; //serial # : 3 bytes	
    	int crcLen = 2; //CRC checksum : 2 bytes
    	
    	boolean fourColor = true;
    	boolean sevenColor = false;
    	
    	Bitmap mBmp = null;
    	
    	int[] mShowCodeArray;
    	
    	int backgroundClr = Color.BLACK;
    	int[] colorList = null;
    	int mColorStart = 1; //0~7
    	int mColorEnd = 4; //0~7

    	FileWriter mFWriter = null;
    	StringBuilder mStrBuilder = null;
    	
    	String mTargetFile = "alienhead.JPG";	//file to send, put in the root external folder

    	int mDifBorderLen = 0;
    	byte[][] mCodeMapping = null;
    	byte[] mCodeSelect = null;
    	
    	int mImgIndex = 0;
    	
    	
    	public SurView(Context context) 
    	{
    		super(context);
    		  		
    		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    		
    		mAccList = new int[accListLen];
    		for(int i = 0; i<accListLen; i++)
    			mAccList[i] = 98;
    		
    		sumAccList = 98*accListLen;
            
    		
    		// TODO Auto-generated constructor stub
    		getHolder().addCallback(this);
    		_thread = new DrawThread(getHolder(), this);
    	
    		mStandardIntervalMs = 1000/mFps;
    		mSleepSumMs = 0;
    	}

    	public void startSensor() {
			// TODO Auto-generated method stub
    		 mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
			
		}

		public void stopSensor() {
			// TODO Auto-generated method stub
			mSensorManager.unregisterListener(this);
		}

		@Override
    	public void onDraw (Canvas cvs)
    	{   		
    		timingReference_file_starter(cvs);
    		    		
    		//compute interval between frame
    		long curTS = System.nanoTime();
    		
    		if(lLastTimeStamp==0)
    		{
    			lLastTimeStamp = curTS;
    		}
    		else if(curTS<lLastTimeStamp)
    		{
    			lLastTimeStamp = curTS;
    			//Log.e("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$", Long.toString(curTS)+"#"+Long.toString(lLastTimeStamp));
    		}
    		else
    		{
    			long interval = curTS-lLastTimeStamp;
    			lLastTimeStamp = curTS;
    			//System.out.println("#####################: " + interval);
    			
    			//adapt to dynamic system delay
    			mSleepSumMs+=interval/1000000;
    			mSleepIntervalSumCnt++;
    			
    			if(mSleepIntervalSumCnt==mSleepIntervalSumNum)
    			{
    				Double d = new Double(mSleepSumMs/mSleepIntervalSumNum);
    				long avg = d.longValue();
    				
    				mSleepMs += (long)((mStandardIntervalMs-avg)/2);
    				
    				//Log.e(">>>>>>avg",Long.toString(avg));
    				 				
    				mSleepIntervalSumCnt=0;
    				mSleepSumMs=0;
    		
    			}
    		}	
    	}//END FUNC onDraw
		
		private void showBmpCode(Canvas cvs)
		{
			
			String bmpFolderPath = Environment.getExternalStorageDirectory() + 
	    			"/Pictures/";
			
			jBmp = 4;
			iBmp++;
			if(iBmp==11)
			{
				iBmp=1;	
			}
			
			
			String name = Integer.toString(iBmp)+"-"+Integer.toString(jBmp)+".bmp";
			//String name = Integer.toString(1)+"-"+Integer.toString(1)+".bmp";
			Bitmap bmpShow = BitmapFactory.decodeFile(bmpFolderPath+name);
			
			cvs.drawBitmap(bmpShow, null, dst, null);
			
			//Log.e("IJ", name);
		}
    	
    	private void differentiateColor(Canvas cvs)
    	{		
    		
    		
			//*************************************************************
			//-------draw background color-------	
			cvs.drawColor(backgroundClr);
			
			//draw white frame
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(left, top, right+1, bottom+1);
			cvs.drawColor(Color.WHITE);
			cvs.restore();
			
			//------draw RGB in corners-------
			//top left
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(left, top, left+3*blockWidth, top+3*blockWidth);
			cvs.drawColor(Color.GREEN);
			cvs.restore();
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(left+blockWidth, top+blockWidth, left+2*blockWidth, top+2*blockWidth);
			cvs.drawColor(Color.BLACK);
			cvs.restore();
			
			//bottom right
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(right-3*blockWidth+1, bottom-3*blockWidth+1, right+1, bottom+1);
			cvs.drawColor(Color.RED);
			cvs.restore();
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(right-2*blockWidth+1, bottom-2*blockWidth+1, right-blockWidth+1, bottom-blockWidth+1);
			cvs.drawColor(Color.BLACK);
			cvs.restore();
			
			//bottom left
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(left, bottom-3*blockWidth+1, left+3*blockWidth, bottom+1);
			cvs.drawColor(Color.BLUE);
			cvs.restore();
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(left+blockWidth, bottom-2*blockWidth+1, left+2*blockWidth, bottom-blockWidth+1);
			cvs.drawColor(Color.BLACK);
			cvs.restore();
			
			//top right
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(right-3*blockWidth+1, top, right+1, top+3*blockWidth);
			cvs.drawColor(Color.BLUE);
			cvs.restore();
			cvs.save(Canvas.CLIP_SAVE_FLAG);
			cvs.clipRect(right-2*blockWidth+1, top+blockWidth, right-blockWidth+1, top+2*blockWidth);
			cvs.drawColor(Color.BLACK);
			cvs.restore();

			
			int l,t,r,b;
			int c=0;
			
			//*************************************************************
			//display timing reference blocks
			for(int y=0; y<hBlocks; y++)
				for(int x=0; x<wBlocks; x++)
				{			
					//not drawing blocks in timing reference
					if(y==1 || x==1 || y==hBlocks-2 || x==wBlocks-2)
					{								
						if((x+y)%2==0)
						{		
							l = left + blockWidth*x;
							r = l + blockWidth;
							t = top + blockWidth*y;
							b = t + blockWidth;
							
							c = Color.BLACK;	
							cvs.save(Canvas.CLIP_SAVE_FLAG);
							cvs.clipRect(l, t, r, b);
							cvs.drawColor(c);
							cvs.restore();
						}
					
					}
				}
		
			//*************************************************************
			//deal with code area
			int codeTop = top + blockWidth*3;
			int codeBottom = bottom - blockWidth*3;
			int codeLeft = left + blockWidth*3;
			int codeRight = right - blockWidth*3;
			
			if(bSendStarter)
			{	//display starter frame for user prep and auto focus
				for(int y=0; y<yCodeBlocks; y++)
				{
					for(int x=0; x<xCodeBlocks; x++)
					{
						l = codeLeft + blockWidth*x;
						r = l + blockWidth-1;
						t = codeTop + blockWidth*y;
						b = t + blockWidth-1;
						/*l = codeLeft + blockWidth*x;
						t = codeTop + blockWidth*y;
						b = t + blockWidth+2;
						r = l + blockWidth+2;*/
						
						c = Color.BLACK;
						
						cvs.save(Canvas.CLIP_SAVE_FLAG);
						cvs.clipRect(l, t, r, b);
						cvs.drawColor(c);
						cvs.restore();
					}
				}
			}
			else if(bFinished)
			{	//display all white code area indicating finish
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(codeLeft, codeTop, codeRight+1, codeBottom+1);
				cvs.drawColor(Color.WHITE);
				cvs.restore();
			}
			else
			{	//display code area indicating finish
				int colorIndex;
				for(int y=0; y<yCodeBlocks; y++)
					for(int x=0; x<xCodeBlocks; x++)
					{
						l = codeLeft + blockWidth*x;
						r = l + blockWidth;
						t = codeTop + blockWidth*y;
						b = t + blockWidth;
						
						colorIndex = mCodeArray[x+xCodeBlocks*y];
						if(colorIndex<8)
						{
							c = colorList[colorIndex];
						}
						else
						{
							Log.e("XXXX",Integer.toString(mCodeArray[x+xCodeBlocks*y]+1));
						}
						
						cvs.save(Canvas.CLIP_SAVE_FLAG);
						cvs.clipRect(l, t, r, b);
						cvs.drawColor(c);
						cvs.restore();
					}
			}
			
		}//END FUNC------------------------------------------------------------
    			
    			
    	
    	private void showCode(Canvas cvs)
    	{		
    		//IN USE
    		
    		
				//*************************************************************
				//-------draw background color-------	
				cvs.drawColor(backgroundClr);
				
				//draw white frame
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(left, top, right+1, bottom+1);
				cvs.drawColor(Color.WHITE);
				cvs.restore();
				
				//------draw RGB in corners-------
				//top left
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(left, top, left+3*blockWidth, top+3*blockWidth);
				cvs.drawColor(Color.GREEN);
				cvs.restore();
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(left+blockWidth, top+blockWidth, left+2*blockWidth, top+2*blockWidth);
				cvs.drawColor(Color.BLACK);
				cvs.restore();
				
				//bottom right
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(right-3*blockWidth+1, bottom-3*blockWidth+1, right+1, bottom+1);
				cvs.drawColor(Color.RED);
				cvs.restore();
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(right-2*blockWidth+1, bottom-2*blockWidth+1, right-blockWidth+1, bottom-blockWidth+1);
				cvs.drawColor(Color.BLACK);
				cvs.restore();
				
				//bottom left
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(left, bottom-3*blockWidth+1, left+3*blockWidth, bottom+1);
				cvs.drawColor(Color.BLUE);
				cvs.restore();
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(left+blockWidth, bottom-2*blockWidth+1, left+2*blockWidth, bottom-blockWidth+1);
				cvs.drawColor(Color.BLACK);
				cvs.restore();
				
				//top right
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(right-3*blockWidth+1, top, right+1, top+3*blockWidth);
				cvs.drawColor(Color.BLUE);
				cvs.restore();
				cvs.save(Canvas.CLIP_SAVE_FLAG);
				cvs.clipRect(right-2*blockWidth+1, top+blockWidth, right-blockWidth+1, top+2*blockWidth);
				cvs.drawColor(Color.BLACK);
				cvs.restore();

				
				int l,t,r,b;
				int c=0;
				
				//*************************************************************
				//display timing reference blocks
				for(int y=0; y<hBlocks; y++)
					for(int x=0; x<wBlocks; x++)
					{			
						//not drawing blocks in timing reference
						if(y==1 || x==1 || y==hBlocks-2 || x==wBlocks-2)
						{								
							if((x+y)%2==0)
							{		
								l = left + blockWidth*x;
								r = l + blockWidth;
								t = top + blockWidth*y;
								b = t + blockWidth;
								
								c = Color.BLACK;	
								cvs.save(Canvas.CLIP_SAVE_FLAG);
								cvs.clipRect(l, t, r, b);
								cvs.drawColor(c);
								cvs.restore();
							}
						
						}
					}
			
				//*************************************************************
				//deal with code area
				int codeTop = top + blockWidth*3;
				int codeBottom = bottom - blockWidth*3;
				int codeLeft = left + blockWidth*3;
				int codeRight = right - blockWidth*3;
				
				/*if(bSendStarter)
				{	//display starter frame for user prep and auto focus
					for(int y=0; y<yCodeBlocks; y++)
					{
						for(int x=0; x<xCodeBlocks; x++)
						{
							l = codeLeft + blockWidth*x;
							r = l + blockWidth-1;
							t = codeTop + blockWidth*y;
							b = t + blockWidth-1;
							
							c = Color.BLACK;
							
							cvs.save(Canvas.CLIP_SAVE_FLAG);
							cvs.clipRect(l, t, r, b);
							cvs.drawColor(c);
							cvs.restore();
						}
					}
				}*/
				/*else if(bFinished)
				{	//display all white code area indicating finish
					cvs.save(Canvas.CLIP_SAVE_FLAG);
					cvs.clipRect(codeLeft, codeTop, codeRight+1, codeBottom+1);
					cvs.drawColor(Color.WHITE);
					cvs.restore();
				}*/
				//else
				{	//display code area indicating finish
					int colorIndex;
					for(int y=0; y<yCodeBlocks; y++)
						for(int x=0; x<xCodeBlocks; x++)
						{
							l = codeLeft + blockWidth*x;
							r = l + blockWidth;
							t = codeTop + blockWidth*y;
							b = t + blockWidth;
							
							colorIndex = mShowCodeArray[x+xCodeBlocks*y];
							if(colorIndex<8)
							{
								c = colorList[colorIndex];
							}
							else
							{
								Log.e("XXXX",Integer.toString(mCodeArray[x+xCodeBlocks*y]+1));
							}
							
							cvs.save(Canvas.CLIP_SAVE_FLAG);
							cvs.clipRect(l, t, r, b);
							cvs.drawColor(c);
							cvs.restore();
						}
				}
    	}//END FUNC------------------------------------------------------------
    	
    	private void timingReference_file_starter(Canvas cvs)
    	{	
    		//IN USE
    		
    		//*************************************************************
    		//-------draw background color-------	
    		cvs.drawColor(backgroundClr);
    		
    		//draw white frame
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(left, top, right+1, bottom+1);
    		cvs.drawColor(Color.WHITE);
    		cvs.restore();
    		
    		//------draw RGB in corners-------
    		//top left
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(left, top, left+3*blockWidth, top+3*blockWidth);
    		cvs.drawColor(Color.GREEN);
    		cvs.restore();
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(left+blockWidth, top+blockWidth, left+2*blockWidth, top+2*blockWidth);
    		cvs.drawColor(Color.BLACK);
    		cvs.restore();
    		
    		//bottom right
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(right-3*blockWidth+1, bottom-3*blockWidth+1, right+1, bottom+1);
    		cvs.drawColor(Color.RED);
    		cvs.restore();
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(right-2*blockWidth+1, bottom-2*blockWidth+1, right-blockWidth+1, bottom-blockWidth+1);
    		cvs.drawColor(Color.BLACK);
    		cvs.restore();
    		
    		//bottom left
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(left, bottom-3*blockWidth+1, left+3*blockWidth, bottom+1);
    		cvs.drawColor(Color.BLUE);
    		cvs.restore();
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(left+blockWidth, bottom-2*blockWidth+1, left+2*blockWidth, bottom-blockWidth+1);
    		cvs.drawColor(Color.BLACK);
    		cvs.restore();
    		
    		//top right
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(right-3*blockWidth+1, top, right+1, top+3*blockWidth);
    		cvs.drawColor(Color.BLUE);
    		cvs.restore();
    		cvs.save(Canvas.CLIP_SAVE_FLAG);
    		cvs.clipRect(right-2*blockWidth+1, top+blockWidth, right-blockWidth+1, top+2*blockWidth);
    		cvs.drawColor(Color.BLACK);
    		cvs.restore();

    		
    		int l,t,r,b;
    		int c=0;
    		
    		//*************************************************************
    		//display timing reference blocks
    		for(int y=0; y<hBlocks; y++)
    			for(int x=0; x<wBlocks; x++)
    			{			
    				//not drawing blocks in timing reference
    				if(y==1 || x==1 || y==hBlocks-2 || x==wBlocks-2)
    				{								
    					if((x+y)%2==0)
    					{		
    						l = left + blockWidth*x;
    						r = l + blockWidth;
    						t = top + blockWidth*y;
    						b = t + blockWidth;
    						
    						c = Color.BLACK;	
    						cvs.save(Canvas.CLIP_SAVE_FLAG);
    						cvs.clipRect(l, t, r, b);
    						cvs.drawColor(c);
    						cvs.restore();
    					}
    				
    				}
    			}
    	
    		//*************************************************************
    		//deal with code area
    		int codeTop = top + blockWidth*3;
    		int codeBottom = bottom - blockWidth*3;
    		int codeLeft = left + blockWidth*3;
    		int codeRight = right - blockWidth*3;
    		
    		if(bSendStarter)
    		{	//display starter frame for user prep and auto focus
    			for(int y=0; y<yCodeBlocks; y++)
    			{
    				for(int x=0; x<xCodeBlocks; x++)
    				{
    					l = codeLeft + blockWidth*x;
    					r = l + blockWidth-1;
    					t = codeTop + blockWidth*y;
    					b = t + blockWidth-1;
    					/*l = codeLeft + blockWidth*x;
    					t = codeTop + blockWidth*y;
    					b = t + blockWidth+2;
    					r = l + blockWidth+2;*/
    					
    					c = Color.BLACK;
    					
    					cvs.save(Canvas.CLIP_SAVE_FLAG);
    					cvs.clipRect(l, t, r, b);
    					cvs.drawColor(c);
    					cvs.restore();
    				}
    			}
    		}
    		else if(bFinished)
    		{	//display all white code area indicating finish
    			cvs.save(Canvas.CLIP_SAVE_FLAG);
    			cvs.clipRect(codeLeft, codeTop, codeRight+1, codeBottom+1);
    			cvs.drawColor(Color.WHITE);
    			cvs.restore();
    		}
    		else
    		{	//display code area indicating finish
    			int colorIndex;
    			
    			byte[] codeArr = mCodeArray;   			 			
    			   			
    			
    			if(is24Coding || isDifPlacing)
    				codeArr = mCodeSelect;
    			
    			for(int y=0; y<yCodeBlocks; y++)
    				for(int x=0; x<xCodeBlocks; x++)
    				{
    					l = codeLeft + blockWidth*x;
    					r = l + blockWidth;
    					t = codeTop + blockWidth*y;
    					b = t + blockWidth;
    					
    					colorIndex = codeArr[x+xCodeBlocks*y];
    					if(colorIndex<8)
    					{
    						c = colorList[colorIndex];
    					}
    					else
    					{
    						Log.e("XXXX",Integer.toString(mCodeArray[x+xCodeBlocks*y]+1));
    					}
    					
    					    					
    					cvs.save(Canvas.CLIP_SAVE_FLAG);
    					cvs.clipRect(l, t, r, b);
    					cvs.drawColor(c);
    					cvs.restore();
    				}
    		}
    		
    	}//END FUNC------------------------------------------------------------
    	
    	
    	
    	private void byte2FourColors(byte b, byte[] cArr)
    	{

            
    		byte colorIndex = 1;
    		
    		//1st 2 bit
    		colorIndex=1;
    		if ( (b&0x80)!=0 )
    			colorIndex+=2;
    		if ( (b&0x40)!=0 )
    			colorIndex+=1;
    		cArr[0] = colorIndex;
    		
    		//2nd 2 bit
    		colorIndex=1;
    		if ( (b&0x20)!=0 )
    			colorIndex+=2;
    		if ( (b&0x10)!=0 )
    			colorIndex+=1;
    		cArr[1] = colorIndex;
    		
    		//3rd 2 bit
    		colorIndex=1;
    		if ( (b&0x08)!=0 )
    			colorIndex+=2;
    		if ( (b&0x04)!=0 )
    			colorIndex+=1;
    		cArr[2] = colorIndex;
    		
    		//4th 2 bit
    		colorIndex=1;
    		if ( (b&0x02)!=0 )
    			colorIndex+=2;
    		if ( (b&0x01)!=0 )
    			colorIndex+=1;
    		cArr[3] = colorIndex;
    	}
    	
    	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) 
    	{
    		// TODO Auto-generated method stub
    		
    	}

    	public void surfaceCreated(SurfaceHolder holder) 
    	{
    		//initialize vibrator and beeper
            //Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
            //toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
            //vibrator.vibrate(1000);
                        
            
    		// TODO Auto-generated method stub
    		_thread.setRunning(true);
    		_thread.start();
    		
    		//-------compute drawing area-------
    		mHeight = this.getHeight();
    		mWidth = this.getWidth();	
    		
    		//hBlocks = Math.round((float)(mHeight-blockWidth)/blockWidth);
    		//wBlocks = Math.round((float)(mWidth-blockWidth)/blockWidth);
    		
    		//compute code size in # of blocks
    		hBlocks = mHeight/blockWidth;
    		wBlocks = mWidth/blockWidth;	
    		if(hBlocks%2==0)
    		{
    			hBlocks--;
    		}		
    		if(wBlocks%2==0)
    		{
    			wBlocks--;
    		}
    		
    		//compute code corners
    		top = (int)((mHeight-(hBlocks*blockWidth))/2)-1;
    		bottom = top+(hBlocks*blockWidth)-1;
    		left = (int)((mWidth-(wBlocks*blockWidth))/2)-1;
    		right = left+(wBlocks*blockWidth)-1;	
    		
    		//portrait mode, x->width y->height, width<height
    		xCodeBlocks = wBlocks-6;
    		yCodeBlocks = hBlocks-6;
    		
    		codeBlockLen = yCodeBlocks*xCodeBlocks;
    		
    		//compute number of bytes transfered for on screen of code
    		if(fourColor)
    		{
    			bufLen = (xCodeBlocks*yCodeBlocks)*2/8 - headerLen - crcLen - flagLen;
    			mBufArray = new byte[bufLen];	//move to run()
    			mByte2ColorArr = new byte[4];	//move to run()
    		}		
    		else if(sevenColor)
    		{
    			//bufLen = (xCodeBlocks*yCodeBlocks)*2/8 - headerLen - crcLen;
    			//mBufArray = new byte[bufLen];
    		}
    		
    		String str = ":"+xCodeBlocks+"X"+yCodeBlocks;
    		Log.e("xCodeBlocks X yCodeBlocks",str);
    		
    		    		
    		//------differentiate colors-------
    		colorList = new int[8];
    		colorList[0]=Color.BLACK;	
    		colorList[1]=Color.RED;
    		colorList[2]=Color.GREEN;
    		colorList[3]=Color.BLUE;
    		colorList[4]=Color.WHITE;
    		colorList[5]=Color.MAGENTA;
    		colorList[6]=Color.YELLOW;
    		colorList[7]=Color.CYAN;
    				
    		//mDataArray = new byte[hBlocks][wBlocks];
    		mCodeArray = new byte[codeBlockLen];

    		
    		//---------customize showCode display-----------
    		/*int preClr = 1;
    		mShowCodeArray = new int[codeBlockLen];
    		for(int i = 0; i<mShowCodeArray.length; i++)
    		{
    			if(Math.random()<0.1)
    				mShowCodeArray[i] = preClr;
    			else
    				mShowCodeArray[i] = (int)(Math.random()*100)%4+1;
    			
    			preClr = mShowCodeArray[i];
    		}*/
    		
    		//---------create code scheme mapping-----------
    		mCodeMapping = new byte[24][4];
    		mCodeSelect = new byte[codeBlockLen];
    		
    		mCodeMapping[0][0] = 1;
    		mCodeMapping[0][1] = 2;
    		mCodeMapping[0][2] = 3;
    		mCodeMapping[0][3] = 4;
    		
    		mCodeMapping[1][0] = 1;
    		mCodeMapping[1][1] = 2;
    		mCodeMapping[1][2] = 4;
    		mCodeMapping[1][3] = 3;
    		
    		mCodeMapping[2][0] = 1;
    		mCodeMapping[2][1] = 3;
    		mCodeMapping[2][2] = 2;
    		mCodeMapping[2][3] = 4;
    		
    		mCodeMapping[3][0] = 1;
    		mCodeMapping[3][1] = 3;
    		mCodeMapping[3][2] = 4;
    		mCodeMapping[3][3] = 2;
    		
    		mCodeMapping[4][0] = 1;
    		mCodeMapping[4][1] = 4;
    		mCodeMapping[4][2] = 2;
    		mCodeMapping[4][3] = 3;
    		
    		mCodeMapping[5][0] = 1;
    		mCodeMapping[5][1] = 4;
    		mCodeMapping[5][2] = 3;
    		mCodeMapping[5][3] = 2;
    		
    		mCodeMapping[6][0] = 2;
    		mCodeMapping[6][1] = 1;
    		mCodeMapping[6][2] = 3;
    		mCodeMapping[6][3] = 4;
    		
    		mCodeMapping[7][0] = 2;
    		mCodeMapping[7][1] = 1;
    		mCodeMapping[7][2] = 4;
    		mCodeMapping[7][3] = 3;
    		
    		mCodeMapping[8][0] = 2;
    		mCodeMapping[8][1] = 3;
    		mCodeMapping[8][2] = 1;
    		mCodeMapping[8][3] = 4;
    		
    		mCodeMapping[9][0] = 2;
    		mCodeMapping[9][1] = 3;
    		mCodeMapping[9][2] = 4;
    		mCodeMapping[9][3] = 1;
    		
    		mCodeMapping[10][0] = 2;
    		mCodeMapping[10][1] = 4;
    		mCodeMapping[10][2] = 1;
    		mCodeMapping[10][3] = 3;
    		
    		mCodeMapping[11][0] = 2;
    		mCodeMapping[11][1] = 4;
    		mCodeMapping[11][2] = 3;
    		mCodeMapping[11][3] = 1;
    		
    		mCodeMapping[12][0] = 3;
    		mCodeMapping[12][1] = 1;
    		mCodeMapping[12][2] = 2;
    		mCodeMapping[12][3] = 4;
    		
    		mCodeMapping[13][0] = 3;
    		mCodeMapping[13][1] = 1;
    		mCodeMapping[13][2] = 4;
    		mCodeMapping[13][3] = 2;
    		
    		mCodeMapping[14][0] = 3;
    		mCodeMapping[14][1] = 2;
    		mCodeMapping[14][2] = 1;
    		mCodeMapping[14][3] = 4;
    		
    		mCodeMapping[15][0] = 3;
    		mCodeMapping[15][1] = 2;
    		mCodeMapping[15][2] = 4;
    		mCodeMapping[15][3] = 1;
    		
    		mCodeMapping[16][0] = 3;
    		mCodeMapping[16][1] = 4;
    		mCodeMapping[16][2] = 1;
    		mCodeMapping[16][3] = 2;
    		
    		mCodeMapping[17][0] = 3;
    		mCodeMapping[17][1] = 4;
    		mCodeMapping[17][2] = 2;
    		mCodeMapping[17][3] = 1;
    		
    		mCodeMapping[18][0] = 4;
    		mCodeMapping[18][1] = 1;
    		mCodeMapping[18][2] = 2;
    		mCodeMapping[18][3] = 3;
    		
    		mCodeMapping[19][0] = 4;
    		mCodeMapping[19][1] = 1;
    		mCodeMapping[19][2] = 3;
    		mCodeMapping[19][3] = 2;
    		
    		mCodeMapping[20][0] = 4;
    		mCodeMapping[20][1] = 2;
    		mCodeMapping[20][2] = 1;
    		mCodeMapping[20][3] = 3;
    		
    		mCodeMapping[21][0] = 4;
    		mCodeMapping[21][1] = 2;
    		mCodeMapping[21][2] = 3;
    		mCodeMapping[21][3] = 1;
    		
    		mCodeMapping[22][0] = 4;
    		mCodeMapping[22][1] = 3;
    		mCodeMapping[22][2] = 1;
    		mCodeMapping[22][3] = 2;
    		
    		mCodeMapping[23][0] = 4;
    		mCodeMapping[23][1] = 3;
    		mCodeMapping[23][2] = 2;
    		mCodeMapping[23][3] = 1;
    		
    	}//END FUNC-------------------------------------------------------------
    	
    	
    	public void change2NewContext(Byte[] context){
    		_thread = new DrawThread(getHolder(), this);
    	}
    	public void surfaceDestroyed(SurfaceHolder holder) 
    	{
    		// TODO Auto-generated method stub
    		boolean retry = true;
    		    _thread.setRunning(false);
    	    while (retry) 
    	    {
    	        try {
    	            _thread.join();
    	            retry = false;
    	        } catch (InterruptedException e) {
    	            // we will try it again and again...
    	        }
    	    }
    	    
    	    if(inStream!=null)
    	    {
    	    	try {
    				inStream.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	    }
    	}
    	
    	class DrawThread extends Thread
    	{
    		private SurView _surView;
    		private SurfaceHolder _surfaceHolder;
    		private boolean _run = false;
    		
    		private int runCnt = 0;
    		
    		
    		
    		
    		public DrawThread (SurfaceHolder sH, SurView sV)
    		{
    			_surView = sV;
    			_surfaceHolder = sH;
    		}
    		
    		public void setRunning(boolean run)
    		{
    			_run = run;
    		}
    		
    		@Override
    		public void run()
    		{
    			Process.setThreadPriority(-20);
    			    						
    			if(!bFileOpened)
    			{
    				inFile = new File(Environment.getExternalStorageDirectory()+"/Pictures", mTargetFile);
    					
    				try {
    					inStream = new BufferedInputStream(new FileInputStream(inFile));
    				} catch (FileNotFoundException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}

    				bFileOpened = true;
    			}
    			
    			Canvas c;
    			byte mSerial_1 = 0; 	//000 indicate the last frame
    			byte mSerial_2 = 0;
    			byte mSerial_3 = 0;
    			
    		    while (_run) 
    		    {
    		    	//check acc
    		    	//Log.e("ACC", Integer.toString(sumAccList));
    		    	float sumacc = sumAccList;
    		    	if(Math.abs(sumAccList-sumAccStable)>sumAccThreshold)
    		    	{
    		    		
    		    		highAccCnt++;
    		    		lowAccCnt = 0;
    		    		
    		    		if(highAccCnt==neededAccCnt)
    		    		{
    		    			//increase block size;
    		    			
    		    			highAccCnt = 0;
    		    			
		    				newBlockWidth = maxBlockWidth;
		    				bConfigChanged = true;
		    			 		    			  		    			
    		    		}
    		    	}
    		    	else
    		    	{
    		    		lowAccCnt++;
    		    		highAccCnt = 0;
    		    		
    		    		if(lowAccCnt==neededAccCnt)
    		    		{
    		    			//increase block size;
    		    			
    		    			lowAccCnt = 0;
    		    			
    		    			if(blockWidth-1>=originBlockWidth)
    		    			{
    		    				newBlockWidth = blockWidth-1;
    		    				bConfigChanged = true;
    		    			}	    				
		    			 		    			  		    			
    		    		}
    		    	}  		    	
    		    	
    		    	if(isShowBmps)
    		    	{
    		    		
    		    		//------------------update screen-----------------------
        		        c = null;
        		        try 
        		        {
        		            c = _surfaceHolder.lockCanvas(null);
        		            synchronized (_surfaceHolder) {
        		                _surView.onDraw(c);
        		            }
        		        } 
        		        finally 
        		        {
        		            // do this in a finally so that if an exception is thrown
        		            // during the above, we don't leave the Surface in an
        		            // inconsistent state
        		            if (c != null) 
        		            {
        		                _surfaceHolder.unlockCanvasAndPost(c);
        		            }
        		        }
        		        
        		        //---------------sleep until next send--------------------
        		        try {
        					sleep(defaultSleep,0); //15fps
        				} catch (InterruptedException e) {
        					e.printStackTrace();
        				}
        		        
        		        continue;
    		    	}
    		    	
    		    	
    		    	//*************************************************************
    		    	//----------configurate screen setting----------
    		    	if(bConfigChanged)
    		    	{
    		    		bConfigChanged = false;
    		    		blockWidth = newBlockWidth;
    		    		
    		    		//compute code size in # of blocks
    		    		hBlocks = mHeight/blockWidth;
    		    		wBlocks = mWidth/blockWidth;	
    		    		if(hBlocks%2==0)
    		    		{
    		    			hBlocks--;
    		    		}		
    		    		if(wBlocks%2==0)
    		    		{
    		    			wBlocks--;
    		    		}
    		    		
    		    		//compute code corners
    		    		top = (int)((mHeight-(hBlocks*blockWidth))/2)-1;
    		    		bottom = top+(hBlocks*blockWidth)-1;
    		    		left = (int)((mWidth-(wBlocks*blockWidth))/2)-1;
    		    		right = left+(wBlocks*blockWidth)-1;	
    		    		
    		    		//portrait mode, x->width y->height, width<height
    		    		xCodeBlocks = wBlocks-6;	//minus 3*2 timing blocks
    		    		yCodeBlocks = hBlocks-6;
    		    		
    		    		//compute number of bytes transfered for on screen of code
    		    		if(fourColor)
    		    		{
    		    			bufLen = (xCodeBlocks*yCodeBlocks)*2/8 - headerLen - crcLen - flagLen;
    		    			mBufArray = null;
    		    			mBufArray = new byte[bufLen];	//move to run()
    		    			mByte2ColorArr = null;
    		    			mByte2ColorArr = new byte[4];	//move to run()
    		    		}		
    		    		else if(sevenColor)
    		    		{
    		    			//bufLen = (xCodeBlocks*yCodeBlocks)*2/8 - headerLen - crcLen;
    		    			//mBufArray = new byte[bufLen];
    		    		}
    		    		
    		    		String str = ":"+xCodeBlocks+"X"+yCodeBlocks;
    		    		Log.e("xCodeBlocks X yCodeBlocks",str);
    		    
    		    		codeBlockLen = xCodeBlocks*yCodeBlocks;
    		    		mCodeArray = null;
    		    		mCodeArray = new byte[codeBlockLen];
    		    		mCodeSelect = null;
    		    		mCodeSelect = new byte[codeBlockLen];
    		    		
    		    		
    		    	}//END reconfiguration
    		    	
    		    	
    		    	
    		    	//*************************************************************
    		    	//-------decide if send starter screen-------
    				if(bSendStarter)
    				{
    					starterCnt++;		
    					if(starterCnt==starterNum)
    					{
    						bSendStarter = false;
    					}  			
    					
    				}
    				else if(bFinished)
    				{
    					
    				}
    				
    				
    				//*************************************************************
    				//-------starter displayed, send content-------
    				else
    				{	
    					runCnt++;
    					int len = 0;
    					
    					//len = inStream.read(mBufArray, 0, bufLen);
    					//System.arraycopy(Single_block,0,mBufArray,0,CODE_SIZE);
    					
    					
    					/*
    					Intent intent = getIntent();
    					Bundle bundle = intent.getExtras();
    					
    					if(bundle.getString("data") != null)
    					{
    						String k = bundle.getString("data");
    						Log.e("data",k);
    					}
    					*/
    					
    	          		//Glue glue;
    	          		//String z;
    	          		//glue = (Glue) getApplication();
    	          		//glue.add();
    	          		//z = glue.getK()+" ";
    					//
    					/*
    					MainActivity main = null;
    					main = (MainActivity) main;
    					int k =  MainActivity.k;
    					Log.e("data",k+"");
    					*/
    					

    	          		  String a = null;
    	          		  try {
							a = readFile("k.txt");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	          		  Log.e("data1",a);
    	          		  if(Integer.parseInt(a) % 3 == 0){
    	          			System.arraycopy(Single_block,0,mBufArray,0,CODE_SIZE);
    	          		  }
    	          		  else if(Integer.parseInt(a) % 3 == 1){
    	          			System.arraycopy(Single_block_1,0,mBufArray,0,CODE_SIZE);
    	          		  }
    	          		  else if(Integer.parseInt(a) % 3 == 2){
    	          			System.arraycopy(Single_block_2,0,mBufArray,0,CODE_SIZE); 
    	          		  }


						len = mBufArray.length;
						Log.e("#mBufArray-length:", Integer.toString(len));
    					
    					//------------send if still content left------------
    					if(len>0)
    					{	
    						
    						//*******************************************************
    						//---------------Compute Serial #-----------------
    						if(len!=bufLen)
    						{
    							bFinished = true;
    							toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK);
    							mSerial=0;	//indicate current is the last frame
    						}	
    						else
    							mSerial++;
    							
    						mSerial_3 = (byte)(mSerial&0xff);
    						mSerial_2 = (byte)((mSerial>>8)&0xff);
    						mSerial_1 = (byte)((mSerial>>16)&0xff);
    						
    						/*long reverseLong = 0;
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
    										
    						Log.e("serial #", Long.toString(reverseLong));*/
    						
    						int pos = 0;
    						byte b;
    						
    						if(mSerial%15==0)
    						{
    							//newBlockWidth++;	//@@@@@@@@@@@@@@@@@@@@@@@@@@@
    							//bConfigChanged = true;
    						}
    												

    						//*******************************************************
    						//------------encode flag # into color code--------------
    						if(fourColor)
    						{	
    							//1st byte
    							b = 0;
    							byte2FourColors(b, mByte2ColorArr);
    							for(int n=0; n<4; n++)
    							{
    								mCodeArray[pos] = mByte2ColorArr[n];
    								pos++;
    							}
    						}
    						
    						//*******************************************************
    						//------------encode serial # into color code------------
    						//1st byte
    						b = mSerial_1;
    						byte2FourColors(b, mByte2ColorArr);
    						for(int n=0; n<4; n++)
    						{
    							mCodeArray[pos] = mByte2ColorArr[n];
    							pos++;
    						}
    						//2nd byte
    						b = mSerial_2;
    						byte2FourColors(b, mByte2ColorArr);
    						for(int n=0; n<4; n++)
    						{
    							mCodeArray[pos] = mByte2ColorArr[n];
    							pos++;
    						}
    						//3rd byte
    						b = mSerial_3;
    						byte2FourColors(b, mByte2ColorArr);
    						for(int n=0; n<4; n++)
    						{
    							mCodeArray[pos] = mByte2ColorArr[n];
    							pos++;
    						}
    						
    						//*******************************************************
    						//------------encode data in buf to color code------------
    						for(int i=0; i<len; i++)
    						{
    							b = mBufArray[i];	
    							//Log.e("bit::", Byte.toString(b));
    							
    							//-------------encode use 4 colors----------------
    							if(fourColor)
    							{	
    								byte2FourColors(b, mByte2ColorArr);
    								for(int n=0; n<4; n++)
    								{
    									mCodeArray[pos] = mByte2ColorArr[n];
    									pos++;
    								}									
    							}//END encode 4 colors
    							
    						}//END sending data in mBufArray
    						

    						//*******************************************************
    						//---------------encode CRC to color code---------------
    						b = 0;
    						byte2FourColors(b, mByte2ColorArr);
    						for(int n=0; n<4; n++)
    						{
    							mCodeArray[pos] = mByte2ColorArr[n];
    							pos++;
    						}
    						b = 0;
    						byte2FourColors(b, mByte2ColorArr);
    						for(int n=0; n<4; n++)
    						{		
    							mCodeArray[pos] = mByte2ColorArr[n];
    							pos++;
    						}
    						
    						//*******************************************************
    						//-----------------padding 1 in the end-----------------
    						while(pos<mCodeArray.length)
    						{							
    							mCodeArray[pos] = 1;
    							pos++;
    						}
    						
    						
    						//*******************************************************
    						//---------------Choose Coding Scheme (24)---------------
    						if(is24Coding)
    						{
    							int start = 4;
    							int bestScheme = 0;
    							int minBorderLen = codeBlockLen*codeBlockLen;
    							
    							int origBorderLen = 0;
    							
    							byte curColor, rightColor, dnColor;
    							
    							//padding 0~start
    							for(int i = 0; i<start; i++)
    								mCodeSelect[i] = mCodeArray[i];
    							
    							for(int schemeIndex = 0; schemeIndex<24; schemeIndex++)
    							{								
    								//transfer to new color scheme
    								for(int i = start; i<codeBlockLen; i++)
    									mCodeSelect[i] = mCodeMapping[schemeIndex][mCodeArray[i]-1];

    								//compute border length
    								int borderLen = 0;
    								for(int y = 0; y<yCodeBlocks; y++)
    									for(int x = 0; x<xCodeBlocks; x++)
    									{
    										curColor = mCodeSelect[y*xCodeBlocks+x];
    										
    										if(y!=yCodeBlocks-1)
    										{
    											dnColor = mCodeSelect[(y+1)*xCodeBlocks+x];
    											if(dnColor!=curColor)
    												borderLen++;
    										}
    										if(x!=xCodeBlocks-1)
    										{
    											rightColor = mCodeSelect[y*xCodeBlocks+x+1];
    											if(rightColor!=curColor)
    												borderLen++;
    										}
    									}
    								
    								if(schemeIndex==0)
    									origBorderLen = borderLen;
    								if(borderLen<minBorderLen)
    								{
    									minBorderLen = borderLen;
    									bestScheme = schemeIndex;
    								}
    								
    								Log.e(Integer.toString(schemeIndex), Integer.toString(borderLen) );
    							}//END for Scheme
    							
    							
    							//set flag as best scheme mapping
    							for(int i = 0; i<start; i++)
    								mCodeSelect[i] = mCodeMapping[bestScheme][i];
    							//transfer to best color scheme
    							for(int i = start; i<codeBlockLen; i++)
    								mCodeSelect[i] = mCodeMapping[bestScheme][mCodeArray[i]-1];
    							
    							//Log.e("**************", "***********************************************************" );
    							//Log.e("Code Scheme", Integer.toString(bestScheme) );
    							//Log.e("Border Length Dif", Integer.toString(origBorderLen-minBorderLen) );
    							
    						}//END CODE 24
    						
    						
    						
    						//*******************************************************
    						//---------------choose differnet stride in coding---------------
    						if(isDifPlacing)
    						{
    							int start = 16;
    							int bestStride = 0;
    							int minBorderLen = codeBlockLen*codeBlockLen;
    							int schemeNum = mStrideNum;
    							
    							int origBorderLen = 0;
    							
    							byte curColor, rightColor, dnColor;
    							
    							//padding 0~start
    							for(int i = 0; i<start; i++)
    								mCodeSelect[i] = mCodeArray[i];
    							
    							for(int stride = 1; stride<=schemeNum; stride++)
    							{		
    								int curPos = start;
    								
    								//transfer to new color scheme
    								for(int i = 0; i<stride; i++)
    								{
    									int scale = 0;
    									
    									int index = start+scale*stride+i;
    									while(index<codeBlockLen)
    									{
    										mCodeSelect[curPos] = mCodeArray[index];
    										curPos++;
    										scale++;
    										
    										index = start+scale*stride+i;
    									}
    								}
    								

    								//compute border length
    								int borderLen = 0;
    								for(int y = 0; y<yCodeBlocks; y++)
    									for(int x = 0; x<xCodeBlocks; x++)
    									{
    										curColor = mCodeSelect[y*xCodeBlocks+x];
    										
    										if(y!=yCodeBlocks-1)
    										{
    											dnColor = mCodeSelect[(y+1)*xCodeBlocks+x];
    											if(dnColor!=curColor)
    												borderLen++;
    										}
    										if(x!=xCodeBlocks-1)
    										{
    											rightColor = mCodeSelect[y*xCodeBlocks+x+1];
    											if(rightColor!=curColor)
    												borderLen++;
    										}
    									}
    								
    								if(stride==1)
    									origBorderLen = borderLen;
    								if(borderLen<minBorderLen)
    								{
    									minBorderLen = borderLen;
    									bestStride = stride;
    								}
    								
    								//Log.e(Integer.toString(stride), Integer.toString(borderLen) );
    							}//END for Scheme
    							
    							
    							//set flag as best scheme mapping
    							b = (byte)bestStride;
    							byte2FourColors(b, mByte2ColorArr);
    							for(int n=0; n<4; n++)
    							{
    								mCodeSelect[n] = mByte2ColorArr[n];
    							}
    							
    							//transfer to best color scheme
    							int curPos = start;
    							for(int i = 0; i<bestStride; i++)
    							{
    								int scale = 0;
    								
    								int index = start+scale*bestStride+i;
    								while(index<codeBlockLen)
    								{
    									mCodeSelect[curPos] = mCodeArray[index];
    									curPos++;
    									scale++;
    									index = start+scale*bestStride+i;
    								}
    							}

    							//Log.e("Border Length Dif", Integer.toString(origBorderLen-minBorderLen) );
    							mDifBorderLen = origBorderLen-minBorderLen;
    							
    						}//END DIF Stride
    						
    						
    											
    						//*******************************************************
    						//---------------Log frame encoding into txt-----------
    						if(isLogEncoding)
    						{
    							//initialize writer and string
    							try
    					        {
    								String logFileName = "log"+Long.toString(mSerial)+".txt";
    								File logFile = new File(Environment.getExternalStorageDirectory()+"/codeLog/", logFileName);
    					        	mFWriter = new FileWriter(logFile);	
    					        	mStrBuilder = new StringBuilder();
    							}
    					        catch(Exception e)
    					        {
    							    e.printStackTrace();
    							}
    							
    							byte[] logArr = mCodeArray;
    							if(isDifPlacing)
    								logArr = mCodeSelect;
    							else
    							{
    								//compute border length for log
    								int borderLen = 0;
    								byte curColor, dnColor, rightColor;
    								for(int y = 0; y<yCodeBlocks; y++)
    									for(int x = 0; x<xCodeBlocks; x++)
    									{
    										curColor = mCodeArray[y*xCodeBlocks+x];
    										
    										if(y!=yCodeBlocks-1)
    										{
    											dnColor = mCodeArray[(y+1)*xCodeBlocks+x];
    											if(dnColor!=curColor)
    												borderLen++;
    										}
    										if(x!=xCodeBlocks-1)
    										{
    											rightColor = mCodeArray[y*xCodeBlocks+x+1];
    											if(rightColor!=curColor)
    												borderLen++;
    										}
    									}
    								mDifBorderLen = borderLen;
    							}

    							try
    							{							
    								mFWriter.write(Integer.toString( (int)(mDifBorderLen) ));
    					    		mFWriter.write('\n');
    					    		
    								for (byte bClr : logArr)
    								{								
    						    		mFWriter.write(Integer.toString( (int)(bClr) ));
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
    						
    					}//END if len>0
    					
    					//------------check end of file-----------
    					if(bFinished)
    					{
    						//bSendStarter = true;
    						Log.e("RUN_CNT", Integer.toString(runCnt));
    						try {
    							inStream.close();
    						} catch (IOException e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
    					}
    				}//END fill mCodeArray*******************************************/
    				
    				//------------------update screen-----------------------
    		        c = null;
    		        try 
    		        {
    		            c = _surfaceHolder.lockCanvas(null);
    		            synchronized (_surfaceHolder) {
    		                _surView.onDraw(c);
    		            }
    		        } 
    		        finally 
    		        {
    		            // do this in a finally so that if an exception is thrown
    		            // during the above, we don't leave the Surface in an
    		            // inconsistent state
    		            if (c != null) 
    		            {
    		                _surfaceHolder.unlockCanvasAndPost(c);
    		            }
    		        }
    		        
    		        //---------------sleep until next send--------------------
    		        try {
    		        	if(mSleepMs<=0 || mSleepMs>mMaxSleep)	//>standard?//!!!!!!!!
    		        		mSleepMs=defaultSleep;	//standard-25//!!!!!!!!
    					sleep(defaultSleep,0); //15fps
    				} catch (InterruptedException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			
    		    }//END while
    		    
    		}//END run()
    	}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			
			
		}

		public void onSensorChanged(SensorEvent event) 
		{			
			if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
	            return;
			
			int x = (int)event.values[0];
			int y = (int)event.values[1];
			int z = (int)event.values[2];
	        
			//float sum = Math.abs(x)+Math.abs(y)+Math.abs(z);
			int sum = x*x+y*y+z*z;
			
			curAccIndex++;
			if(curAccIndex>=accListLen)
				curAccIndex = 0;
			
			sumAccList = sumAccList-mAccList[curAccIndex]+sum;
			
			mAccList[curAccIndex] = sum;
			
			
		}

    }//END SURVIEW>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    //END SURVIEW>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
}