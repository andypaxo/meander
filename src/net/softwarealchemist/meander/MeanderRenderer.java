package net.softwarealchemist.meander;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.zones.HinterlandZone;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.util.MemoryHelper;

public class MeanderRenderer implements GLSurfaceView.Renderer
{
	private FrameBuffer fb;
	private Zone currentZone;
	private PlayerController controller;
	private ResourceManager resManager;

	public MeanderRenderer(PlayerController controller, ResourceManager resManager) {
		this.controller = controller;
		this.resManager = resManager;
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(gl, w, h);
		Log.d("meander", "Using GLES v" + fb.getOpenGLMajorVersion());
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		gl.glAlphaFunc(GL10.GL_GREATER, 0.01f);

		if (currentZone == null) {
			currentZone = new HinterlandZone();
			currentZone.build(resManager);
		}
		
		MemoryHelper.compact();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void onDrawFrame(GL10 gl) {
		controller.doMovement(currentZone);
		currentZone.renderInto(fb);
	}
}
