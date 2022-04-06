package reporting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dataManagment.JsonObj;
import server.PageLoader;

public class CombinedReport extends Report {
	
	private ArrayList<Type> types;

	public CombinedReport(JsonObj obj, String name) {
		super(obj, name);
		HashMap<String, Integer> typeMap = new HashMap<String, Integer>();
		for(int i = 0; i < lights.size(); i++) {
			ReportLight l = lights.get(i);
			String t = l.getTypeF();
			if(typeMap.containsKey(t)) {
				typeMap.put(t, typeMap.get(t)+1);
			} else {
				typeMap.put(t, 1);
			}
		}
		types = new ArrayList<Type>();
		for(Map.Entry<String, Integer> kv : typeMap.entrySet()) {
			types.add(new Type(kv.getKey(), kv.getValue()));
		}
		Collections.sort(types, Collections.reverseOrder());
	}

	@Override
	public String run() {
		String str = "";
		try {
			String rp = PageLoader.loadFile("reports/combinedReport.html");
			rp = rp.replace("%PlotName%", name);

			String patchTable = "<tr><th>Channel</th><th>Type</th><th>Address</th><th>Mode</th></tr>";
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
					patchTable += "<tr><td>" + l.ch + "</td><td>" + l.getTypeF() + "</td><td>" + l.getAdrF() + "</td><td>" + l.dmxMode + "</td></tr>";
				}
			}
			rp = rp.replace("%PatchTable%", patchTable);
			rp = rp.replace("%UniUsed%", uniUsed);
			
			String equipTable = "<tr><th>Type</th><th>Quantity</th></tr>";
			for(int i = 0; i < types.size(); i++) {
				Type t = types.get(i);
				equipTable += "<tr><td>" + t.name + "</td><td>" + t.q + "</td></tr>";
			}
			rp = rp.replace("%EquipTable%", equipTable);
			str = rp;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			str = "Error Loading defualt";
		}
		return str;
	}
	
	private class Type implements Comparable<Type> {
		String name;
		int q;
		
		public Type(String name, int q) {
			this.name = name;
			this.q = q;
		}

		@Override
		public int compareTo(Type o) {
			return q - o.q;
		}
	}

}
