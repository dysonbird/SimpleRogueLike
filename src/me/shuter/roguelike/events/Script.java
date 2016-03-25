package me.shuter.roguelike.events;

import me.shuter.roguelike.Base;
import me.shuter.roguelike.Thing;

public class Script extends Base implements EventHandler {

	private static final long serialVersionUID = -5682884289934533213L;

	@Override
	public boolean handle(Thing t, Event e) {
		return false;
	}

}
