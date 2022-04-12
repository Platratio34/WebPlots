package userManagment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import dataManagment.JsonObj;
import util.Hasher;

public class User {
	
	private String name;
	private String passHash;
	private List<String> perms;
	private long lastLogin;
	
	public User(String name, String pass) {
		this.name = name;
		this.passHash = Hasher.Hash1(pass);
		perms = new ArrayList<String>();
	}
	public User(String name, String pass, String[] perms) {
		this.name = name;
		this.passHash = Hasher.Hash1(pass);
		this.perms = new ArrayList<String>();
		addPerms(perms);
	}
	
	public User(String name, String passHash, String perms, long lastLogin) {
		this.name = name;
		this.passHash = passHash;
		this.perms = new ArrayList<String>();
		this.lastLogin = lastLogin;
		if(perms != null) {
			addPerms(perms.split(","));
		}
	}
	public boolean checkPass(String pass) {
		return passHash.equals(Hasher.Hash1(pass));
	}
	
	public boolean checkHashPass(String passHashed) {
		return passHash.equals(passHashed);
	}
	
	public String getName() {
		return name+"";
	}
	
	public long getLastLogin() {
		return lastLogin;
	}
	public String getLastLoginF() {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastLogin), TimeZone.getDefault().toZoneId()).toString();
	}
	
	public void setLoginNow() {
		lastLogin = System.currentTimeMillis();
	}
	
	public void addPerm(String perm) {
		perms.add(perm);
//		System.out.println(perms);
	}
	public void addPerms(String[] perms) {
		for(int i = 0; i < perms.length; i++) {
			addPerm(perms[i]);
		}
	}
	
	public void removePerm(String perm) {
		perms.remove(perm);
	}
	public void removePerms(String[] perms) {
		for(int i = 0; i < perms.length; i++) {
			removePerm(perms[i]);
		}
	}
	
	public String getPerms() {
		return getPerms(true);
	}
	public String getPerms(boolean q) {
		String str = "";
		String qs = q?"\"":"";
		for(int i = 0; i < perms.size(); i++) {
			if(i>0) str += ",";
			str += qs + perms.get(i) + qs;
		}
		return str;
	}
	public String getPermsJSON() {
		JsonObj obj = new JsonObj();
		for(int i = 0; i < perms.size(); i++) {
			obj.addArray(perms.get(i));
		}
		return obj.toString();
	}
	
//	public void setPerm(String perm, boolean a) {
//		permMap.put(perm, a);
//	}
	
	public boolean checkPerm(String perm) {
		if(perms.contains(perm)) {
	        return true;
	    }
	    if(perms.contains("*")) {
	        return true;
	    }
	    if(perms.contains(perm+".*")) {
			return true;
		}
	    if(perm.contains(".")) {
	        for(int i = perm.length(); i > 0; i--) {
//	        	System.out.println();
	            if(perm.substring(i-1,i).equals(".")) {
//                	System.out.println(perm.substring(0,i) + "*");
	                if(perms.contains( perm.substring(0,i) + "*") ) {
	                    return true;
	                }
	            }
	        }
	    }
	    return false;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other.getClass() != getClass()) {
			return false;
		}
		return ((User)other).equalsUser(name,passHash);
	}
	protected boolean equalsUser(String name, String passHash) {
		return this.name.equals(name) && this.passHash.equals(passHash);
	}
	
	@Override
	public String toString() {
		return "User:{name=\""+name+"\", passHash=\""+passHash+"\", perms="+perms+"}";
	}
	public String getPassHash() {
		return passHash;
	}
	public void setPass(String pass) {
		this.passHash = Hasher.Hash1(pass);
	}
}
