package me.shuter.roguelike;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.shuter.roguelike.events.Event;
import me.shuter.roguelike.util.ModLoader;
import me.shuter.roguelike.util.RPG;
import me.shuter.roguelike.util.Rand;

public class Lib implements Serializable, Cloneable {
	private static final long serialVersionUID = 2901550495293686664L;
	
	private HashMap<String, Object> things = new HashMap<>();
	private List<Thing> all = new ArrayList<>();
	private transient Map<String, Map<List<Integer>, Thing>> types = new HashMap<>();;
	
	private static Lib instance;
	
    private static Object libLock=new Object();
    
    public static Lib instance() {
        synchronized (libLock) {
        	if(instance == null) {
	            instance = new Lib();
	            Lib.init();
	        }
	    }
        return instance;
    }
    
    public static void init() {
        // set up base classes
        initBase();
        
        //core mod
        ModLoader.load(RPG.MOD);
		
        if (Game.isDebug()) {
        	System.out.println("Library initialisation complete");
        	System.out.println(instance().stats());
        }
    }
    
    private static void initBase() {
        Thing t;
        
        t = new Thing();
        t.set("Name", "base thing");
        t.set("IsThing", 1);
        Lib.add(t);
    }
    
    public static Thing get(String name) {
        return (Thing) Lib.instance().things.get(name);
    }
    
    public static Thing extend(String newName, String baseName) {
        Base base = get(baseName);
        if (base == null) throw new Error("Can't find base properties [" + baseName + "]");
        Thing newThing = new Thing(base);
        newThing.set("Name", newName);
        return newThing;
    }
    
    public static Thing createThing(Base aThing) {
		
    	Thing newThing= new Thing(aThing);
        if (newThing.handles("OnCreate")) {
            newThing.handle(new Event("Create"));
        }
        
        String s=newThing.getString("DefaultThings");
        if (s!=null) createDefaultThings(newThing,s);

    	return newThing;
    }
    
    /**
     * Create the default inventory for a newly created thing
     * Uses coded string format:
     * e.g. "iron sword" = specific iron sword
     * e.g. "[IsFood]" = some kind of food
     * e.g. "50% ham" = percentage chance of ham
     * 
     * @param t Newly created thing
     * @param s Coded string of default items
     */
    private static void createDefaultThings(Thing t, String s) {
    	String[] ts=s.split(",");
    	for (int i=0; i<ts.length; i++) {
    		ts[i]=ts[i].trim();
    		try {
				createDefaultThing(t,ts[i]);
    		} catch (Throwable x) {
				x.printStackTrace();
    		}
    	}
    }
    
    private static void createDefaultThing(Thing t, String s) {
    	int p=s.indexOf('%');
    	if (p>0) {
    		try {
    			int prob = Integer.parseInt(s.substring(0,p).trim());
    			if (Rand.r(100) >= prob) return;
    			s = s.substring(p+1,s.length()).trim();
    		} catch (Exception e) {
    			e.printStackTrace();
    			Game.warn("createDefaultThing parse error: "+s);
    			return;
    		}
    	}
    	Thing nt;
    	if (s.charAt(0) == '[') {
    		String stype = s.substring(1,s.length()-1);
    		int level = t.getLevel();
    		level = level + Rand.d(2,6) - Rand.d(2,6);
    		nt = Lib.createType(stype, level);
    	} else {
    	 	nt = Lib.create(s);		
    	}
    	t.addThingWithStacking(nt);
    }
    
    public static Thing create(String name) {
    	return create(name, Game.level());
    }
    
    public static Thing create(String name, int level) {
    	int number = 0;
    	if (Character.isDigit(name.charAt(0))) {
    		while (Character.isDigit(name.charAt(0))) { // count or percent chance
    			number = (number * 10) + Character.getNumericValue(name.charAt(0));
                name = name.substring(1);
    		}
    		if (name.charAt(0) == '*') { // random up to n
                number = Rand.r(number) + 1;
                name = name.substring(1);
    		} else if (name.charAt(0) == '%') { // percentage
                if (Rand.r(100) >= number) {
                    return null;
                }
                return create(name.substring(1).trim(),level);
            }
            name = name.trim();
        }
    	 
    	Thing t;
    	if (name.charAt(0) == '[') {
    		t = Lib.createType(name.substring(1,name.length() - 1),level);
    	} else {	
    	    Base aThing = get(name);
	        if (aThing == null) {
	        	// this is an error, so send warning
	            Game.warn("Lib: Can't create " + name);
	        	
	            return (!name.equals("strange rock")) ? Lib.create("strange rock") : null;
	        }
	        t = createThing(aThing);
	    }
        
    	if ((number > 0) && t.getFlag("IsItem")) {
    		t.set("Number",number);
    	}

        return t;
    }
    
