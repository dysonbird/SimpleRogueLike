package me.shuter.roguelike.entity;

import java.awt.Point;
import java.io.Serializable;

import me.shuter.roguelike.manager.MapManager;
import me.shuter.roguelike.map.Land;
import me.shuter.roguelike.map.Room;
import me.shuter.roguelike.map.World;
import me.shuter.roguelike.util.RPG;

public class Hero implements Serializable {
	private static final long serialVersionUID = 317057884337152558L;
	
	transient public World world;
	transient public Land land;
	transient public Room room;
	
	public String name;
	public int landX;
	public int landY;
	public int roomX;
	public int roomY;
	public int x;
	public int y;
	
	transient public Point p;
	
	public void door(int direction) {
		int tarLandX = landX, tarLandY = landY;
		int tarRoomX = roomX, tarRoomY = roomY;
		switch(direction) {
		case 1://N
			if((tarRoomY = roomY + 1) >= RPG.ROOM_LIMIT_Y) {
				if((tarLandY = landY + 1) >= RPG.LAND_LIMIT_Y) {
					System.out.println("reach world edge");
				} else {
					tarRoomY = 0;
				}
			}
			break;
		case 2://S
			if((tarRoomY = roomY - 1) < 0) {
				if((tarLandY = landY - 1) <0) {
					System.out.println("reach world edge");
				} else {
					tarRoomY = RPG.ROOM_LIMIT_Y;
				}
			}
			break;
		case 3://W
			if((tarRoomX = roomX - 1) < 0) {
				if((tarLandX = landX - 1) < 0) {
					System.out.println("reach world edge");
				} else {
					tarRoomX = RPG.ROOM_LIMIT_X;
				}
			}
			break;
		case 4://E
			if((tarRoomX = roomX + 1) >= RPG.ROOM_LIMIT_X) {
				if((tarLandX = landX + 1) >= RPG.LAND_LIMIT_X) {
					System.out.println("reach world edge");
				} else {
					tarRoomX = 0;
				}
			}
			break;
		default:
				System.out.println("move direction wrong!");
		}
		
		landX = tarLandX;
		landY = tarLandY;
		roomX = tarRoomX;
		roomY = tarRoomY;
		
		//enter land
		land = MapManager.getInstance().enterLand(world, landX, landY);
		//enter room
		room = MapManager.getInstance().enterRoom(land, roomX, roomY);
		
		switch(direction) {
		case 1:
			x = room.sd.y;
			y = room.sd.x - 1;
			break;
		case 2:
			x = room.nd.y;
			y = room.nd.x + 1;
			break;
		case 3:
			x = room.ed.y - 1;
			y = room.ed.x;
			break;
		case 4:
			x = room.wd.y + 1;
			y = room.wd.x;
			break;
		}
		p = null;
	}
	
	public Point getBlockPoint() {
		Point point = new Point();
        point.x = (int)getAnchorX() / RPG.FONT_SIZE - 1;
        point.y = (int)getAnchorY() / RPG.FONT_SIZE - 1;
        
        if(getAnchorX() % RPG.FONT_SIZE > 0) {
        	point.x++;
        }
        if(getAnchorY() % RPG.FONT_SIZE > 0) {
        	point.y++;
        }
        
        //更新坐标
        x = point.x;
        y = point.y;
        
        return point;
	}
	
	public int getAnchorX() {
		return p.x + RPG.FONT_SIZE / 2;
	}
	
	public int getAnchorY() {
		return p.y - RPG.FONT_SIZE / 2;
	}
	
	public void loadHero() {
		//enter land
		land = MapManager.getInstance().enterLand(world, landX, landY);
		//enter room
		room = MapManager.getInstance().enterRoom(land, roomX, roomY);
		
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(name).append("\n");
		sb.append("    ").append("Land: ").append(land.name).append(" X: ").append(landX).append(" Y: ").append(landY).append("\n");
		sb.append("    ").append("Room: ").append(room.name).append(" X: ").append(roomX).append(" Y: ").append(roomY);
		return sb.toString();
	}
}
