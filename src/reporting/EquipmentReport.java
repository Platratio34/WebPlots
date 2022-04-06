package reporting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dataManagment.JsonObj;
import server.PageLoader;

public class EquipmentReport extends Report {
	
	private ArrayList<Type> types;

	public EquipmentReport(JsonObj obj, String name) {
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
			String rp = PageLoader.loadFile("reports/equipmentReport.html");
			rp = rp.replace("%PlotName%", name);
			String tableInner = "<tr><th>Type</th><th>Quantity</th></tr>";
			for(int i = 0; i < types.size(); i++) {
				Type t = types.get(i);
				tableInner += "<tr><td>" + t.name + "</td><td>" + t.q + "</td></tr>";
			}
			rp = rp.replace("%TableInner%", tableInner);
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
