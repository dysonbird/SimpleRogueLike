package me.shuter.roguelike;

import java.awt.BorderLayout;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;

import me.shuter.roguelike.entity.Hero;
import me.shuter.roguelike.events.Movement;
import me.shuter.roguelike.graphics.Render;
import me.shuter.roguelike.manager.MapManager;
import me.shuter.roguelike.map.Land;
import me.shuter.roguelike.map.Room;
import me.shuter.roguelike.map.World;
import me.shuter.roguelike.util.DirectionEnum;
import me.shuter.roguelike.util.RPG;
import me.shuter.roguelike.util.Rand;
import me.shuter.roguelike.util.TileType;

@SuppressWarnings("serial")
public class GameFrame extends JFrame implements Runnable {
	public World world;
	public Hero hero;
	
	public Movement move;
	public Render render;
	
	public int WIDTH = 335;
	public int HEIGHT = 360;
	public int FPS = 60;
	public boolean running = false;
	public String version = "1.0";
	
    public GameFrame() {
    	try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	move = new Movement(this);
        render = new Render(this);
        
        addKeyListener(move);
        add(render, BorderLayout.CENTER);
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
			hero.x = room.nd.y;
			hero.y = room.nd.x + 1;
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
					
					hero.getBlockPoint();
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
	
	/**
	 * 锚点在左下角
	 * @param x
	 * @param y
	 * @return true 碰撞  false 不碰撞
	 */
    public boolean collision(int x, int y) {     
        if ((hero.p.x + x) < 0 || (hero.p.x + x) >= (hero.room.tileMap.tiles.length - 1) * RPG.FONT_SIZE  
        		|| (hero.p.y + y - RPG.FONT_SIZE) < 0 || (hero.p.y + y) >= (hero.room.tileMap.tiles[0].length) * RPG.FONT_SIZE) {
            return true;
        }
        
        Point bp = hero.getBlockPoint();
        
        int nbx = bp.x;
        if(x < 0) {
        	nbx--;
        } else if(x > 0){
        	nbx++;
        }
        int nby = bp.y;
        if(y < 0) {
        	nby--;
        } else if(y > 0){
        	nby++;
        }
        
        if(hero.room.tileMap.tiles[nby][nbx].value == TileType.WALL.getValue()) {
        	if(bp.y == nby) {
            	if((x > 0 && (hero.getAnchorX() + RPG.FONT_SIZE/2 + x) >= nbx * RPG.FONT_SIZE)
            			|| (x < 0 && (hero.getAnchorX() - RPG.FONT_SIZE/2) <= (nbx+1) * RPG.FONT_SIZE)) {
            		return true;
            	}
            } else if(bp.x == nbx) {
            	if((y > 0 && (hero.getAnchorY() + RPG.FONT_SIZE/2 + y) >= nby * RPG.FONT_SIZE)
            			|| (y < 0 && (hero.getAnchorY() - RPG.FONT_SIZE/2 + y) <= (nby+1) * RPG.FONT_SIZE)) {
            		return true;
            	}
            }
        } else if(hero.room.tileMap.tiles[nby][nbx].value == TileType.DOOR.getValue()) {
        	if(move.UP) {
        		hero.door(DirectionEnum.NORTH.getValue());
			} else if(move.DOWN) {
				hero.door(DirectionEnum.SOUTH.getValue());
			} else if(move.LEFT) {
				hero.door(DirectionEnum.WEST.getValue());
			} else if(move.RIGHT) {
				hero.door(DirectionEnum.EAST.getValue());
			}
        	return true;
        }
        
        return false;
    }
	
	public void update(int x, int y) {
		hero.p.x += x;
		hero.p.y += y;
	}

	@Override
	public void run() {
		int sleep = (int)(1000 / FPS);
		
		while(running) {
			move.update();
			
			if(move.UP) {
				if(!collision(0, -1)) {
					update(0, -1);
				}
			} else if(move.DOWN) {
				if(!collision(0, 1)) {
					update(0, 1);
				}
			} else if(move.LEFT) {
				if(!collision(-1, 0)) {
					update(-1, 0);
				}
			} else if(move.RIGHT) {
				if(!collision(1, 0)) {
					update(1, 0);
				}
			}
			
			repaint();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("Interrupted thread!");
            }
		}
	}
	
    public static void main(String[] args) {
    	GameFrame game = new GameFrame();
        game.setSize(game.WIDTH, game.HEIGHT);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setLocationRelativeTo(null);
        game.setTitle(game.world.name + " " +game.version + " Land: " + game.hero.land.name + " Room: " + game.hero.room.name + "-" + game.hero.name);
        game.setVisible(true);

        Thread thread = new Thread(game);
        thread.start();
        game.running = true;
    }
}
