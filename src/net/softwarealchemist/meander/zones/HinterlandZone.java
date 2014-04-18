package net.softwarealchemist.meander.zones;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.meander.HeightMapGenerator;
import net.softwarealchemist.meander.ResourceManager;
import net.softwarealchemist.meander.Zone;
import net.softwarealchemist.meander.util.BoundingBox;
import net.softwarealchemist.meander.util.MathUtil;
import android.graphics.Rect;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class HinterlandZone implements Zone {

	private World world = null;
	private Light sun = null;
	private RGBColor back = new RGBColor(50, 50, 100);
	
	private float[][] heightMap;
	private BoundingBox worldBounds;
	private final int worldScale = 10;
	private final int worldTiles = 32;
	
	private List<BoundingBox> solidBoundingBoxes = new ArrayList<BoundingBox>();
		
	@Override
	public void build(ResourceManager resManager) {
		if (world != null)
			return;
		
		world = new World();
		world.setAmbientLight(100, 100, 130);
		world.setFogging(World.FOGGING_ENABLED);
		world.setFogParameters(10 * worldScale, 50, 50, 100);
		worldBounds = new BoundingBox(1 * worldScale, 1 * worldScale, (worldTiles - 2) * worldScale, (worldTiles - 2) * worldScale);
		world.setClippingPlanes(1f, 10f * worldScale);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);
		SimpleVector sv = new SimpleVector(-50, -200, -30);
		sun.setPosition(sv);
				
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
		
		resManager.loadTexture("ground");
		terrain.setTexture("ground");
		terrain.strip();
		terrain.build();
		world.addObject(terrain);
		
		Object3D model;

		model = resManager.loadModelWithTexture("rune-rock");
		addBoundingBoxes(placeModel(model, 10, 1, true), 2, 2);

		model = resManager.loadModelWithTexture("gnarly-tree");
		addBoundingBoxes(placeModel(model, 20, 3, true), 1.5f, 1.5f);

		model = resManager.loadModelWithTexture("pine-tree");
		addBoundingBoxes(placeModel(model, 30, 4, true), 1.5f, 1.5f);

		model = resManager.loadModelWithTexture("tower");
		addBoundingBoxes(placeModel(model, 10, 1, false), 4, 4);
		
		model = resManager.loadModelWithTexture("mill");
		addBoundingBoxes(placeModel(model, 6, 1, false), 10, 6);
		
		placeCamera();
	}

	private void placeCamera() {
		Camera camera = world.getCamera();
		camera.setPosition(worldBounds.centerX(), -5, worldBounds.centerY());
		camera.lookAt(SimpleVector.create(worldBounds.centerX() + 1, -5, worldBounds.centerY()));
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

	private Object3D[] placeModel(Object3D model, int numClumps, int clumpSize, boolean rotate) {
		Object3D[] result = new Object3D[numClumps * clumpSize];
		SimpleVector instancePosition = new SimpleVector();
		final int clumpRadius = 1 * worldScale;
		Rect bounds = worldBounds.toRect();
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
	
	@Override
	public float getHeightAtPoint(SimpleVector targetPosition) {
		SimpleVector position = SimpleVector.create(targetPosition);
		position.scalarMul(1f / worldScale);
		int roundX = (int) Math.floor(position.x);
		int roundZ = (int) Math.floor(position.z);
		float heightAtPoint = MathUtil.blerp(
				position.x - roundX, position.z - roundZ,
				heightMap[roundX][roundZ],   heightMap[roundX+1][roundZ],
				heightMap[roundX][roundZ+1], heightMap[roundX+1][roundZ+1]);
		return heightAtPoint;
	}

	@Override
	public void renderInto(FrameBuffer fb) {
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public Camera getCamera() {
		return world.getCamera();
	}

	@Override
	public List<BoundingBox> getSolidBoundingBoxes() {
		return solidBoundingBoxes;
	}

	@Override
	public BoundingBox getBounds() {
		return worldBounds;
	}
}
