package me.shuter.roguelike.entity;

import me.shuter.roguelike.Game;
import me.shuter.roguelike.Lib;
import me.shuter.roguelike.Thing;
import me.shuter.roguelike.annotation.Mod;
import me.shuter.roguelike.events.Event;
import me.shuter.roguelike.events.Script;

@Mod
public class Door {
	
    public static class DoorCreation extends Script {
        private static final long serialVersionUID = 3256723974577598773L;

        public boolean handle(Thing t, Event e) {
        	Game.warn("Door creation Name:" + t.name() + " Level:" + t.getLevel());
//        	String keyName=t.getString("KeyName");
//			if (keyName==null) {
//				t.set("KeyName",chooseKey(t.getLevel()));
//			}
            return true;
        }
    }
    
	protected static String chooseKey(int level) {
		Thing key = Lib.createType("IsKey",level);
		return key.name();
	}
	
	public static Thing createDoor(int level) {
		return Lib.createType("IsDoor",level);
	}
	
	public static void init() {
		Thing t=Lib.extend("base door", "base thing");
	    t.set("UName","door");
	    t.set("IsDoor",1);
	    t.set("IsOpenable",1);
	    t.set("Frequency",50);
	    t.set("IsOpen",0);
	    t.set("LevelMin",1);
	    t.set("IsBlocking",1);
	    t.set("ASCII","+");
	    t.set("OnCreate",new DoorCreation());
	    t.set("DefaultThings","10% [IsTrap]");
	    Lib.add(t);
	    
	    t=Lib.extend("door", "base door");
	    t.set("LevelMin",1);
	    t.set("IsTownDoor",1);
	    t.set("LevelMax", 3);
	    Lib.add(t);
	    
	    t=Lib.extend("weak locked door", "base door");
	    t.set("LevelMin",4);
	    t.set("IsTownDoor",1);
	    Lib.add(t);
	    
	    t=Lib.extend("locked door", "base door");
	    t.set("LevelMin",6);
	    t.set("IsTownDoor",1);
	    Lib.add(t);
	}
}
