package net.softwarealchemist.meander;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.meander.util.BoundingBox;
import net.softwarealchemist.meander.util.TriggerArea;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public abstract class Zone {
	protected World world;

	protected List<BoundingBox> solidBoundingBoxes = new ArrayList<BoundingBox>();
	protected List<TriggerArea> triggerAreas = new ArrayList<TriggerArea>();

	protected BoundingBox worldBounds;
	protected RGBColor back;
	
	public void build(ResourceManager resManager, MeanderRenderer renderer) {
		if (world == null) {
			 world = new World();
			buildWorld(resManager, renderer);
		}
	}
	
	protected abstract void buildWorld(ResourceManager resManager, MeanderRenderer renderer);
	
	public abstract float getHeightAtPoint(SimpleVector position);

	public void renderInto(FrameBuffer fb) {
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	public Camera getCamera() {
		return world.getCamera();
	}

	public List<BoundingBox> getSolidBoundingBoxes() {
		return solidBoundingBoxes;
	}
	
	public List<TriggerArea> getTriggerAreas() {
		return triggerAreas;
	}

	public BoundingBox getBounds() {
		return worldBounds;
	}
	
	public World getWorld() {
		return world;
	}

	public void dispose() {
		world.dispose();
	}
	
}