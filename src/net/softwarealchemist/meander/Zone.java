package net.softwarealchemist.meander;

import java.util.ArrayList;
import java.util.List;

import net.softwarealchemist.meander.util.BoundingBox;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public abstract class Zone {
	protected World world;
	protected List<BoundingBox> solidBoundingBoxes = new ArrayList<BoundingBox>();
	protected BoundingBox worldBounds;
	protected RGBColor back;
	
	public void build(ResourceManager resManager) {
		if (world == null) {
			 world = new World();
			buildWorld(resManager);
		}
	}
	
	protected abstract void buildWorld(ResourceManager resManager);
	
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

	public BoundingBox getBounds() {
		return worldBounds;
	}
	
}