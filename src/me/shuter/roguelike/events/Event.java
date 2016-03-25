package me.shuter.roguelike.events;

import me.shuter.roguelike.Base;
import me.shuter.roguelike.Game;

public final class Event extends Base {
	private static final long serialVersionUID = 6334127968716967662L;
	
    public Event(String s) {
		Game.assertTrue(!s.startsWith("On"));
		set("Name",s);
		set("HandlerName",("On"+s).intern());
	}
	
	public String handlerName() {
		return getString("HandlerName");
	}
	
	public static Event createActionEvent(int time) {
		Event e=new Event("Action");
		e.set("Time",time);
		return e;
	}
}
