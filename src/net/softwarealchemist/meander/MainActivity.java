package net.softwarealchemist.meander;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.softwarealchemist.meander.util.BoundingBox;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

public class MainActivity extends Activity {    
	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private float touchTurn = 0;
	private float touchTurnUp = 0;
	private float xAngle = 0;
	private float xpos = -1;
	private float ypos = -1;
	private float touchDrift = 0;

	private Light sun = null;
	
	private SimpleVector facing = new SimpleVector();

	private boolean isWalking;
	
	private Rect worldBounds;
	private final int worldScale = 10;
	private final int worldTiles = 32;
	
	private List<BoundingBox> solidBoundingBoxes = new ArrayList<BoundingBox>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        create3DStuffs();
    }
    
    private void create3DStuffs() {
		mGLView = new MySurfaceView(getApplication());
		renderer = new MyRenderer();
		mGLView.setRenderer(renderer);
		setContentView(mGLView);
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
	
	class MySurfaceView extends GLSurfaceView {

		public MySurfaceView(Context context) {
			super(context);
			setEGLContextClientVersion(2);
		}
		
	}
    
    class MyRenderer implements GLSurfaceView.Renderer {
		private float[][] heightMap;

		public MyRenderer() {
		}

		public void onSurfaceChanged(GL10 unused, int w, int h) {
			if (fb != null)
				fb.dispose();
			fb = new FrameBuffer(w, h);
			Log.d("meander", "Using GLES v" + fb.getOpenGLMajorVersion());
			
			GLES20.glEnable(GL10.GL_BLEND);
			GLES20.glEnable(GL10.GL_ALPHA_TEST);
			GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			// GLES20.glAlphaFunc(GL10.GL_GREATER, 0.01f);

			if (world != null)
				return;
			
			world = new World();
			world.setAmbientLight(100, 100, 130);
			world.setFogging(World.FOGGING_ENABLED);
			world.setFogParameters(10 * worldScale, 50, 50, 100);
			worldBounds = new Rect(1 * worldScale, 1 * worldScale, (worldTiles - 2) * worldScale, (worldTiles - 2) * worldScale);
			world.setClippingPlanes(1f, 10f * worldScale);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);
			SimpleVector sv = new SimpleVector(-50, -200, -30);
			sun.setPosition(sv);
			
			AssetManager assManager = getApplicationContext().getAssets();
			
			HeightMapGenerator generator = new HeightMapGenerator();
			final double maxOffset = 2048;
			final int 
				xOffset = (int) (Math.random() * maxOffset * 2 - maxOffset),
				yOffset = (int) (Math.random() * maxOffset * 2 - maxOffset);
			heightMap = generator.generate(xOffset, yOffset, worldTiles, worldTiles, 0.03f);
			for (int i = 0; i < heightMap.length; i++) {
				float[] row = heightMap[i];
				for (int j = 0; j < row.length; j++) {
					row[j] *= -30f;
				}
			}

			Object3D terrain = new Object3D(worldTiles * worldTiles * 2);				
			int x, z, s = worldScale;
			float texScale = 1f / (worldTiles - 1);
			for (int i = 0; i < worldTiles - 1; i++)
				for (int j = 0; j < worldTiles - 1; j++) {
					x = i * s;
					z = j * s;
					terrain.addTriangle(
							SimpleVector.create(x  , heightMap[i  ][j  ], z  ), (i+0) * texScale, (j+0) * texScale,
							SimpleVector.create(x+s, heightMap[i+1][j  ], z  ), (i+1) * texScale, (j+0) * texScale,
							SimpleVector.create(x+s, heightMap[i+1][j+1], z+s), (i+1) * texScale, (j+1) * texScale);
					terrain.addTriangle(
							SimpleVector.create(x  , heightMap[i  ][j+1], z+s), (i+0) * texScale, (j+1) * texScale,
							SimpleVector.create(x  , heightMap[i  ][j  ], z  ), (i+0) * texScale, (j+0) * texScale,
							SimpleVector.create(x+s, heightMap[i+1][j+1], z+s), (i+1) * texScale, (j+1) * texScale);
				}
			
			loadTexture(assManager, "ground");
			terrain.setTexture("ground");
			terrain.strip();
			terrain.build();
			world.addObject(terrain);
			
			Object3D model;

			model = loadModelWithTexture(assManager, "rune-rock");
			addBoundingBoxes(placeModel(model, 10, 1, true), 2, 2);

			model = loadModelWithTexture(assManager, "gnarly-tree");
			addBoundingBoxes(placeModel(model, 20, 3, true), 1.5f, 1.5f);

			model = loadModelWithTexture(assManager, "pine-tree");
			addBoundingBoxes(placeModel(model, 30, 4, true), 1.5f, 1.5f);

			model = loadModelWithTexture(assManager, "tower");
			addBoundingBoxes(placeModel(model, 10, 1, false), 4, 4);
			
			model = loadModelWithTexture(assManager, "mill");
			addBoundingBoxes(placeModel(model, 6, 1, false), 10, 6);
			
			Camera camera = world.getCamera();
			camera.setPosition(worldBounds.centerX(), -5, worldBounds.centerY());
			camera.lookAt(SimpleVector.create(worldBounds.centerX() + 1, -5, worldBounds.centerY()));
			
			MemoryHelper.compact();
		}

		private void addBoundingBoxes(Object3D[] models, float width, float depth) {
			float rX = width / 2f;
			float rZ = depth / 2f;
			SimpleVector translation;
			for (int i = 0; i < models.length; i++) {
				translation = models[i].getTranslation();
				solidBoundingBoxes.add(new BoundingBox(
						translation.x - rX,
						translation.z - rZ,
						width,
						depth));
			}
		}

		private Object3D loadModelWithTexture(AssetManager assManager, String modelName) {
			Object3D model;
			loadTexture(assManager, modelName);
			model = loadModel(assManager, modelName);
			model.setTexture(modelName);
			return model;
		}

		private Object3D loadModel(AssetManager assManager, String modelName) {
			Object3D model = null;
			try {	
				InputStream objStream, mtlStream;
				objStream= assManager.open("models/"+modelName+".obj");
				mtlStream = assManager.open("models/"+modelName+".mtl");
				model = Loader.loadOBJ(objStream, mtlStream, 1)[0];
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			return model;
		}

		Drawable textureImage;
		Texture texture;
		private void loadTexture(AssetManager assManager, String textureName) {
			try {
				texture = new Texture(assManager.open("textures/"+textureName+".png"), true);
				texture.setFiltering(false);
				TextureManager.getInstance().addTexture(textureName, texture);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		private Object3D[] placeModel(Object3D model, int numClumps, int clumpSize, boolean rotate) {
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
					instancePosition.y = getHeightAtPoint(instancePosition);
					
					Object3D instance = model.cloneObject();
					if (rotate)
						instance.rotateY((float) (Math.random() * 2.0 * Math.PI));
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
			doMovement();
			
			fb.clear(back);
			world.renderScene(fb);
			world.draw(fb);
			fb.display();

		}

		long lastCall = 0, thisCall;
		SimpleVector camPosition = SimpleVector.create();
		SimpleVector camDirection = SimpleVector.create();
		final float walkSpeed = 5f;
		final float playerSize = 2.5f;
		final float playerR = playerSize / 2f;
		BoundingBox camBox = new BoundingBox();
		private void doMovement() {
			Camera camera = world.getCamera();
			
			// Rotation
			camera.rotateAxis(camera.getYAxis(), touchTurn);
			if ((touchTurnUp > 0 && xAngle < Math.PI / 4.2) || (touchTurnUp < 0 && xAngle > -Math.PI / 4.2)) {
				camera.rotateX(touchTurnUp);
				xAngle += touchTurnUp;
			}

			// Translation
			camera.getPosition(camPosition);
			thisCall = System.currentTimeMillis();
			if (isWalking && lastCall > 0) {
				float dTime = (thisCall - lastCall) / 1000f; 
				camera.getDirection(camDirection);
				camDirection.y = 0;
				camDirection.normalize();
				camDirection.scalarMul(walkSpeed * dTime);
				
				BoundingBox boxToTest;
				
				camPosition.x += camDirection.x;
				camBox.set(camPosition, playerSize);
				if (camDirection.x > 0) {
					for (int i = 0; i < solidBoundingBoxes.size(); i++) {
						boxToTest = solidBoundingBoxes.get(i);
						if (boxToTest.intersects(camBox)) {
							camPosition.x = boxToTest.left - playerR - 0.01f;
							camBox.set(camPosition, playerSize);
						}
					}
				} else {
					for (int i = 0; i < solidBoundingBoxes.size(); i++) {
						boxToTest = solidBoundingBoxes.get(i);
						if (boxToTest.intersects(camBox)) {
							camPosition.x = boxToTest.right + playerR + 0.01f;
							camBox.set(camPosition, playerSize);
						}
					}
				}
				
				camPosition.z += camDirection.z;
				camBox.set(camPosition, playerSize);
				if (camDirection.z > 0) {
					for (int i = 0; i < solidBoundingBoxes.size(); i++) {
						boxToTest = solidBoundingBoxes.get(i);
						if (boxToTest.intersects(camBox)) {
							camPosition.z = boxToTest.top - playerR - 0.01f;
							camBox.set(camPosition, playerSize);
						}
					}
				} else {
					for (int i = 0; i < solidBoundingBoxes.size(); i++) {
						boxToTest = solidBoundingBoxes.get(i);
						if (boxToTest.intersects(camBox)) {
							camPosition.z = boxToTest.bottom + playerR + 0.01f;
							camBox.set(camPosition, playerSize);
						}
					}
				}
				
				camera.setPosition(camPosition);
			}
			lastCall = thisCall;
			
			// Keep to bounds
			SimpleVector position = camera.getPosition();
			position.x = clamp(position.x, worldBounds.left, worldBounds.right);
			position.z = clamp(position.z, worldBounds.top, worldBounds.bottom);
			
			// Stick to floor
			position.y = getHeightAtPoint(position) - 2f;
			
			camera.setPosition(position);
		}

//		private int fps = 0;
//		private void logFps() {
//			if (System.currentTimeMillis() - time >= 1000) {
//				Logger.log(fps + "fps");
//				fps = 0;
//				time = System.currentTimeMillis();
//			}
//			fps++;
//		}
		
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
