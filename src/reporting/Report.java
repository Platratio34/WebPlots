package reporting;

import java.util.ArrayList;
import java.util.Collections;

import dataManagment.JsonObj;

public abstract class Report {
	
	public ArrayList<ReportLight> lights;
	
	public String name;
	
	public Report(JsonObj obj, String name) {
		this.name = name;
		lights = new ArrayList<ReportLight>();
		if(obj.hasKey("lights")) {
			JsonObj[] objs = obj.getKey("lights").getArr();
			for(int i = 0; i < objs.length; i++) {
				lights.add(new ReportLight(objs[i]));
			}
			Collections.sort(lights);
		}
	}
	
	public abstract String run();
}
