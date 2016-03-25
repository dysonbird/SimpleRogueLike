package me.shuter.roguelike.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import me.shuter.roguelike.annotation.Mod;



public class ModLoader {
	
	public static void load(String basePackage) {
		String packageName = basePackage;
		if (packageName.endsWith(".")) {
			packageName = packageName.substring(0, packageName.lastIndexOf('.'));
		}
		String path = packageName.replace('.', '/');
		
		List<Class<?>> classes = new ArrayList<>();
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(path);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					doScanPackageClassesByFile(classes, filePath, packageName);
				} 
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		for(Class<?> clazz : classes) {
			try {
				Method method = clazz.getMethod("init");
				method.invoke(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void load(String[] jar) {
		
	}
	
	private static void doScanPackageClassesByFile(List<Class<?>> classes, String filePath, String packageName) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				doScanPackageClassesByFile(classes, file.getAbsolutePath(), packageName + "." + file.getName());
			} else {
				String className = file.getName().substring(0,file.getName().length() - 6);
				try {
					Class<?> clazz = Class.forName(packageName + '.' + className);
					if(clazz.getAnnotation(Mod.class) != null) {
						classes.add(clazz);
					}
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
