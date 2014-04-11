package net.softwarealchemist.meander;

import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.util.SystemUiHider;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {    
    private static MainActivity master = null;

	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private float touchTurn = 0;
	private float touchTurnUp = 0;

	private float xpos = -1;
	private float ypos = -1;
	private float touchDrift = 0;

	private Object3D terrain = null;
	private int fps = 0;

	private Light sun = null;
	
	private SimpleVector facing = new SimpleVector();

	private boolean isWalking;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        create3DStuffs();
    }
    
    private void create3DStuffs() {
    	if (master != null) {
			copy(master);
		}
    	
		mGLView = new GLSurfaceView(getApplication());
		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
    
    private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean onTouchEvent(MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			if (touchDrift < 15)
				isWalking = !isWalking;
			
			xpos = -1;
			ypos = -1;
			touchTurn = 0;
			touchTurnUp = 0;
			touchDrift = 0;
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();
			
			touchDrift += Math.abs(xd) + Math.abs(yd);

			touchTurn = xd / 200f;
			touchTurnUp = yd / -200f;
			
			facing.y += touchTurn;
			facing.x += touchTurnUp;
			return true;
		}

		return super.onTouchEvent(me);
	}
    
    class MyRenderer implements GLSurfaceView.Renderer {

		private long time = System.currentTimeMillis();

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) {
			if (fb != null) {
				fb.dispose();
			}
			fb = new FrameBuffer(gl, w, h);

			if (master == null) {

				world = new World();
				world.setAmbientLight(20, 20, 20);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);

				// Create a texture out of the icon...:-)
				Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.ic_launcher)), 64, 64));
				TextureManager.getInstance().addTexture("texture", texture);

				//cube = Primitives.getCube(10);
				terrain = new Object3D(64 * 64 * 2);
				HeightMapGenerator generator = new HeightMapGenerator();
				generator.setGenerationSize(6);
				float[][] heightMap = generator.generate();
				for (int i = 0; i < heightMap.length; i++) {
					float[] row = heightMap[i];
					for (int j = 0; j < row.length; j++) {
						row[j] *= 5f;
					}
				}
				
				for (int i = 0; i < 64; i++)
					for (int j = 0; j < 64; j++) {
						float height = (float)Math.random() * 10f;
						terrain.addTriangle(
								SimpleVector.create(i  , heightMap[i  ][j  ], j  ), (i+0) / 64f, (j+0) / 64f,
								SimpleVector.create(i+1, heightMap[i+1][j  ], j  ), (i+1) / 64f, (j+0) / 64f,
								SimpleVector.create(i+1, heightMap[i+1][j+1], j+1), (i+1) / 64f, (j+1) / 64f);
						terrain.addTriangle(
								SimpleVector.create(i  , heightMap[i  ][j+1], j+1), (i+0) / 64f, (j+1) / 64f,
								SimpleVector.create(i  , heightMap[i  ][j  ], j  ), (i+0) / 64f, (j+0) / 64f,
								SimpleVector.create(i+1, heightMap[i+1][j+1], j+1), (i+1) / 64f, (j+1) / 64f);
					}
				
				terrain.setTexture("texture");
				terrain.translate(-32, 0, -32);
				terrain.strip();
				terrain.build();

				world.addObject(terrain);

				Camera camera = world.getCamera();
				camera.setPosition(0, -5, -20);
				camera.lookAt(SimpleVector.create());
				
				SimpleVector sv = new SimpleVector(-50, -100, -30);
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = MainActivity.this;
				}
			}
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {
			Camera camera = world.getCamera();
			
			camera.rotateAxis(camera.getYAxis(), touchTurn);
			camera.rotateX(touchTurnUp);

			if (isWalking)
				camera.moveCamera(Camera.CAMERA_MOVEIN, 0.1f);

			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

			if (System.currentTimeMillis() - time >= 1000) {
				Logger.log(fps + "fps");
				fps = 0;
				time = System.currentTimeMillis();
			}
			fps++;
		}
	}
}
