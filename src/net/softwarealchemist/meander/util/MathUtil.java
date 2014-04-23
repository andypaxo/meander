package net.softwarealchemist.meander.util;

public class MathUtil {
	
	public static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
	
	public static float blerp(
		float tx, float ty, 
		float c00, float c10,
		float c01, float c11)
	{
	    float a = c00 * (1f - tx) + c10 * tx;
	    float b = c01 * (1f - tx) + c11 * tx;
	    return a * (1f - ty) + b * ty;
	}
	
	public static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}

}
