package reporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import dataManagment.JsonObj;

public abstract class Report {
	
	public ArrayList<ReportLight> lights;
	
	public String name;
	
	public Report(JsonObj obj, String name) {
		this.name = name;
		lights = new ArrayList<ReportLight>();
		if(obj.hasKey("lights")) {
			HashMap<String, JsonObj> objs = obj.getKey("lights").getMap();
			for(Entry<String, JsonObj> ent : objs.entrySet()) {
				lights.add(new ReportLight(ent.getValue()));
			}
			Collections.sort(lights);
		}
	}
	
	public abstract String run();
}
