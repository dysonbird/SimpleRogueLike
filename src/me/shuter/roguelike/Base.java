package me.shuter.roguelike;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.shuter.roguelike.events.EventHandler;
import me.shuter.roguelike.util.Text;

public class Base implements Cloneable, Serializable {
	private static final long serialVersionUID = 3407715543119313613L;
	// properties
    private HashMap<String,Object> local;
    private Base inherited;
    public static boolean GET_SET_DEBUG = false;
    public static boolean GET_OUTPUT_DEBUG = false;    
    public static boolean SET_OUTPUT_DEBUG = false;
    
    public Base() {
    }
    
    public Base(Base parent) {
        this.inherited = parent;
    }
    
    public HashMap<String,Object> getLocal() {
        return local;
    }
    
    public EventHandler getHandler(String s) {
        Object o = get(s);
        return (EventHandler) o;
    }
	
    public boolean containsKey(String key) {
        if (local != null && local.containsKey(key)) return true;

        if (inherited != null) return inherited.containsKey(key);
        return false;
    }
    
    public boolean set(String key, Object value) {
        boolean didSet = realSet(key, value);
        if(GET_SET_DEBUG&&SET_OUTPUT_DEBUG) {
            System.out.println("set: " + key + " value: " + value + " didSet " + didSet);
        }
        return didSet;
    }
    
    private boolean realSet(String key, Object value) {
        if ((local==null)||(local != null && !local.containsKey(key))) {
        	if (inherited != null && inherited.containsKey(key)) {
	            Object parentValue = inherited.get(key);
	            if (parentValue == value) return false;
	            if ((parentValue != null) && parentValue.equals(value)) return false;
        	}
        }
        if (local == null) local = new HashMap<>();
        local.put(key, value);
        return true;
    }
    
    public Object get(String key) {
        Object value = realGet(key);
        if(GET_SET_DEBUG) {
        	if (GET_OUTPUT_DEBUG) {
	            StackTraceElement[] stackTrace = new RuntimeException("debug").getStackTrace();
	            String stack = "";
	            for (int i = 0; i < stackTrace.length; i++) {
	                StackTraceElement element = stackTrace[i];
	                String methodName = element.getMethodName().toLowerCase();
	                if(methodName.indexOf("get") >= 0) continue;
	                stack = element.getClassName() + "." + element.getMethodName();
	                break;
	            }
	            System.out.println("get: " + key + " value: " + value + " from " + stack);
        	}
        }
        return value;
    }
    
    private Object realGet(String key) {
        if (local != null) {
            if (local.containsKey(key)) { return local.get(key); }
        }
        if (inherited != null) return inherited.get(key);
        return null;
    }
    
    public final String getString(String s) {
        return (String) get(s);
    }
    
    public int getStat(String s) {
        return getBaseStat(s);
    }

    public int getBaseStat(String key) {
    	Integer i=(Integer)realGet(key);
    	if (i==null) return 0;
        return i.intValue();
    }
    
    public final boolean getFlag(String key) {
        Integer b = (Integer) get(key);
        return (b == null) ? false : (b.intValue() > 0);
    }
    
    public String report() {
        List<String> al = new ArrayList<>();
        String text = "";
        Base p = getFlattenedStuff(this);
        if (p.local == null) return text;
        Iterator<String> i = p.local.keySet().iterator();
        while (i.hasNext()) {
            String k = i.next();
            Object o = p.get(k);
            String s=k + " : " + (o == null ? "null" : o.toString());
            s=Text.rightPad(s,50);
            al.add( s + "depth="+getPropertyDepth(k)+"\n");
        }
        Collections.sort(al);
        i = al.iterator();
        while (i.hasNext()) {
            String s = i.next();
            text = text + s;
        }
        return text;
    }
    
    public Map<String,Object> getCollapsedMap() {
        Map<String,Object> map = null;

        if (inherited != null) map = inherited.getCollapsedMap();

        if (map == null) {
            if (local == null) {
                return new HashMap<>();
            }
            map = new HashMap<>(local);
        } else {
            if (local == null) return map;

            Iterator<String> it = local.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                map.put(key, local.get(key));
            }
        }
        return map;
    }
    
    private int getPropertyDepth(String key) {
    	if ((local!=null)&&local.containsKey(key)) return 1;
    	if (inherited==null) return 0;
    	return inherited.getPropertyDepth(key)+((local==null)?0:1);
    }
    
    public static Base getFlattenedStuff(Base source) {
    	Base destination = new Base();
        flattenInto(source, destination);
        return destination;
    }
    
    public static void flattenInto(Base source, Base dest) {
        if (source == null) return;
        flattenInto(source.inherited, dest);

        if (source.local == null) return;

        Iterator<String> it = source.local.keySet().iterator();
        while (it.hasNext()) {
            String s = it.next();
            dest.set(s, source.get(s));
        }
    }
    
    public static Base getFlattened(Base source) {
    	Base destination = new Base();
        flattenInto(source, destination);
        return destination;
    }
    
    public void flattenProperties() {
    	Base flattened = Base.getFlattened(this);
    	this.local = flattened.local;
    	this.inherited = null;
    }
    
    public String[] findAttributesStartingWith(String toFind) {
        List<String> found = new ArrayList<>();
        findAttributesStartingWith(toFind, found);
        return found.toArray(new String[found.size()]);
    }
    
    public void findAttributesStartingWith(String toFind, List<String> found) {
        if (local != null) {
            for (Iterator<String> iter = getLocal().keySet().iterator(); iter.hasNext();) {
                String attribute = iter.next();
                if (attribute.startsWith(toFind)) found.add(attribute);
            }
        }
        if(inherited != null) {
            inherited.findAttributesStartingWith(toFind, found);
        }
    }
}
