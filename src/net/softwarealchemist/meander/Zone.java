package net.softwarealchemist.meander;

import java.util.List;

import net.softwarealchemist.meander.util.BoundingBox;

import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.SimpleVector;

public interface Zone {
	void build(ResourceManager resManager);
	void renderInto(FrameBuffer fb);
	
	Camera getCamera();
	List<BoundingBox> getSolidBoundingBoxes();
	float getHeightAtPoint(SimpleVector position);
	BoundingBox getBounds();
}