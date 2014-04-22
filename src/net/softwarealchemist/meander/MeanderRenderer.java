package net.softwarealchemist.meander;

import java.util.Hashtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.zones.*;
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
	private Hashtable<String, Class<?>> zoneTypes;

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
			currentZone = new CabinZone();
			currentZone.build(resManager, this);
		}
		
		zoneTypes = new Hashtable<String, Class<?>>();
		zoneTypes.put("hinterland", HinterlandZone.class);
		zoneTypes.put("cabin", CabinZone.class);
		
		MemoryHelper.compact();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void onDrawFrame(GL10 gl) {
		controller.doMovement(currentZone);
		currentZone.renderInto(fb);
	}

	public void changeZone(String zoneName) {
		Class<?> newZoneType = zoneTypes.get(zoneName);
		try {
			final Zone newZone = (Zone) newZoneType.newInstance();
			newZone.build(resManager, this);
			currentZone = newZone;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
