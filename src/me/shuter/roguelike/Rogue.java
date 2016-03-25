package me.shuter.roguelike;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import me.shuter.roguelike.entity.Hero;
import me.shuter.roguelike.manager.MapManager;
import me.shuter.roguelike.map.Land;
import me.shuter.roguelike.map.Room;
import me.shuter.roguelike.map.TileMap;
import me.shuter.roguelike.map.World;
import me.shuter.roguelike.util.RPG;
import me.shuter.roguelike.util.Rand;

public class Rogue implements Runnable {
	public World world;
	public Hero hero;
	
	public Rogue() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void init() throws Exception {
		if(load()) {
			hero.world = world;
			hero.land = MapManager.getInstance().enterLand(hero.world, hero.landX, hero.landY);
			hero.room = MapManager.getInstance().enterRoom(hero.land, hero.roomX, hero.roomY);
		} else {
			world = new World(RPG.WORLD_NAME);
			
			int landX = RPG.LAND_LIMIT_X / 2 + 1;
			int landY = RPG.LAND_LIMIT_Y / 2 + 1;
			
			Land land = MapManager.getInstance().loadLand(world, landX, landY);
			land.name = "First Land";
			
			int roomX = Rand.r(4096, 12288);
			int roomY = Rand.r(4096, 12288);
			Room room = MapManager.getInstance().loadRoom(land, roomX, roomY);
			room.name = "Hometown";
			
			hero = new Hero();
			hero.world = world;
			hero.land = land;
			hero.room = room;
			hero.name = "dodo";
			hero.landX = landX;
			hero.landY = landY;
			hero.roomX = roomX;
			hero.roomY = roomY;
			hero.x = room.nd.x + 1;
			hero.y = room.nd.y;
			
//			room.place(hero.x, hero.y, TileType.HERO);
		}
	}
	
	public void save() {
		final String fileName =  "world.data";
		final File file = new File(fileName);
		
		new Thread(new Runnable() {
            public void run(){
            	FileOutputStream fos = null;
            	ObjectOutputStream out = null; 
				
				try {
					if (file.exists()) {// 文件存在,先删除
						file.delete();
					}
					file.createNewFile();// 创建新文件
					
					fos = new FileOutputStream(file);
					out = new ObjectOutputStream(fos);
					
					out.writeObject(world);
					out.writeObject(hero);
					
					out.flush();
				} catch (Exception e) {
				} finally {
					try {
						out.close();
						fos.close();
					} catch (IOException e) {
					}
				}
            }
        }).start();
	}
	
	public boolean load() throws IOException, ClassNotFoundException {
		String fileName =  "world.data";
		File file = new File(fileName);
		
		if(!file.exists()) {
			return false;
		}
		
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(fis);
		
		world = (World)in.readObject();
		hero = (Hero)in.readObject();
		hero.world = world;
		
		in.close();
		fis.close();
		
		return true;
	}
	
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				String cmd = br.readLine().trim();
				if(cmd.equals("exit")) {
					break;
				} else if(cmd.startsWith("go")) {
					String[] array = cmd.split(" ");
					int direction = Integer.valueOf(array[1]);
					hero.door(direction);
					System.out.println(hero.room.tileMap.toString());
				} else if(cmd.equals("print")) {
					System.out.println(world.toString());
					System.out.println("Hero Info:");
					System.out.println("    " + hero.toString());
					System.out.println(hero.room.tileMap.toString());
				} else if(cmd.equals("save")) {
					save();
				} else if(cmd.equals("tile")) {
					Room room = new Room();
					TileMap map = MapManager.getInstance().initTileMap(room);
					System.out.println(map.toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Rogue game = new Rogue();
		
		Thread mainLoop = new Thread(game);
		mainLoop.start();
	}
}
