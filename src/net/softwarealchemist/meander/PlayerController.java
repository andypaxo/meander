package net.softwarealchemist.meander;

import java.util.List;

import net.softwarealchemist.meander.util.BoundingBox;
import net.softwarealchemist.meander.util.MathUtil;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

import android.view.MotionEvent;

public class PlayerController {

	private float touchTurn = 0;
	private float touchTurnUp = 0;
	private float xAngle = 0;
	private float xpos = -1;
	private float ypos = -1;
	private float touchDrift = 0;
	private boolean isWalking;

	private final float walkSpeed = 5f;
	private final float playerSize = 2.5f;
	private final float playerR = playerSize / 2f;

	long lastCall = 0, thisCall;
	
	private final SimpleVector facing = SimpleVector.create();
	private final SimpleVector camPosition = SimpleVector.create();
	private final SimpleVector camDirection = SimpleVector.create();
	private final BoundingBox camBox = new BoundingBox();
	
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
		
		return false;
	}
	
	public void doMovement(Zone zone) {
		startUpdate();
		
		Camera camera = zone.getCamera();
		List<BoundingBox> boundingBoxes = zone.getSolidBoundingBoxes();
		BoundingBox worldBounds = zone.getBounds();
		
		rotate(camera);
		
		// Translation
		camera.getPosition(camPosition);
		camera.getDirection(camDirection);
		
		if (updateDirection(camDirection)) {
			BoundingBox boxToTest;
			
			camPosition.x += camDirection.x;
			camBox.set(camPosition, playerSize);
			if (camDirection.x > 0) {
				for (int i = 0; i < boundingBoxes.size(); i++) {
					boxToTest = boundingBoxes.get(i);
					if (boxToTest.intersects(camBox)) {
						camPosition.x = boxToTest.left - playerR - 0.01f;
						camBox.set(camPosition, playerSize);
					}
				}
			} else {
				for (int i = 0; i < boundingBoxes.size(); i++) {
					boxToTest = boundingBoxes.get(i);
					if (boxToTest.intersects(camBox)) {
						camPosition.x = boxToTest.right + playerR + 0.01f;
						camBox.set(camPosition, playerSize);
					}
				}
			}
			
			camPosition.z += camDirection.z;
			camBox.set(camPosition, playerSize);
			if (camDirection.z > 0) {
				for (int i = 0; i < boundingBoxes.size(); i++) {
					boxToTest = boundingBoxes.get(i);
					if (boxToTest.intersects(camBox)) {
						camPosition.z = boxToTest.top - playerR - 0.01f;
						camBox.set(camPosition, playerSize);
					}
				}
			} else {
				for (int i = 0; i < boundingBoxes.size(); i++) {
					boxToTest = boundingBoxes.get(i);
					if (boxToTest.intersects(camBox)) {
						camPosition.z = boxToTest.bottom + playerR + 0.01f;
						camBox.set(camPosition, playerSize);
					}
				}
			}
			
			camera.setPosition(camPosition);
		}
		
		// Keep to bounds
		SimpleVector position = camera.getPosition();
		position.x = MathUtil.clamp(position.x, worldBounds.left, worldBounds.right);
		position.z = MathUtil.clamp(position.z, worldBounds.top, worldBounds.bottom);
		
		// Stick to floor
		position.y = zone.getHeightAtPoint(position) - 2f;
		
		camera.setPosition(position);
		
		endUpdate();
	}

	public void startUpdate() {
		thisCall = System.currentTimeMillis();
	}
	
	public void endUpdate() {
		lastCall = thisCall;
	}
	
	public void rotate(Camera camera) {
		camera.rotateAxis(camera.getYAxis(), touchTurn);
		if ((touchTurnUp > 0 && xAngle < Math.PI / 4.2) || (touchTurnUp < 0 && xAngle > -Math.PI / 4.2)) {
			camera.rotateX(touchTurnUp);
			xAngle += touchTurnUp;
		}
	}

	public float getPlayerSize() {
		return playerSize;
	}

	public boolean updateDirection(SimpleVector camDirection) {
		if (isWalking && lastCall > 0) {
			float dTime = (thisCall - lastCall) / 1000f;
			camDirection.y = 0;
			camDirection.normalize();
			camDirection.scalarMul(walkSpeed * dTime);
			return true;
		}
		return false;
	}
}