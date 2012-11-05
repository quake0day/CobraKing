package com.example.cobra;

import android.app.Application;

public class Glue extends Application{
	
	private String Serial;   
	private int k;
	
	private static final String VALUE = "quake";
	
	private String value;
	
	public void onCreate(){
		super.onCreate();
		setValue(VALUE);
	}
	
	public void setValue(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
	public String getSerial(){ 
	        return Serial; 
	}    
	public void setSerial(String s){ 
	        this.Serial = s; 
	} 
	
	public synchronized void add(){
		this.k = this.k + 1;
	}
	public synchronized int getK(){
		return k;
	}
	

}