    public static void add(Thing thing) {
        Lib lib = instance();
        if (lib == null)
            throw new Error("Game.hero.lib not available!");
        String name = (String) thing.get("Name");
        if (name == null)
            throw new Error("Trying to add unnamed object to Library!");

        prepareAdd(thing);
        
        if (lib.things.get(name)!=null) {
            Game.warn("Trying to add duplicate object ["+name+"] to library!");      
        }
        lib.things.put(name, thing);
        lib.all.add(thing);
        lib.addThingToTypeArray(thing);
    }
    
    // pre=processing before addition to library
    private static void prepareAdd(Thing t) {
        // ensure LevelMin set if not a "base" item
        // since this allows creation!
        String name=t.getString("Name");
        if (!Lib.isBaseClass(t)) {
            if (t.getStat("LevelMin")<=0) {
                t.set("LevelMin",1);
                Game.warn("Warning: no LevelMin for " + name);
            }
        }
        if (t.getStat("Level") <= t.getStat("LevelMin")) {
            t.set("Level",t.getStat("LevelMin"));
        }
    }
    
    private Base getThingFromType(String type, int level) {
        // get list of possibilities at this level
        List<Thing> things = getTypeArray(type, level);
        // search surrounding levels if nothing found
        for (int i = 1; things.isEmpty() && i < 50;) {
            things = getTypeArray(type, level + i);
            i = (i > 0) ? -i : -i + 1;
        }

        if (things.isEmpty()) {
            throw new Error("Can't create type [" + type + "] at level " + level);
        }
        Base aThing = null;
        for (int i = 0; i < 100; i++) {
            aThing = things.get(Rand.r(things.size()));
            Integer freq = (Integer)aThing.get("Frequency");
            if ((freq == null) || (Rand.r(100) < freq.intValue())) {
                break;
            }
        }
        
        if (aThing==null) {
            throw new Error("Can't find type [" + type + "] at level " + level);
        }
        return aThing;
    }
    
    public static Thing createType(String flag, int level) {
        if (level<0) level=0;
    	
        Base aThing = instance().getThingFromType(flag,level);

        Thing t = createThing(aThing);
        
        return t;
    }
    
    public List<Thing> getTypeArray(String flag, int level) {
        if (level < 1) {
            level = 1;
        }
        if (types == null) {
            types = new Hashtable<>();
        }
        Map<List<Integer>,Thing> levels = types.get(flag);
        if (levels == null) {
            levels = new HashMap<>();
            types.put(flag, levels);
        }
        
        List<Thing> itemsAtLevel = new ArrayList<>();
        
        Integer levelIndex = new Integer(level);
        Iterator<Entry<List<Integer>, Thing>> iter = levels.entrySet().iterator();
        while(iter.hasNext()) {
        	Entry<List<Integer>, Thing> entry = iter.next();
        	List<Integer> levelLimit = entry.getKey();
        	if(levelIndex >= levelLimit.get(0) && levelIndex <= levelLimit.get(1)) {
        		itemsAtLevel.add(entry.getValue());
        	}
        }
        return itemsAtLevel;
    }
    
    private void addThingToTypeArray(Thing thing) {
        if (thing.getStat("Frequency") <= 0) return;//rate
        if (((String) thing.get("Name")).indexOf("base ") >= 0) return;
        
        Integer min = (Integer) thing.get("LevelMin");
        Integer maxInteger = (Integer) thing.get("LevelMax");
        if (min == null) return;
        int max = maxInteger == null ? 50 : maxInteger.intValue();
        
        String[] ifs = thing.findAttributesStartingWith("Is");
        for (int i = 0; i < ifs.length; i++) {
            String ifAttribute = ifs[i];
            
            // skip adding if attribute is not set
            try {
            	if (!thing.getFlag(ifAttribute)) continue;
            } catch (Throwable t) {
            	System.out.println("Error with attibute [" + ifAttribute+"] value is ["+thing.get(ifAttribute)+"]");
            	throw (t);
            }
            
            Map<List<Integer>, Thing> levels = types.get(ifAttribute);
            if(levels == null) {
                levels = new HashMap<>();
                types.put(ifAttribute, levels);
            }
            List<Integer> levelLimit = new ArrayList<>();
            levelLimit.add(min);
            levelLimit.add(max);
            levels.put(levelLimit, thing);
        }
    }
    
    public String stats() {
    	Base p=new Base();
    	
    	for (int i=0; i<all.size(); i++) {
    		Thing t=new Thing(all.get(i));
    		if (!isBaseClass(t)) {
    			t.flattenProperties();
    			Iterator<String> it=t.getCollapsedMap().keySet().iterator();
    			while(it.hasNext()) {
    				String s=it.next();
    				if (s.startsWith("Is")&&t.getFlag(s)) {
    					p.set(s,p.getStat(s)+1);
    				}
    			}
    		}
    	}
    	
    	return p.report();
    }
    
    private static boolean isBaseClass(Thing t) {
        return t.getString("Name").indexOf("base ")==0;
    }
}
