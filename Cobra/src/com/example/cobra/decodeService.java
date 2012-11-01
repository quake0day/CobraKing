package com.example.cobra;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.Button;

public class decodeService extends Service {
	
    @Override
	public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
 
      //  byte[] rgbBuf = new byte[3 * mPreviewWidth * mPreviewHeight];
 	//	Process process = new Process();
 	//	process.initProcess(rgbBuf,mPreviewHeight,mPreviewWidth);
    }
    
    @SuppressWarnings("deprecation")
	public void OnStart(Intent intent, int startId){
    	super.onStart(intent, startId);
    }
    
    public void OnStartCommand(Intent intent, int flags, int startId){
    	super.onStart(intent, startId);
    }
    
    public void onDestroy(){
    	super.onDestroy();
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
