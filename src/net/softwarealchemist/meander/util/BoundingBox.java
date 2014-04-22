package net.softwarealchemist.meander.util;

import android.graphics.Rect;

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
	
	public boolean contains(float x, float y) {
		return x > left && x < right && y > top && y < bottom;
	}

	@Override
	public String toString() {
		return "(" + top + "," + left + ") -> (" + bottom + ", " + right + ")"; 
	}

	public float centerX() {
		return (left + right) / 2f;
	}

	public float centerY() {
		return (top + bottom) / 2f;
	}

	public Rect toRect() {
		return new Rect((int)left, (int)top, (int)right, (int)bottom);
	}
}
