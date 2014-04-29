package net.softwarealchemist.meander;

import android.annotation.TargetApi;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends Activity {    
	private GLSurfaceView mGLView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        createDependencies();
    }
    
    private void createDependencies() {
    	if (mGLView != null)
    		return;
    	
    	setupAnalytics();
		setupView();
    }

	private void setupView() {
		mGLView = new MeanderSurfaceView(getApplication());
		setContentView(mGLView);
	}

	private void setupAnalytics() {
		Tracker tracker = ((MeanderApp)getApplication()).getTracker();
    	tracker.setScreenName("Main Activity");
    	tracker.send(new HitBuilders.AppViewBuilder().build());
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
	    if (hasFocus) {
	        mGLView.setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
	                | View.SYSTEM_UI_FLAG_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	        }
	}
}
