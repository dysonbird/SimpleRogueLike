package me.shuter.roguelike.util;


public class RPG {
	public final static String MOD = "me.shuter.roguelike";
	public final static String WORLD_NAME = "Rogue";
	
	public final static int LAND_LIMIT_X = 32767;
	public final static int LAND_LIMIT_Y = 32767;
	
	public final static int ROOM_LIMIT_X = 16384;
	public final static int ROOM_LIMIT_Y = 16384;
	
	public final static int TILE_LIMIT_X = 32;
	public final static int TILE_LIMIT_Y = 32;
	
	public final static String[] tile = new String[]{".", "#", "D", "@"};
	
	public final static int FONT_SIZE = 10;
	
	public static int getLandIndex(int x, int y) {
		return x * LAND_LIMIT_Y + y;
	}
	
	public static int getRoomIndex(int x, int y) {
		return x * ROOM_LIMIT_Y + y;
	}
}
