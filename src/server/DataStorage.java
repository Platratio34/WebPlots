package server;

import java.util.HashMap;
import java.util.Map;

public class DataStorage {
	
	private Map<String, Map<String, String>> data;
	
	public DataStorage() {
		data = new HashMap<String, Map<String, String>>();
		data.put("default", new HashMap<String, String>());
		data.get("default").put("test","tester");
	}
	
	public boolean hasSet(String set) {
		return data.containsKey(set);
	}
	
	public boolean hasKey(String set, String key) {
		if(hasSet(set)) {
			return data.get(set).containsKey(key);
		} else {
			System.out.println("ERROR: tried to acceses invalid data set: set=\"" + set + "\", key=\"" + key + "\", check set before atempting key");
			return false;
		}
	}
	
	public String getKey(String set, String key) {
		if(hasKey(set,key)) {
			return data.get(set).get(key);
		} else {
			return "Invalid set or key";
		}
	}
}
