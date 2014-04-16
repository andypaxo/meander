package net.softwarealchemist.meander.util;

import com.threed.jpct.SimpleVector;

public class BoundingBox {
	public float top, left, bottom, right;
	
	public BoundingBox() {
	}
	
	public BoundingBox(float left, float top, float width, float height) {
		this.top = top;
		this.left = left;
		this.bottom = top + height;
		this.right = left + width;
	}

	public void set(float top, float left, float width, float height) {
		this.top = top;
		this.left = left;
		this.bottom = top + height;
		this.right = left + width;
	}

	public void set(SimpleVector position, float objectSize) {
		float objectR = objectSize / 2f;
		this.top = position.z - objectR;
		this.left = position.x - objectR;
		this.bottom = position.z + objectR;
		this.right = position.x + objectR;
	}

	public boolean intersects(BoundingBox other) {
		return !(left > other.right || 
		         right < other.left || 
		         top > other.bottom ||
		         bottom < other.top);
	}

	@Override
	public String toString() {
		return "(" + top + "," + left + ") -> (" + bottom + ", " + right + ")"; 
	}
	
	
}
