package net.softwarealchemist.meander.zones;

import net.softwarealchemist.meander.ChangeZoneTrigger;
import net.softwarealchemist.meander.MeanderRenderer;
import net.softwarealchemist.meander.ResourceManager;
import net.softwarealchemist.meander.Zone;
import net.softwarealchemist.meander.util.BoundingBox;

import com.threed.jpct.Camera;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class CabinZone extends Zone {

	@Override
	protected void buildWorld(ResourceManager resManager, MeanderRenderer renderer) {
		worldBounds = new BoundingBox(-48, -48, 96, 96);
		
		Object3D room = resManager.loadModelWithTexture("cabin_room_1");
		room.strip();
		room.build();
		
		world.addObject(room);
		
		Light light = new Light(world);
		light.setPosition(SimpleVector.create(-200, -150, -80));

		placeCamera();
		
		triggerAreas.add(new ChangeZoneTrigger(
				new BoundingBox(40, 40, 10, 10),
				renderer,
				"hinterland"));
	}

	private void placeCamera() {
		Camera camera = world.getCamera();
		camera.setPosition(0, -20, -40);
		camera.lookAt(SimpleVector.create(worldBounds.centerX() + 1, -20, worldBounds.centerY()));
	}

	@Override
	public float getHeightAtPoint(SimpleVector position) {
		return 0;
	}

}
