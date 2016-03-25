package me.shuter.roguelike.util;

public enum TileType {
	FLOOR(0),
	WALL(1),
	DOOR(2),
	HERO(3);
	
	private int value;
 
    private TileType(int value) {
        this.value = value;
    }
 
    public int getValue() {
        return value;
    }
}
