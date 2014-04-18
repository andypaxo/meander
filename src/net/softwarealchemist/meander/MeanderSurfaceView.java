package net.softwarealchemist.meander;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class MeanderSurfaceView extends GLSurfaceView {
	private MeanderRenderer renderer = null;
	private PlayerController controller;
	
	public MeanderSurfaceView(Context context) {
		super(context);
		controller = new PlayerController();
		ResourceManager resManager = new ResourceManager(context.getAssets());
		renderer = new MeanderRenderer(controller, resManager);
		setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return controller.onTouchEvent(me) || super.onTouchEvent(me);
	}
}
