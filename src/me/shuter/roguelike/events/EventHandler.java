package me.shuter.roguelike.events;

import me.shuter.roguelike.Thing;

public interface EventHandler {
	public boolean handle(Thing t, Event e);
}
