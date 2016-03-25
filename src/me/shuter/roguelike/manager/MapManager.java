package me.shuter.roguelike.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.shuter.roguelike.map.Land;
import me.shuter.roguelike.map.Room;
import me.shuter.roguelike.map.Tile;
import me.shuter.roguelike.map.TileMap;
import me.shuter.roguelike.map.World;
import me.shuter.roguelike.util.RPG;
import me.shuter.roguelike.util.Rand;
import me.shuter.roguelike.util.TileType;

public class MapManager {
	private static MapManager ins = new MapManager();
	private MapManager() {}
	public static MapManager getInstance() {
		return ins;
	}
	
	/*-----------------Land----------------------*/
	
	public void addLand(World world, Land land) {
		int index = RPG.getLandIndex(land.x, land.y);;
		world.lands.put(index, land);
		
		//TODO save land
	}
	
	public Land loadLand(World world, int x, int y) {
		int index = RPG.getLandIndex(x, y);
		Land land = world.lands.get(index);
		if(null == land) {
			//TODO load land from file or other storage
			
			if(null == land) {
				land = new Land();
				land.x = x;
				land.y = y;
				land.landIndex = index;
				//random name
				land.name = "Land-" + index;
				
				//TODO discover the new land, show tip to name the land
				addLand(world,land);
			}
		}
		return land;
	}
	
	public Land enterLand(World world, int x, int y) {
		return loadLand(world, x, y);
	}
	
	/*--------------------Room------------------------*/
	
	public void addRoom(Land land, Room room) {
		int index = RPG.getRoomIndex(room.x, room.y);
		land.rooms.put(index, room);
		
		//TODO save room
	}
	
	public Room loadRoom(Land land, int x, int y) {
		int index = RPG.getRoomIndex(x, y);
		Room room = land.rooms.get(index);
		if(null == room) {
			//TODO load room from file or other storage
			
			if (null == room) {
				room = new Room();
				initTileMap(room);
				room.x = x;
				room.y = y;
				room.landIndex = land.landIndex;
				//random name
				room.name = "Area-" + index;

				// TODO discover the new room, show tip to name the room
				addRoom(land, room);
			}
		}
		
		room.initDoor();
		
		return room;
	}
	
	public Room enterRoom(Land land, int x, int y) {
		return loadRoom(land, x, y);
	}
	
	/*----------------------tile----------------------------*/
	
	/**
	 * 按照 40% 几率初始化数据；W(p) = rand(0, 100) < 40%
	 * 重复四次：W'(p) = R1(p) >= 5 || R2(p) <= 2
	 * 重复三次：W'(p) = R1(p) >= 5
	 * @return
	 */
	public TileMap initTileMap(Room room) {
		// step 1
		TileMap tileMap = new TileMap();
		for(int i = 0; i < tileMap.tiles.length; i++) {
			for(int j = 0; j < tileMap.tiles[0].length; j++) {
				tileMap.tiles[i][j] = new Tile();
				if(i == 0 || j == 0 || i == tileMap.tiles.length - 1 || j == tileMap.tiles[0].length - 1) {
					tileMap.tiles[i][j].value = TileType.WALL.getValue();
				} else {
					tileMap.tiles[i][j] .value = randpick(40);
				}
				tileMap.tiles[i][j].x = i;
				tileMap.tiles[i][j].y = j;
			}
		}
		
		// step 2
		TileType[][] temp;
		for(int count = 0; count < 4; count++) {
			temp = new TileType[tileMap.tiles.length][tileMap.tiles[0].length];
			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
					if(checkNeighborWalls(1, i, j, tileMap.tiles) >= 5 || checkNeighborWalls(2, i, j, tileMap.tiles) <= 2) {
						temp[i][j] = TileType.WALL;
					} else {
						temp[i][j] = TileType.FLOOR;
					}
				}
			}
			
