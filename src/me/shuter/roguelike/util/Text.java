package me.shuter.roguelike.util;

public class Text {
	
	private static final String whitespace = "                                                                                          ";
	// return whitesapce of specified length
	
	public static String whiteSpace(int l) {
		if (l > 0)
			return whitespace.substring(0, l);
        return "";
	}
	
	public static String rightPad(String s, int l) {
		if (s==null) s="";
		return s+whiteSpace(l-s.length());
	}
}
