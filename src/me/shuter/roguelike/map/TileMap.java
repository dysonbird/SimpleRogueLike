package me.shuter.roguelike.map;

import java.io.Serializable;

import me.shuter.roguelike.util.RPG;

public class TileMap implements Serializable {
	private static final long serialVersionUID = -3181527573396366487L;
	
	public Tile[][] tiles = new Tile[RPG.TILE_LIMIT_X][RPG.TILE_LIMIT_Y];

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < RPG.TILE_LIMIT_X; i++) {
			for(int j = 0; j < RPG.TILE_LIMIT_Y; j++) {
				sb.append("").append(RPG.tile[tiles[i][j].value]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
