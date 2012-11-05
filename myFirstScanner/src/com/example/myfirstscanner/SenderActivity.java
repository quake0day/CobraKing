package com.example.myfirstscanner;

import com.example.myfirstscanner.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class SenderActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sender, menu);
        return true;
    }
    public void startEncoding(View view){
    	Intent intent=new Intent(this,EncoderActivity.class);
    	intent.putExtra("LONG_STRING",this.findViewById(id.input_message).toString());
    	this.startActivity(intent);
    }
}
