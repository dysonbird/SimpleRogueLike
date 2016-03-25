package me.shuter.roguelike.map;

import java.io.Serializable;

public class Tile implements Serializable {
	private static final long serialVersionUID = -4888500539087976829L;
	
	public int x;
	public int y;
	public int value;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("X: ").append(x).append(" Y: ").append(y).append(" TYPE: ").append(value);
		return sb.toString();
	}
}