			//update
			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
					tileMap.tiles[i][j].value = temp[i][j].getValue();
				}
			}
		}
		
		//step 3
		for(int count = 0; count < 3; count++) {
			temp = new TileType[tileMap.tiles.length][tileMap.tiles[0].length];
			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
					if(checkNeighborWalls(1, i, j, tileMap.tiles) >= 5) {
						temp[i][j] = TileType.WALL;
					} else {
						temp[i][j] = TileType.FLOOR;
					}
				}
			}
			
			//update
			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
					tileMap.tiles[i][j].value = temp[i][j].getValue();
				}
			}
		}
		
		//step 4
		for(int count = 0; count < 4; count++) {
			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
					if(checkNeighborWalls(3, i, j, tileMap.tiles) == 0) {
						tileMap.tiles[i][j].value = TileType.WALL.getValue();
						if(tileMap.tiles[i][j+1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i][j+1].value = randpick(95);
						}
						if(tileMap.tiles[i][j-1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i][j-1].value = randpick(95);
						}
						if(tileMap.tiles[i+1][j].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i+1][j].value = randpick(95);
						}
						if(tileMap.tiles[i-1][j].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i-1][j].value = randpick(95);
						}
						if(tileMap.tiles[i-1][j-1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i-1][j-1].value = randpick(40);
						}
						if(tileMap.tiles[i-1][j+1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i-1][j+1].value = randpick(40);
						}
						if(tileMap.tiles[i+1][j-1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i+1][j-1].value = randpick(40);
						}
						if(tileMap.tiles[i+1][j-1].value == TileType.FLOOR.getValue()) {
							tileMap.tiles[i+1][j-1].value = randpick(40);
						}
					}
				}
			}
		}
		
		//step 5 生成门 
		int N = Rand.r(tileMap.tiles[0].length / 4, tileMap.tiles[0].length / 4 * 3);
		Tile nd = tileMap.tiles[0][N];
		nd.value = TileType.DOOR.getValue();
		simpleConnectDoor(1, N, tileMap);
		
		int S = Rand.r(tileMap.tiles[0].length / 4, tileMap.tiles[0].length / 4 * 3);
		Tile sd = tileMap.tiles[tileMap.tiles.length - 1][S];
		sd.value = TileType.DOOR.getValue();
		simpleConnectDoor(tileMap.tiles.length - 2, S, tileMap);
		
		int W = Rand.r(tileMap.tiles.length / 4, tileMap.tiles.length / 4 * 3);
		Tile wd = tileMap.tiles[W][0];
		wd.value = TileType.DOOR.getValue();
		simpleConnectDoor(W, 1, tileMap);
		
		int E = Rand.r(tileMap.tiles.length / 4, tileMap.tiles.length / 4 * 3);
		Tile ed = tileMap.tiles[E][tileMap.tiles[0].length - 1];
		ed.value = TileType.DOOR.getValue();
		simpleConnectDoor(E, tileMap.tiles[0].length - 2, tileMap);
		
		//step 6 clear dead point
		clearDeadPoint(tileMap.tiles[0][N], tileMap.tiles[tileMap.tiles.length - 1][S], 
				tileMap.tiles[W][0], tileMap.tiles[E][tileMap.tiles[0].length - 1], tileMap);
		
		//step 7
