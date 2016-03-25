package me.shuter.roguelike;

public final class Game extends Base {
	private static final long serialVersionUID = 2689085305534605155L;
	private static Game instance = new Game();
	
	private boolean debug = true;
	
	/**
	 * 
	 * @return
	 */
	public static Game instance() {
		return instance;
	}
	
	/**
	 * Returns the current level (hero's level)
	 * @return Difficulty level
	 */
	public static int level() {
		//Map m=hero.getMap();
		//if (m!=null) return m.getLevel();
//		return hero() == null ? 1 : hero().getLevel();
		return 1;
	}
	
	public static void warn(String s) {
		if (Game.isDebug()) System.out.println(s);
	}
	
	/**
	 * @return Returns the debug.
	 */
	public static boolean isDebug() {
		return Game.instance().debug;
	}

	public static void assertTrue(boolean condition) {
		if (!condition) {
			try {
				throw new AssertionError();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
    public static void asynchronousCreateLib() {
		new Thread(new Runnable() {
			public void run() {
				Lib.instance();
			}
		}).start();
	}
}
