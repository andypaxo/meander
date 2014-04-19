package net.softwarealchemist.meander.zones;

import com.threed.jpct.Camera;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;

import net.softwarealchemist.meander.ResourceManager;
import net.softwarealchemist.meander.Zone;
import net.softwarealchemist.meander.util.BoundingBox;

public class CabinZone extends Zone {

	@Override
	protected void buildWorld(ResourceManager resManager) {
		worldBounds = new BoundingBox(-16, -16, 32, 32);
		
		resManager.loadTexture("planks");
		
		Object3D room = new Object3D(2);
		room.addTriangle(
			SimpleVector.create(-16, 0, -16), 0, 0,
			SimpleVector.create( 16, 0, -16), 1, 0,
			SimpleVector.create( 16, 0,  16), 1, 1);
		room.addTriangle(
			SimpleVector.create(-16, 0,  16), 0, 1,
			SimpleVector.create(-16, 0, -16), 0, 0,
			SimpleVector.create( 16, 0,  16), 1, 1);
		room.setTexture("planks");
		room.strip();
		room.build();
		
		world.addObject(room);
		
		Light light = new Light(world);
		light.setPosition(SimpleVector.create(-20, -30, -8));

		placeCamera();
	}

	private void placeCamera() {
		Camera camera = world.getCamera();
		camera.setPosition(worldBounds.centerX(), -5, worldBounds.centerY());
		camera.lookAt(SimpleVector.create(worldBounds.centerX() + 1, -5, worldBounds.centerY()));
	}

	@Override
	public float getHeightAtPoint(SimpleVector position) {
		return 0;
	}

}
