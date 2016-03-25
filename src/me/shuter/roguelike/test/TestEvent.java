package me.shuter.roguelike.test;

public class TestEvent {
	public static void main(String[] args) {
		TestApp app = new TestApp();
		Thread game = new Thread(app);
		game.start();
	}
}
