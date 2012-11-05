package com.example.myfirstscanner;

import com.example.myfirstscanner.R.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SenderActivity extends Activity {
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);
         // CheckBox c10= (CheckBox) this.findViewById(id.checkbox_10);
          //CheckBox c14=(CheckBox) this.findViewById(id.checkbox_14);
      /*  c10.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                {
                    c14.toggle();
                }
            }
        });
        c14.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if ( isChecked )
                {
                    c10.toggle();
                }
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sender, menu);
        return true;
    }
    public void startEncoding(View view){
    	Intent intent=new Intent(this,EncoderActivity.class);
    	intent.putExtra("LONG_STRING",this.findViewById(id.input_message).toString());
    	if(((CheckBox) this.findViewById(id.checkbox_10)).isChecked())
    		intent.putExtra("version", 200);
    	else intent.putExtra("version", 500);
    	this.startActivity(intent);
    }
}
