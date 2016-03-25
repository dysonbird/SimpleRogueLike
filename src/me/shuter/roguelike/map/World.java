package me.shuter.roguelike.map;

import java.io.Serializable;

import me.shuter.roguelike.util.IntHashMap;

public class World implements Serializable {
	private static final long serialVersionUID = 6043041946152643023L;
	
	public String name;
	public IntHashMap<Land> lands = new IntHashMap<>();
	
	public World(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WORLD: ");
		sb.append(name);
		sb.append("\n");
		for(Land land : lands.values()) {
			sb.append("    ").append(land.toString());
		}
		return sb.toString();
	}
}
