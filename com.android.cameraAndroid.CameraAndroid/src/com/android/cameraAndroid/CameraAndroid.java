package com.android.cameraAndroid;

import
 android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "NewApi" })
public class CameraAndroid extends Activity {

	private CameraPreview preview;
	private Camera camera;
	private ToneGenerator tone;
	private static final int OPTION_SNAPSHOT = 0;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	private static final int MEDIA_TYPE_VIDEO = 2;
	private Uri fileUri;
	private File file;
	ImageView img;
	
	// Create a arraylist to store bitmap
	ArrayList<Bitmap> rev = new ArrayList<Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		preview = new CameraPreview(this,camera);
		setContentView(R.layout.main);
		//create new Intent
		//Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
		File sdcard = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);

		file = new File(sdcard,"/MyCameraApp/VID_20121013_125228.mp4");
		
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try{
			retriever.setDataSource(file.getAbsolutePath());
			for(int i = 1000;i<5000;i=i+1000)
			{
			//img.setImageBitmap(retriever.getFrameAtTime(10,MediaMetadataRetriever.OPTION_CLOSEST));
			Bitmap bitmap = retriever.getFrameAtTime(i,MediaMetadataRetriever.OPTION_CLOSEST);
			int w = bitmap.getWidth();
			for(int k = 0 ; k < w ; k++)
			{
				int color = bitmap.getPixel(k, 0);
				Log.v("color:", ""+color);

			}
			
			
			rev.add(bitmap);
			
			}
		}catch(IllegalArgumentException ex){
			ex.printStackTrace();
		}
		try {
			saveFrames(rev);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);  // set the video file name
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality
		
		
		startActivityForResult(intent,CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE){
			if(resultCode == RESULT_OK){
				Toast.makeText(this, "Finished! Video save to :\n"+
			data.getData(), Toast.LENGTH_LONG).show();
				
			}else if(resultCode == RESULT_CANCELED){
				
			}else{
				
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch(itemId){
		case OPTION_SNAPSHOT:
			camera.takePicture(shutterCallback, null, jpegCallback);
			break;
		}
		return true;
	}
	
	// Func to save Video frames into Bitmap Arraylist
	public void saveFrames(ArrayList<Bitmap> saveBitmapList) throws IOException{
		Random r  = new Random();
		int folder_id = r.nextInt(1000)+1;

		String folder = Environment.getExternalStorageDirectory()+"/videos/frames/"+folder_id+"/";
		File saveFolder = new File(folder);
		if(! saveFolder.exists()){
			saveFolder.mkdirs();
		}
		
		int i = 1;
		for(Bitmap b: saveBitmapList){
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			
			b.compress(Bitmap.CompressFormat.PNG, 40, bytes);	
			File f = new File(saveFolder,("frame"+i+".png"));	
			f.createNewFile();
			
			FileOutputStream fo = new FileOutputStream(f);
			fo.write(bytes.toByteArray());
			fo.flush();
			fo.close();
			i++;
			
		}
	}
	
	// create a file Uri for saving an image or video
	private static Uri getOutputMediaFileUri(int type){
		return Uri.fromFile(getOutputMediaFile(type));
	}
	// create a file for saveing an image or video
	@TargetApi(8)
	private static File getOutputMediaFile(int type){
		
		File MediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES),"MyCameraApp");
		if(! MediaStorageDir.exists()){
			if (! MediaStorageDir.mkdirs()){
				Log.d("MyCameraApp","failed to create directory");
				return null;
			}
		}
		
		//create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if(type == MEDIA_TYPE_VIDEO){
			mediaFile = new File(MediaStorageDir.getPath()+File.separator+"VID_"+timeStamp+".mp4");
			
		}else{
			return null;
		}
		return mediaFile;
		
	}
	private PictureCallback jpegCallback = new PictureCallback(){

		public void onPictureTaken(byte[] data, Camera camera) {
			Parameters ps = camera.getParameters();
			if(ps.getPictureFormat() == PixelFormat.JPEG){
			    String path = save(data);
			    Uri uri = Uri.fromFile(new File(path));
   			    Intent intent = new Intent();
   			    intent.setAction("android.intent.action.VIEW");
   			    intent.setDataAndType(uri, "image/jpeg");
   			    startActivity(intent);
			}
		}
	};
	
	private ShutterCallback shutterCallback = new ShutterCallback(){
		public void onShutter() {
			if(tone == null)
				tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
						ToneGenerator.MAX_VOLUME);
			tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	};
	
	private String save(byte[] data){
		String path = "/sdcard/"+System.currentTimeMillis()+".jpg";
		try {
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				String storage = Environment.getExternalStorageDirectory().toString();
				StatFs fs = new StatFs(storage);
				long available = fs.getAvailableBlocks()*fs.getBlockSize();
				if(available<data.length){

					return null;
				}
				File file = new File(path);
				if(!file.exists())
					file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(data);
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return path;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_SNAPSHOT, 0, R.string.snapshot);
		return super.onCreateOptionsMenu(menu);
	}

	class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
		private final String TAG = null;
		SurfaceHolder mHolder;
		private Camera mCamera;

		public CameraPreview(Context context, Camera camera) {
			super(context);
			mCamera = camera;
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
		public void surfaceCreated(SurfaceHolder holder) {
			try{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			}catch(IOException e){
				Log.d(TAG,"Error setting camera preview:"+e.getMessage());
			}
		}

		
		public void surfaceDestroyed(SurfaceHolder holder) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
		
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(w, h);
			camera.setParameters(parameters);
			camera.startPreview();
		}
	}
}