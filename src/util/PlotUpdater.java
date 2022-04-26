package util;

import java.util.HashMap;

import dataManagment.JsonObj;

public class PlotUpdater {
	
	public static int cVersion = 2;
	
	public static int version(JsonObj obj) {
		if(obj.hasKey("version")) {
			return obj.getKey("version").integer();
		}
		return 0;
	}
	
	public static boolean update(JsonObj obj) {
		int v = version(obj);
//		System.out.println(v);
		if(v >= cVersion) {
			return false;
		}
		switch(v) {
			case 0:
				v = 1;
//				System.out.println(version(obj));
				JsonObj[] ls = obj.getKey("lights").getArr();
				for(int i = 0; i < ls.length; i++) {
					JsonObj l = ls[i];
					l.setKey("id", "l" + i);
					l.setKey("cd", false);
				}
				JsonObj[] bs = obj.getKey("bars").getArr();
				for(int i = 0; i < bs.length; i++) {
					JsonObj b = bs[i];
					b.setKey("id", "b" + i);
					b.setKey("cd", false);
				}
			case 1:
				v = 2;
				ls = obj.getKey("lights").getArr();
				JsonObj lsO = new JsonObj();
				obj.setKey("lights", lsO);
				for(int i = 0; i < ls.length; i++) {
					lsO.setKey(ls[i].getKey("id").string(), ls[i]);
//					JsonObj l = ls[i];
//					l.setKey("id", "l" + i);
//					l.setKey("cd", false);
					
				}
				bs = obj.getKey("bars").getArr();
				JsonObj bsO = new JsonObj();
				obj.setKey("bars", bsO);
				for(int i = 0; i < bs.length; i++) {
					bsO.setKey(bs[i].getKey("id").string(), bs[i]);
//					JsonObj b = bs[i];
//					b.setKey("id", "b" + i);
//					b.setKey("cd", false);
				}
		}
		obj.setKey("version", v);
		System.out.println("Updated to v" + v);
		if(v != cVersion) {
			update(obj);
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static JsonObj addUpdate(JsonObj b, JsonObj n) {
		int vb = version(b);
		if(vb < cVersion) {
			update(b);
		}
		int vn = version(n);
		if(vn < cVersion) {
			update(n);
		}
		
		JsonObj lightsB = b.getKey("lights");
		HashMap<String, JsonObj> lMapB = (HashMap<String, JsonObj>) lightsB.getMap().clone();
		JsonObj lightsN = n.getKey("lights");
		HashMap<String, JsonObj> lMapN = (HashMap<String, JsonObj>) lightsN.getMap().clone();
		
		for(HashMap.Entry<String, JsonObj> ent : lMapB.entrySet()) {
			String k = ent.getKey();
			if(lMapN.containsKey(k)) {
				JsonObj o = lMapN.get(k);
				if(o.getKey("cd").bool()) {
					if(o.getKey("type").string().equals("-Delete-")) {
						lightsB.removeKey(k);
					} else {
						lightsB.setKey(k, o);
					}
				}
				lMapN.remove(k);
			}
		}
		for(HashMap.Entry<String, JsonObj> ent : lMapN.entrySet()) {
			lightsB.setKey(ent.getKey(), ent.getValue());
		}
		
		
		
		JsonObj barsB = b.getKey("bars");
		HashMap<String, JsonObj> bMapB = (HashMap<String, JsonObj>) barsB.getMap().clone();
		JsonObj barsN = n.getKey("bars");
		HashMap<String, JsonObj> bMapN = (HashMap<String, JsonObj>) barsN.getMap().clone();
		
		for(HashMap.Entry<String, JsonObj> ent : bMapB.entrySet()) {
			String k = ent.getKey();
			if(bMapN.containsKey(k)) {
				JsonObj o = bMapN.get(k);
				if(o.getKey("cd").bool()) {
					if(o.getKey("type").string().equals("-Delete-")) {
						barsB.removeKey(k);
					} else {
						barsB.setKey(k, o);
					}
				}
				bMapN.remove(k);
			}
		}
		for(HashMap.Entry<String, JsonObj> ent : bMapN.entrySet()) {
			barsB.setKey(ent.getKey(), ent.getValue());
		}

//		JsonObj[] ls = b.getKey("lights").getArr();
//		JsonObj[] ln = n.getKey("lights").getArr();
//		for(int i = 0; i < ln.length; i++) {
//			if(ln[i].getKey("cd").bool()) {
//				boolean c = false;
//				for(int j = 0; j < ls.length; j++) {
//					if(ls[j].getKey("id").equals(ln[i].getKey("id"))) {
//						ln[i].setKey("cd", false);
//						ls[j] = ln[i];
//						c = true;
//					}
//				}
//				if(!c) {
//					ln[i].setKey("cd", false);
//					b.addArray(ln[i]);
//				}
//			}
//		}
//		
//		JsonObj[] bs = b.getKey("bars").getArr();
//		JsonObj[] bn = n.getKey("bars").getArr();
//		for(int i = 0; i < bn.length; i++) {
//			if(bn[i].getKey("cd").bool()) {
//				boolean c = false;
//				for(int j = 0; j < bs.length; j++) {
//					if(bs[j].getKey("id").equals(bn[i].getKey("id"))) {
//						bn[i].setKey("cd", false);
//						bs[j] = bn[i];
//						c = true;
//					}
//				}
//				if(!c) {
//					bn[i].setKey("cd", false);
//					b.addArray(bn[i]);
//				}
//			}
//		}
		
		return b;
	}
}
