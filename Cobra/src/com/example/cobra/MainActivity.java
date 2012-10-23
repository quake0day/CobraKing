package com.example.cobra;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	class MyView extends SurfaceView implements SurfaceHolder.Callback{
		private SurfaceView mSurfaceview = null; // SurfaceView object: charge of Video display
		private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder object: support Surfaceview
		private Camera mCamera = null; // Camera object, for preview
		private static final String TAG = "MyActivity";
		boolean bIfPreview;
		
		public MyView(Context context) {
			super(context);
			initSurfaceView();
			// TODO Auto-generated constructor stub
		}

		// Define object

		
	//InitSurfaceView
	private void initSurfaceView()
	{
		mSurfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceview.getHolder(); // bind SurfaceView, and get SurfaceHolder object
		mSurfaceHolder.addCallback((Callback) MainActivity.this); // Add Callback interface to SurfaceHolder
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // Set type of display
		
	}
	
	/* SurfaceHolder.Callback Callback func */
	public void surfaceCrated(SurfaceHolder holder)
	// Each time SurfaceView start/the first time object, when the preview window been created
	// This func will be called.
	{
		int defaultCameraId = 0;
		//Find the total number of cameras avaiable
		int numberOfCameras = Camera.getNumberOfCameras();
		//Find the id of the default camera
		CameraInfo cameraInfo = new CameraInfo();
		for(int i = 0; i<numberOfCameras;i++)
		{
			mCamera.getCameraInfo(i, cameraInfo);
			if(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) // Obtain front Camera's ID
			{
				defaultCameraId = i;
			}
		}
		mCamera = Camera.open(defaultCameraId);  //open front camera
		
		try{
			Log.i(TAG,"SurfaceHolder.Callback: surface Created");
		} catch (Exception e){
			if ( null != mCamera)
			{
				mCamera.release();
				mCamera = null;
			}
			Log.i(TAG+"initCamera",e.getMessage());
			
		}
		
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Log.i(TAG,"SurfaceHolder.Callback: Surface Changed");
		
		initCamera();
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.i(TAG,"SurfaceHolder.Callback:Surface Destroyed");
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null); // this should put first
			mCamera.stopPreview();
			bIfPreview = false;
			mCamera.release();
			mCamera = null;
		}
	}
	
	/* preview the Camera */
	private void initCamera() // using in surfaceChange
	{
		Log.i(TAG,"going into initCamera");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void onResume(){
    	setContentView(new MyView(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
