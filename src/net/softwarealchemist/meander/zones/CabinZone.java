package net.softwarealchemist.meander.zones;

import net.softwarealchemist.meander.ResourceManager;
import net.softwarealchemist.meander.Zone;
import net.softwarealchemist.meander.util.BoundingBox;

import com.threed.jpct.Camera;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class CabinZone extends Zone {

	@Override
	protected void buildWorld(ResourceManager resManager) {
		worldBounds = new BoundingBox(-48, -48, 96, 96);
		
		
		Object3D room = resManager.loadModelWithTexture("cabin_room_1");
//		room.addTriangle(
//			SimpleVector.create(-16, 0, -16), 0, 0,
//			SimpleVector.create( 16, 0, -16), 1, 0,
//			SimpleVector.create( 16, 0,  16), 1, 1);
//		room.addTriangle(
//			SimpleVector.create(-16, 0,  16), 0, 1,
//			SimpleVector.create(-16, 0, -16), 0, 0,
//			SimpleVector.create( 16, 0,  16), 1, 1);
		room.strip();
		room.build();
		
		world.addObject(room);
		
		Light light = new Light(world);
		light.setPosition(SimpleVector.create(-200, -150, -80));

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
