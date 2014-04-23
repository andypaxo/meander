package net.softwarealchemist.meander;

import java.util.Hashtable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.util.MathUtil;
import net.softwarealchemist.meander.zones.*;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.MemoryHelper;
import com.threed.jpct.util.Overlay;

public class MeanderRenderer implements GLSurfaceView.Renderer
{
	private FrameBuffer fb;
	private Zone currentZone;
	private PlayerController controller;
	private ResourceManager resManager;
	private Hashtable<String, Class<?>> zoneTypes;
	
	private Overlay veil;
	private Texture veilTexture;
	private double fadeStartTime, fadeEndTime;
	private boolean isFading;
	private float targetAlpha, startAlpha;
	private String nextZone;

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
		
		if (veil != null)
			createVeil();
		
		if (currentZone == null) {
			zoneTypes = new Hashtable<String, Class<?>>();
			zoneTypes.put("hinterland", HinterlandZone.class);
			zoneTypes.put("cabin", CabinZone.class);
			
			veilTexture = new Texture(16, 16);
			TextureManager.getInstance().addTexture("veil", veilTexture);
			
			loadZone("cabin");
		}
		
		MemoryHelper.compact();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	}

	public void onDrawFrame(GL10 gl) {
		controller.doMovement(currentZone);
		currentZone.renderInto(fb);
		
		if (isFading) {
			float t = (float) ((System.currentTimeMillis() - fadeStartTime) / (fadeEndTime - fadeStartTime));
			final int veilAlpha = (int) (MathUtil.lerp(startAlpha, targetAlpha, t) * 16f);
			Log.d("Meander", "Veil duration set to " + (fadeEndTime - fadeStartTime) / 1000);
			Log.d("Meander", "Veil delta " + t);
			Log.d("Meander", "Veil alpha set to " + veilAlpha);
			veil.setTransparency( veilAlpha );
		}
		
		if (System.currentTimeMillis() > fadeEndTime) {
			isFading = false;
			veil.setVisibility(false);
			if (nextZone != null) {
				loadZone(nextZone);
				nextZone = null;
			}
		}
	}
	
	public void changeZone(String zoneName) {
		if (nextZone != null)
			return;
		this.nextZone = zoneName;
		startFade(1, 2);
		controller.setActive(false);
	}

	public void loadZone(String zoneName) {
		Class<?> newZoneType = zoneTypes.get(zoneName);
		try {
			final Zone newZone = (Zone) newZoneType.newInstance();
			newZone.build(resManager, this);
			currentZone = newZone;
			
			createVeil();
			startFade(0, 2); // This isn't great when loading zones after the first. Should wait for next render to start before setting timer
			controller.setActive(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void startFade(float targetAlpha, float durationSeconds) {
		isFading = true;
		fadeStartTime = System.currentTimeMillis();
		fadeEndTime = fadeStartTime + durationSeconds * 1000;
		this.targetAlpha = targetAlpha;
		this.startAlpha = 1 - targetAlpha;
		
		veil.setVisibility(true);
		veil.setTransparency((int) (startAlpha * 16f));
	}

	private void createVeil() {
		if (veil != null)
			veil.dispose();			
		veil = new Overlay(currentZone.getWorld(), fb, "veil");
		veil.setDepth(0.01f);
		veil.setVisibility(false);
	}
}
