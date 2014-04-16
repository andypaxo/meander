package net.softwarealchemist.meander.util;

public class BoundingBox {
	float top, left, bottom, right;
	
	public BoundingBox(float top, float left, float width, float height) {
		this.top = top;
		this.left = left;
		this.bottom = top + height;
		this.right = left + width;
	}
}
