package me.shuter.roguelike.util;

import java.util.Random;

public class Rand {
	private static Random rand = new Random(System.nanoTime());
	
	public static int r(int number) {
		return rand.nextInt(number);
	}
	
	public static int d(int face) {
		return Rand.d(1, face);
	}
	
	public static int d(int num, int face) {
		int r = 0;
		for(int i = 0; i < num; i++) {
			r += Rand.r(1, face);
		}
		return r;
	}
	
	
	/**
	 * 随机范围值[]
	 * @param min
	 * @param max
	 * @return
	 */
	public static int r(int min,int max){
		int abs = Math.abs(max - min) + 1;
		return (min + rand.nextInt(abs));
	}
}
