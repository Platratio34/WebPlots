package reporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import dataManagment.JsonObj;
import server.PageLoader;

public class PatchReport {
	
	public ArrayList<ReportLight> lights;
	
	public String name;
	
	public PatchReport(JsonObj obj, String name) {
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
	
	public String run() {
		String str = "";
		try {
			String rp = PageLoader.loadFile("reports/patchReport.html");
			rp = rp.replace("%PlotName%", name);
			String tableInner = "<tr><th>Type</th><th>Address</th><th>Mode</th></tr>";
			ArrayList<Integer> uUsed = new ArrayList<Integer>();
			String uniUsed = "";
			for(int i = 0; i < lights.size(); i++) {
				ReportLight l = lights.get(i);
				if(l.hasDmx) {
					if(!uUsed.contains(l.dmxUni)) {
						uUsed.add(l.dmxUni);
						if(uniUsed.length() > 0) {
							uniUsed += ", ";
						}
						uniUsed += "" + l.dmxUni;
					}
					tableInner += "<tr><td>" + l.getTypeF() + "</td><td>" + l.getAdrF() + "</td><td>" + l.dmxMode + "</td></tr>";
				}
			}
			rp = rp.replace("%TableInner%", tableInner);
			rp = rp.replace("%UniUsed%", uniUsed);
			str = rp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			str = "Error Loading defualt";
		}
		return str;
	}
}
