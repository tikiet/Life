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
        
        //lifeSurfaceView.setThreadState(ThreadState.RUNNING);
        
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
    
    @Override
    public void onSaveInstanceState(Bundle bundle){
    	super.onSaveInstanceState(bundle);
    	lifeSurfaceView.saveState(bundle);
    	Log.d("Life", "onSIS called");
    }
}