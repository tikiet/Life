package edu.crabium.android.life;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

public class LifeActivity extends Activity {
	LifeSurfaceView lifeSurfaceView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        
        
        setContentView(R.layout.main);
        lifeSurfaceView = (LifeSurfaceView)findViewById(R.id.surfaceView1);
        lifeSurfaceView.setHeight(displaymetrics.heightPixels);
        lifeSurfaceView.setWidth(displaymetrics.widthPixels);
        
        int density =  getResources().getDisplayMetrics().densityDpi;
        Log.d("Life","density:"+density);
        if(displaymetrics.heightPixels > 800){
        	lifeSurfaceView.setBrickHeight(64);
        	lifeSurfaceView.setBrickWidth(64);
        }else if(displaymetrics.heightPixels > 600){
        	lifeSurfaceView.setBrickHeight(48);
        	lifeSurfaceView.setBrickWidth(48);
        }else if(displaymetrics.heightPixels > 400){
        	lifeSurfaceView.setBrickHeight(32);
        	lifeSurfaceView.setBrickWidth(32);
        }else{
        	lifeSurfaceView.setBrickHeight(24);
        	lifeSurfaceView.setBrickWidth(24);
        }
        	
        if(savedInstanceState != null){
        	Log.d("Life","SavedInstanceState not null");
        	//lifeSurfaceView.setThreadState(ThreadState.RECOVER);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration config){
    	super.onConfigurationChanged(config);
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	lifeSurfaceView.setThreadState(ThreadState.STOPPED);
    }
}