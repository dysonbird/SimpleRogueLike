package me.shuter.roguelike;

import me.shuter.roguelike.events.Event;
import me.shuter.roguelike.events.EventHandler;

public final class Thing extends Base {
	private static final long serialVersionUID = 6399840177124910774L;
	
	public Thing() {
		super();
	}
	
	public Thing(Base base) {
		super(base);
	}
	
	public Thing(Thing t) {
		super(t);
	}
	
	public int getLevel() {
		return getStat("Level");
	}
	
	public String name() {
		return getString("Name");
	}
	
	public boolean handle(Event e) {
		EventHandler eh=getHandler(e.handlerName());
		if (eh!=null) {
			if (eh.handle(this,e)) return true;
		}
		return false;
	}
	
	public boolean handles(String handlerName) {
		Game.assertTrue(handlerName.startsWith("On"));
        return containsKey(handlerName);
	}
	
	public Thing addThingWithStacking(Thing thing) {
		return thing;
	}
}
