package me.shuter.roguelike.map;

import java.io.Serializable;

import me.shuter.roguelike.util.TileType;

public class Room implements Serializable {
	private static final long serialVersionUID = -7961589151176289049L;
	
	public int landIndex;
	public String name;
	public int x;
	public int y;
	public TileMap tileMap;
	
	transient public Tile nd;
	transient public Tile sd;
	transient public Tile wd;
	transient public Tile ed;
	
	public void initDoor() {
		if(nd == null) {
			for(int i = 0; i < tileMap.tiles[0].length; i++) {
				if(tileMap.tiles[0][i].value == TileType.DOOR.getValue()) {
					nd = tileMap.tiles[0][i];
					break;
				}
			}
		}
		
		if(sd == null) {
			for(int i = 0; i < tileMap.tiles[0].length; i++) {
				if(tileMap.tiles[tileMap.tiles.length-1][i].value == TileType.DOOR.getValue()) {
					nd = tileMap.tiles[tileMap.tiles.length-1][i];
					break;
				}
			}
		}
		
		if(wd == null) {
			for(int i = 0; i < tileMap.tiles.length; i++) {
				if(tileMap.tiles[i][0].value == TileType.DOOR.getValue()) {
					nd = tileMap.tiles[i][0];
					break;
				}
			}
		}
		
		if(ed == null) {
			for(int i = 0; i < tileMap.tiles.length; i++) {
				if(tileMap.tiles[i][tileMap.tiles[0].length-1].value == TileType.DOOR.getValue()) {
					nd = tileMap.tiles[i][tileMap.tiles[0].length-1];
					break;
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("        ");
		sb.append("Room: ").append(name).append(" X: ").append(x).append(" Y: ").append(y).append("\n");
		return sb.toString();
	}
}
