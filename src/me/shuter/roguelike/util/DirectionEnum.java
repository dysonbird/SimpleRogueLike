package me.shuter.roguelike.util;

public enum DirectionEnum {
	NONE(0),
	NORTH(1),
	SOUTH(2),
	WEST(3),
	EAST(4);
	
	private int value;
 
    private DirectionEnum(int value) {
        this.value = value;
    }
 
    public int getValue() {
        return value;
    }
}
