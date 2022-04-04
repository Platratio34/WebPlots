package reporting;

import dataManagment.JsonObj;

public class ReportLight implements Comparable<ReportLight> {
	
	public boolean hasDmx;
	public int dmxAddr;
	public int dmxUni;
	public int dmxMode;
	public int ch = -1;
	
	public String type;
	
	public ReportLight(JsonObj obj) {
		if(obj.hasKey("type")) {
			type = obj.getKey("type").string();
		}
		if(obj.hasKey("dmx")) {
			JsonObj DMX = obj.getKey("dmx");
			hasDmx = true;
			if(DMX.hasKey("adr")) {
				dmxAddr = DMX.getKey("adr").integer();
			}
			if(DMX.hasKey("uni")) {
				dmxUni = DMX.getKey("uni").integer();
			}
			if(DMX.hasKey("mode")) {
				dmxMode = DMX.getKey("mode").integer();
			}
			if(DMX.hasKey("ch")) {
				ch = DMX.getKey("ch").integer();
			}
		}
	}
	
	public int getAbsAdr() {
		return ((dmxUni-1)*512)+dmxAddr;
	}

	@Override
	public int compareTo(ReportLight o) {
		if(!hasDmx || !o.hasDmx) {
			return 0;
		}
		int adrD = getAbsAdr() - o.getAbsAdr();
		if(ch > 0 && o.ch > 0) {
			int chD = ch - o.ch;
			if(chD != 0) {
				return chD;
			}
		}
		if(adrD == 0) {
			return dmxMode - o.dmxMode;
		}
		return adrD;
	}
	
	@Override
	public String toString() {
		String str = "Light {";
		str += "type=\"" + type + "\"";
		if(hasDmx) {
			str += "; uni=" + dmxUni;
			str += "; addr=" + dmxAddr;
			str += "; mode=" + dmxMode;
		}
		return str + "}";
	}

	public String getAdrF() {
		return dmxUni + " / " + dmxAddr;
	}
	
	public String getTypeF() {
		switch(type) {
			case "cs-spot":
				return "ColorSource Spot";
			case "cs-spot-db":
				return "ColorSource Spot DB";
			case "ap-150":
				return "AP-150";
			case "dim":
				return "Dimmer";
		}
		return "INVALID TYPE";
	}
	
	
}
