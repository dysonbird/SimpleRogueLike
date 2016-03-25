package me.shuter.roguelike.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import me.shuter.roguelike.Game;
import me.shuter.roguelike.Lib;
import me.shuter.roguelike.Thing;
import me.shuter.roguelike.events.Event;

public class TestApp implements Runnable {

	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			Game.asynchronousCreateLib();
			
			try {
				String cmd = br.readLine().trim();
				if(cmd.equals("exit")) {
					break;
				} else if(cmd.startsWith("door")) {
					String[] array = cmd.split(" ");
					int level = Integer.valueOf(array[1]);
//					Thing t = Door.createDoor(level);
					Thing t = Lib.createType("IsDoor",level);
					if(t.handles("OnCreate")) {
						Event e=new Event("DoorCreate");
						e.set("SomeProperty",10);
						e.set("SomeOtherProperty","Hello");
						t.handle(e);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
