package net.softwarealchemist.meander;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.util.SystemUiHider;
import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
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
	private float xAngle=0;
	private float xpos = -1;
	private float ypos = -1;
	private float touchDrift = 0;

	private int fps = 0;

	private Light sun = null;
	
	private SimpleVector facing = new SimpleVector();

	private boolean isWalking;
	
	private Rect worldBounds;
	private final int worldScale = 10;
	
	private Object3D[] mushrooms;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		private float[][] heightMap;

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
				world.setFogging(World.FOGGING_ENABLED);
				world.setFogParameters(20 * worldScale, 50, 50, 100);
				worldBounds = new Rect(1 * worldScale, 1 * worldScale, 63 * worldScale, 63 * worldScale);

				sun = new Light(world);
				sun.setIntensity(250, 250, 250);
				
				AssetManager assManager = getApplicationContext().getAssets();
				try {
					Drawable textureImage = Drawable.createFromStream(assManager.open("textures/leaves.jpg"), null);
					Texture texture = new Texture(BitmapHelper.convert(textureImage));
					TextureManager.getInstance().addTexture("texture", texture);

					textureImage = Drawable.createFromStream(assManager.open("textures/mushroom.jpg"), null);
					texture = new Texture(BitmapHelper.convert(textureImage));
					TextureManager.getInstance().addTexture("mushroom", texture);
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				HeightMapGenerator generator = new HeightMapGenerator();
				heightMap = generator.generate(0, 0, 64, 64, 0.03f);
				for (int i = 0; i < heightMap.length; i++) {
					float[] row = heightMap[i];
					for (int j = 0; j < row.length; j++) {
						row[j] *= -30f;
					}
				}

				Object3D terrain = new Object3D(64 * 64 * 2);				
				int x, z, s = worldScale;
				for (int i = 0; i < 62; i++)
					for (int j = 0; j < 62; j++) {
						x = i * s;
						z = j * s;
						terrain.addTriangle(
								SimpleVector.create(x  , heightMap[i  ][j  ], z  ), (i+0) / 8f, (j+0) / 8f,
								SimpleVector.create(x+s, heightMap[i+1][j  ], z  ), (i+1) / 8f, (j+0) / 8f,
								SimpleVector.create(x+s, heightMap[i+1][j+1], z+s), (i+1) / 8f, (j+1) / 8f);
						terrain.addTriangle(
								SimpleVector.create(x  , heightMap[i  ][j+1], z+s), (i+0) / 8f, (j+1) / 8f,
								SimpleVector.create(x  , heightMap[i  ][j  ], z  ), (i+0) / 8f, (j+0) / 8f,
								SimpleVector.create(x+s, heightMap[i+1][j+1], z+s), (i+1) / 8f, (j+1) / 8f);
					}
				terrain.setTexture("texture");
				terrain.strip();
				terrain.build();
				world.addObject(terrain);
				
				try {
					InputStream objStream, mtlStream;
					Object3D model;

					objStream= assManager.open("models/LollypopTree.obj");
					mtlStream = assManager.open("models/LollypopTree.mtl");
					model = Loader.loadOBJ(objStream, mtlStream, 1)[0];
					model.setTexture("mushroom");
					model.rotateX((float) Math.PI);					
					mushrooms = placeModel(model, -1.6f, 10, 5);
					
					objStream= assManager.open("models/Well.obj");
					mtlStream = assManager.open("models/Well.mtl");
					model = Loader.loadOBJ(objStream, mtlStream, 1)[0];
					model.setTexture("mushroom");
					model.scale(5f);
					model.rotateX((float) Math.PI);
					placeModel(model, -1f, 10, 1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				for (String textureName : TextureManager.getInstance().getNames()) {
					Log.d("meander", "Texture : " + textureName);
				}
				
				
				Camera camera = world.getCamera();
				camera.setPosition(20, -5, 20);
				camera.lookAt(SimpleVector.create(worldBounds.exactCenterX(), 0, worldBounds.exactCenterY()));
								
				SimpleVector sv = new SimpleVector(-50, -100, -30);
				sun.setPosition(sv);
				MemoryHelper.compact();

				if (master == null) {
					Logger.log("Saving master Activity!");
					master = MainActivity.this;
				}
			}
		}

		private Object3D[] placeModel(Object3D model, float offset, int numClumps, int clumpSize) {
			Object3D[] result = new Object3D[numClumps * clumpSize];
			SimpleVector instancePosition = new SimpleVector();
			final int clumpRadius = 1 * worldScale;
			Rect bounds = new Rect(worldBounds);
			bounds.inset(clumpRadius, clumpRadius);
			
			for (int i = 0; i < numClumps; i++) {
				SimpleVector position = SimpleVector.create(
						(float)Math.random() * bounds.width() + bounds.left,
						0f,
						(float)Math.random() * bounds.height() + bounds.top);
				for (int c = 0; c < clumpSize; c++) {
					instancePosition.set(position);
					instancePosition.x += ((float)Math.random() - 0.5) * clumpRadius * 2;
					instancePosition.z += ((float)Math.random() - 0.5) * clumpRadius * 2;
					instancePosition.y = getHeightAtPoint(instancePosition) + offset;
					
					Object3D instance = model.cloneObject();
					instance.translate(instancePosition);
					instance.strip();
					instance.build();
					world.addObject(instance);
					result[i * clumpSize + c] = instance;
				}
			}
			return result;
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		}

		public void onDrawFrame(GL10 gl) {
			Camera camera = world.getCamera();
			
			camera.rotateAxis(camera.getYAxis(), touchTurn);
			if ((touchTurnUp > 0 && xAngle < Math.PI / 4.2) || (touchTurnUp < 0 && xAngle > -Math.PI / 4.2)) {
				camera.rotateX(touchTurnUp);
				xAngle += touchTurnUp;
			}

			if (isWalking)
				camera.moveCamera(Camera.CAMERA_MOVEIN, 0.1f);
			
			SimpleVector position = camera.getPosition();
			position.x = clamp(position.x, worldBounds.left, worldBounds.right);
			position.z = clamp(position.z, worldBounds.top, worldBounds.bottom);
			position.y = getHeightAtPoint(position) - 2f;
			camera.setPosition(position);
			
			Object3D mushroom;
			SimpleVector toCam;
			SimpleVector mushroomTranslation;
			for (int i = 0; i < mushrooms.length; i++) {
				mushroom = mushrooms[i];
				mushroomTranslation = mushroom.getTranslation();
				toCam = SimpleVector.create(mushroomTranslation);
				toCam.sub(camera.getPosition());
				if (toCam.length() < worldScale) {
					mushroom.translate(0, 0.2f, 0);
				}
			}

			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

//			if (System.currentTimeMillis() - time >= 1000) {
//				Logger.log(fps + "fps");
//				fps = 0;
//				time = System.currentTimeMillis();
//			}
//			fps++;
		}

		private float getHeightAtPoint(SimpleVector targetPosition) {
			SimpleVector position = SimpleVector.create(targetPosition);
			position.scalarMul(1f / worldScale);
			int roundX = (int) Math.floor(position.x);
			int roundZ = (int) Math.floor(position.z);
			float heightAtPoint = blerp(
					position.x - roundX, position.z - roundZ,
					heightMap[roundX][roundZ],   heightMap[roundX+1][roundZ],
					heightMap[roundX][roundZ+1], heightMap[roundX+1][roundZ+1]);
			return heightAtPoint;
		}
		
		private float clamp(float value, float min, float max) {
			return Math.max(min, Math.min(max, value));
		}
		
//		private float lerp(float a, float b, float t) {
//			return a + (b - a) * t;
//		}
		
		float blerp(
				   float tx, float ty, 
				   float c00, float c10,
				   float c01, float c11)
				{
				    float a = c00 * (1f - tx) + c10 * tx;
				    float b = c01 * (1f - tx) + c11 * tx;
				    return a * (1f - ty) + b * ty;
				}
	}
}