//		for(int count = 0; count < 2; count++) {
//			for(int i = 1; i < tileMap.tiles.length - 1; i++) {
//				for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
//					if(checkNeighborWalls(2, i, j, tileMap.tiles) == 0) {
//						tileMap.tiles[i][j].value = TileType.WALL;
//						if(tileMap.tiles[i][j+1].value == TileType.FLOOR) {
//							tileMap.tiles[i][j+1].value = randpick(80);
//						}
//						if(tileMap.tiles[i][j-1].value == TileType.FLOOR) {
//							tileMap.tiles[i][j-1].value = randpick(80);
//						}
//						if(tileMap.tiles[i+1][j].value == TileType.FLOOR) {
//							tileMap.tiles[i+1][j].value = randpick(80);
//						}
//						if(tileMap.tiles[i-1][j].value == TileType.FLOOR) {
//							tileMap.tiles[i-1][j].value = randpick(80);
//						}
//						if(tileMap.tiles[i-1][j-1].value == TileType.FLOOR) {
//							tileMap.tiles[i-1][j-1].value = randpick(70);
//						}
//						if(tileMap.tiles[i-1][j+1].value == TileType.FLOOR) {
//							tileMap.tiles[i-1][j+1].value = randpick(70);
//						}
//						if(tileMap.tiles[i+1][j-1].value == TileType.FLOOR) {
//							tileMap.tiles[i+1][j-1].value = randpick(70);
//						}
//						if(tileMap.tiles[i+1][j-1].value == TileType.FLOOR) {
//							tileMap.tiles[i+1][j-1].value = randpick(70);
//						}
//					}
//				}
//			}
//		}
		
		room.tileMap = tileMap;
		room.nd = nd;
		room.sd = sd;
		room.wd = wd;
		room.ed = ed;
		return tileMap;
	}
	
	private void clearDeadPoint(Tile nd, Tile sd, Tile wd, Tile ed, TileMap tileMap) {
		Tile deadPoint = findDeadPoint(nd, sd, wd, ed, tileMap);;

		while(deadPoint != null) {
			List<Tile> deadPoints = new ArrayList<>();
			findFloorOrDoorNeighbor(deadPoint.x, deadPoint.y, tileMap, deadPoints);
			
			List<Tile> walls = new ArrayList<>();

			//封闭空间不包括door
			if(!deadPoints.contains(nd) 
					&& !deadPoints.contains(sd)
					&& !deadPoints.contains(wd)
					&& !deadPoints.contains(ed)) {
				findDeadWall(tileMap, walls, deadPoints);
				Collections.shuffle(walls);
				simpleClearDeadWall(walls.get(0).x, walls.get(0).y, tileMap, deadPoints);
			} else {
				//封闭空间包括door, 找出不被包含的的door
				if(!deadPoints.contains(nd)) {
					List<Tile> anthorDeadPoints = new ArrayList<>();
					findFloorOrDoorNeighbor(nd.x, nd.y, tileMap, anthorDeadPoints);
					if(deadPoints.size() > anthorDeadPoints.size()) {
						deadPoints = anthorDeadPoints;
					}
				}
				if(!deadPoints.contains(sd)) {
					List<Tile> anthorDeadPoints = new ArrayList<>();
					findFloorOrDoorNeighbor(sd.x, sd.y, tileMap, anthorDeadPoints);
					if(deadPoints.size() > anthorDeadPoints.size()) {
						deadPoints = anthorDeadPoints;
					}
				}
				if(!deadPoints.contains(wd)) {
					List<Tile> anthorDeadPoints = new ArrayList<>();
					findFloorOrDoorNeighbor(wd.x, wd.y, tileMap, anthorDeadPoints);
					if(deadPoints.size() > anthorDeadPoints.size()) {
						deadPoints = anthorDeadPoints;
					}
				}
				if(!deadPoints.contains(ed)) {
					List<Tile> anthorDeadPoints = new ArrayList<>();
					findFloorOrDoorNeighbor(ed.x, ed.y, tileMap, anthorDeadPoints);
					if(deadPoints.size() > anthorDeadPoints.size()) {
						deadPoints = anthorDeadPoints;
					}
				}
				
				findDeadWall(tileMap, walls, deadPoints);
				Collections.shuffle(walls);
				simpleClearDeadWall(walls.get(0).x, walls.get(0).y, tileMap, deadPoints);
			}
			
			deadPoint = findDeadPoint(nd, sd, wd, ed, tileMap);
		}
	}
	
	private void simpleClearDeadWall(int x, int y, TileMap tileMap, List<Tile> deadPoints) {
		List<Tile> pointList = new ArrayList<>();
		pointList.add(tileMap.tiles[x][y]);
		while(true) {
			//判断四个方向是否有floor 有跳出循环
			List<Tile> temp = new ArrayList<>();
			if(y+1 < tileMap.tiles[0].length-1) {
				if(tileMap.tiles[x][y+1].value == TileType.FLOOR.getValue() && !deadPoints.contains(tileMap.tiles[x][y+1])) break;
				else {
					temp.add(tileMap.tiles[x][y+1]);
				}
			}
			if(y-1 > 0) {
				if(tileMap.tiles[x][y-1].value == TileType.FLOOR.getValue() && !deadPoints.contains(tileMap.tiles[x][y-1])) break;
				else {
					temp.add(tileMap.tiles[x][y-1]);
				}
			}
			if(x+1 < tileMap.tiles.length-1) {
				if(tileMap.tiles[x+1][y].value == TileType.FLOOR.getValue() && !deadPoints.contains(tileMap.tiles[x+1][y])) break;
				else {
					temp.add(tileMap.tiles[x+1][y]);
				}
			}
			if(x-1 > 0) {
				if(tileMap.tiles[x-1][y].value == TileType.FLOOR.getValue() && !deadPoints.contains(tileMap.tiles[x-1][y])) break;
				else {
					temp.add(tileMap.tiles[x-1][y]);
				}
			}
			
			Collections.shuffle(temp);
			
			Tile t = temp.get(0);
			pointList.add(t);
			x = t.x;
			y = t.y;
		}
		
		for(Tile tile : pointList) {
			tile.value = TileType.FLOOR.getValue();
		}
	}
	
	private void findDeadWall(TileMap tileMap, List<Tile> walls, List<Tile> deadPoints) {
		for(Tile tile : deadPoints) {
			if(tile.x - 1 > 0 && tileMap.tiles[tile.x-1][tile.y].value == TileType.WALL.getValue() && !walls.contains(tileMap.tiles[tile.x-1][tile.y])) {
				walls.add(tileMap.tiles[tile.x-1][tile.y]);
			}
			if(tile.x+1 < tileMap.tiles.length-1 && tileMap.tiles[tile.x+1][tile.y].value == TileType.WALL.getValue() && !walls.contains(tileMap.tiles[tile.x+1][tile.y])) {
				walls.add(tileMap.tiles[tile.x+1][tile.y]);
			}
			if(tile.y-1 > 0 && tileMap.tiles[tile.x][tile.y-1].value == TileType.WALL.getValue() && !walls.contains(tileMap.tiles[tile.x][tile.y-1])) {
				walls.add(tileMap.tiles[tile.x][tile.y-1]);
			}
			if(tile.y+1 < tileMap.tiles[0].length-1 && tileMap.tiles[tile.x][tile.y+1].value == TileType.WALL.getValue() && !walls.contains(tileMap.tiles[tile.x][tile.y+1])) {
				walls.add(tileMap.tiles[tile.x][tile.y+1]);
			}
		}
	}
	
	public Tile findDeadPoint(Tile nd, Tile sd, Tile wd, Tile ed, TileMap tileMap) {
		for(int i = 1; i < tileMap.tiles.length - 1; i++) {
			for(int j = 1; j < tileMap.tiles[0].length - 1; j++) {
				if(tileMap.tiles[i][j].value == TileType.WALL.getValue()) continue;
				
				List<Tile> temp = new ArrayList<>();
				findFloorOrDoorNeighbor(i, j, tileMap, temp);
				if(!temp.contains(nd) || !temp.contains(sd) || !temp.contains(wd) || !temp.contains(ed)) {
					return tileMap.tiles[i][j];
				}
			}
		}
		
		return null;
	}
	
	private void findFloorOrDoorNeighbor(int x, int y, TileMap tileMap, List<Tile> temp) {
		if(tileMap.tiles[x][y].value == TileType.WALL.getValue() || temp.contains(tileMap.tiles[x][y])) return;

		temp.add(tileMap.tiles[x][y]);
		
		if(y+1 <= tileMap.tiles[0].length-1) {
			findFloorOrDoorNeighbor(x, y+1, tileMap, temp);
		}
		if(y-1 >= 0) {
			findFloorOrDoorNeighbor(x, y-1, tileMap, temp);
		}
		if(x+1 <= tileMap.tiles.length-1) {
			findFloorOrDoorNeighbor(x+1, y, tileMap, temp);
		}
		if(x-1 >= 0) {
			findFloorOrDoorNeighbor(x-1, y, tileMap, temp);
		}
	}
	
	private void simpleConnectDoor(int x, int y, TileMap tileMap) {
		List<Tile> pointList = new ArrayList<>();
		pointList.add(tileMap.tiles[x][y]);
		
		while(true) {
			//判断四个方向是否有floor 有跳出循环
			List<Tile> temp = new ArrayList<>();
			if(y+1 < tileMap.tiles[0].length-1) {
				if(tileMap.tiles[x][y+1].value == TileType.FLOOR.getValue()) break;
				else {
					temp.add(tileMap.tiles[x][y+1]);
				}
			}
			if(y-1 > 0) {
				if(tileMap.tiles[x][y-1].value == TileType.FLOOR.getValue()) break;
				else {
					temp.add(tileMap.tiles[x][y-1]);
				}
			}
			if(x+1 < tileMap.tiles.length-1) {
				if(tileMap.tiles[x+1][y].value == TileType.FLOOR.getValue()) break;
				else {
					temp.add(tileMap.tiles[x+1][y]);
				}
			}
			if(x-1 > 0) {
				if(tileMap.tiles[x-1][y].value == TileType.FLOOR.getValue()) break;
				else {
					temp.add(tileMap.tiles[x-1][y]);
				}
			}
			
			Collections.shuffle(temp);
			
			Tile t = temp.get(0);
			pointList.add(t);
			x = t.x;
			y = t.y;
		}
		
		for(Tile tile : pointList) {
			tile.value = TileType.FLOOR.getValue();
		}
	}
	
	private int randpick(int percent) {
		if(Rand.r(0, 100) < percent)
			return TileType.WALL.getValue();
		else
			return TileType.FLOOR.getValue();
	}
	
	private int checkNeighborWalls(int n, int i, int j, Tile[][] tiles) {
		int num = 0;
		int startX = 0, endX = 0;
		int startY = 0, endY = 0;
		startX = i-n < 0 ? 0 : i-n;
		endX = i+n >= tiles.length ? tiles.length-1 : i+n;
		startY = j-n < 0 ? 0 : j-n;
		endY = j+n >= tiles[0].length ? tiles[0].length-1 : j+n;
		
		for(int ii = startX; ii <= endX; ii++) {
			for(int jj = startY; jj <= endY; jj++) {
				num += tiles[ii][jj].value == TileType.WALL.getValue() ? 1 : 0;
			}
		}
		
		return num;
	}
}
