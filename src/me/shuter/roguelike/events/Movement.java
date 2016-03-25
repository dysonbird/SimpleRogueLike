package me.shuter.roguelike.events;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import me.shuter.roguelike.GameFrame;

public class Movement implements KeyListener {
	public boolean UP        = false;
    public boolean DOWN      = false;
    public boolean LEFT      = false;
    public boolean RIGHT     = false;
    
    public int KEY_CODE		 = 0;
    
    private GameFrame game;
    
    private boolean[] key = new boolean[500];
    
    public Movement(GameFrame game) {
        this.game = game;
        for(int i = 0; i < key.length; i++) {
            key[i] = false;
        }
    }
    
    public void update() {
        UP = key[KeyEvent.VK_UP] || key[KeyEvent.VK_W];
        DOWN = key[KeyEvent.VK_DOWN] || key[KeyEvent.VK_S];
        LEFT = key[KeyEvent.VK_LEFT] || key[KeyEvent.VK_A];
        RIGHT = key[KeyEvent.VK_RIGHT] || key[KeyEvent.VK_D];
    }

	@Override
	public void keyTyped(KeyEvent event) {
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if(event.getKeyCode() == KEY_CODE) return;
		key[KEY_CODE] = false;
		if(event.getKeyCode() < key.length) {
			if(KeyEvent.VK_S == event.getKeyCode()) {
				System.out.println("Save Game");
				game.save();
			} else {
	            key[event.getKeyCode()] = true;
	            
	            KEY_CODE = event.getKeyCode();
			}
        }
	}

	@Override
	public void keyReleased(KeyEvent event) {
		key[event.getKeyCode()] = false;
		KEY_CODE = 0;
	}

}
