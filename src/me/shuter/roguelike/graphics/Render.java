package me.shuter.roguelike.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JPanel;

import me.shuter.roguelike.GameFrame;
import me.shuter.roguelike.util.RPG;
import me.shuter.roguelike.util.TileType;

@SuppressWarnings("serial")
public class Render extends JPanel {
	private GameFrame game;
	
    public Render(GameFrame game) {
        this.game = game;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
	    
        render(g);
    }
    
    public void render(Graphics g) {
        drawBackground(g);
        draw2DTilemap(g);
        draw2DHero(g);
    }
    
    private void drawBackground(Graphics g) {
    	setBackground(Color.black);
    }
    
    private void draw2DTilemap(Graphics g) {
    	Font font = new Font("Arial", Font.BOLD, 10);
    	g.setFont(font);
    	g.setColor(Color.WHITE);
    	
        for(int y = 0; y < game.hero.room.tileMap.tiles[0].length; y++) {
        	for(int x = 0; x < game.hero.room.tileMap.tiles.length; x++) {
        		Point p = twoFormula(x, y);
        		g.drawString(RPG.tile[game.hero.room.tileMap.tiles[y][x].value], p.x, p.y + RPG.FONT_SIZE);
        	}
        }
    }
    
    private void draw2DHero(Graphics g) { 
    	Font font = new Font("Arial", Font.BOLD, 10);
    	g.setFont(font);
    	g.setColor(Color.ORANGE);
    	if(game.hero.p == null) {
    		Point p = twoFormula(game.hero.x, game.hero.y);
    		g.drawString(RPG.tile[TileType.HERO.getValue()], p.x, p.y + RPG.FONT_SIZE);
    		p.y += RPG.FONT_SIZE;
    		game.hero.p = p;
    		
    	} else {
    		g.drawString(RPG.tile[TileType.HERO.getValue()], game.hero.p.x, game.hero.p.y);
    	}
    }
    
    public Point twoFormula(int x, int y) {
        int xx = x * RPG.FONT_SIZE;
        int yy = y * RPG.FONT_SIZE;
        return new Point(xx, yy);
    }
}
