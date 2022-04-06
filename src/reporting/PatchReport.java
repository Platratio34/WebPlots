package reporting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import dataManagment.JsonObj;
import server.PageLoader;

public class PatchReport extends Report {
	
	public PatchReport(JsonObj obj, String name) {
		super(obj, name);
	}
	
	public String run() {
		String str = "";
		try {
			String rp = PageLoader.loadFile("reports/patchReport.html");
			rp = rp.replace("%PlotName%", name);
			String tableInner = "<tr><th>Channel</th><th>Type</th><th>Address</th><th>Mode</th></tr>";
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
					tableInner += "<tr><td>" + l.ch + "</td><td>" + l.getTypeF() + "</td><td>" + l.getAdrF() + "</td><td>" + l.dmxMode + "</td></tr>";
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
