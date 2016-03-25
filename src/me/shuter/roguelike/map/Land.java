package me.shuter.roguelike.map;

import java.io.Serializable;

import me.shuter.roguelike.util.IntHashMap;


public class Land implements Serializable {
	private static final long serialVersionUID = 2871837155770617810L;
	
	public int landIndex;
	public String name;
	public int x;
	public int y;
	public IntHashMap<Room> rooms = new IntHashMap<>();
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Land: ").append(name).append(" X: ").append(x).append(" Y:").append(y).append("\n");
		for(Room room : rooms.values()) {
			sb.append(room.toString());
		}
		return sb.toString();
	}
}
