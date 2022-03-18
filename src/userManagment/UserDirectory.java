package userManagment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import util.Hasher;

public class UserDirectory {
	private static String sQLUrl = "jdbc:mysql://localhost:3306/webPlots?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
	private static String sQLUser = "peter";
	private static String sQLPass = "admin";
	
	private static Map<String, User> users;
	private static Map<String, LoginKey> keys;
	
	public static void updateUsers() {
		if(users == null) {
			users = new HashMap<String, User>();
		}
		if(keys == null) {
			keys = new HashMap<String, LoginKey>();
		}
		try {
			Connection conn = DriverManager.getConnection(sQLUrl, sQLUser, sQLPass);
			Statement stmt = conn.createStatement();
			String strSelect = "select name, passHash, perms, lastLogin from users;";
			ResultSet rset = stmt.executeQuery(strSelect);
			while(rset.next()) {
				String name = rset.getString("name");
				String passHash = rset.getString("passHash");
				String perms = rset.getString("perms");
				long lastLogin = rset.getLong("lastLogin");
				User user = new User(name, passHash, perms, lastLogin);
//				System.out.println(user);
				users.put(name, user);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
//		System.out.println(printUsersJSON());
	}
	
	public static void saveUser(User user) {
		try {
			Connection conn = DriverManager.getConnection(sQLUrl, sQLUser, sQLPass);
			Statement stmt = conn.createStatement();
			String strSelect = "update users set passHash = \""+user.getPassHash()+"\", perms = \""+user.getPerms(false)+"\", lastLogin = "+user.getLastLogin()+" where name = \""+user.getName()+"\";";
			/*ResultSet rset = */stmt.execute(strSelect);
//			System.out.println(rset);
			users.put(user.getName(), user);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static User getUser(String username) {
		if(users.containsKey(username)) {
			return users.get(username);
		} else {
			return null;
		}
	}
	
	public static LoginKey login(String username, String password) {
		if(!users.containsKey(username)) {
			return null;
		}
		User user = users.get(username);
		if(!user.checkPass(password)) {
			return null;
		}
		String key = Hasher.Hash1(username + new SimpleDateFormat("MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis())));
//		LoginKey loginKey = new LoginKey(user,"THIS_IS-A^HORIBLE>KEY+FOR~"+username, 60f);
		LoginKey loginKey = new LoginKey(user,key, 60f);
		keys.put(loginKey.key,  loginKey);
		user.setLoginNow();
//		System.out.println(print());
		try {
			Connection conn = DriverManager.getConnection(sQLUrl, sQLUser, sQLPass);
			Statement stmt = conn.createStatement();
			String strSelect = "update users set lastLogin = "+user.getLastLogin()+" where name =  \""+user.getName()+"\";";
			/*ResultSet rset = */stmt.execute(strSelect);
//			System.out.println(rset);
			users.put(user.getName(), user);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return loginKey;
	}
	
	public static User checkKey(String key) {
		if(keys.containsKey(key)) {
			LoginKey loginKey = keys.get(key);
			if(loginKey.checkExpire()) {
				return loginKey.user;
			} else {
				keys.remove(key);
				return null;
			}
		} else {
			return null;
		}
	}

	public static void logout(String key) {
		keys.remove(key);
	}
	
	public static String print() {
		String str = "Users: {\n";
		boolean f = true;
		for(User u : users.values()) {
			if(!f) {
				str += ",\n";
			}
			f = false;
			str += "\t" + u;
		}
		str += "\n}\n";
		str += "Keys: {\n";
		f = true;
		for(LoginKey k : keys.values()) {
			if(!f) {
				str += ",\n";
			}
			f = false;
			str += "\t" + k;
		}
		str += "\n}";
		return str;
	}
	public static String printUsersJSON() {
		String str = "[";
		boolean f = true;
		for(User u : users.values()) {
			if(!f) {
				str += ",";
			}
			f = false;
			str += "{\"name\":\"" + u.getName() + "\",\"perms\":"+u.getPermsJSON()+",\"lastLogin\":"+u.getLastLogin()+"}";
		}
		return str + "]";
	}
	
	public static boolean addUser(User user) {
		if(users.containsKey(user.getName())) {
			return false;
		}
		try {
			Connection conn = DriverManager.getConnection(sQLUrl, sQLUser, sQLPass);
			Statement stmt = conn.createStatement();
			String strSelect = "insert into users values (\""+user.getName()+"\", \""+user.getPassHash()+"\", \""+user.getPerms()+"\", -1);";
			/*ResultSet rset = */stmt.execute(strSelect);
//			System.out.println(rset);
			users.put(user.getName(), user);
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	public static String listUser() {
		String str = "[";
		boolean f = true;
		for(User u : users.values()) {
			if(!f) {
				str += ",";
			}
			f = false;
			str += "\"" + u.getName() + "\"";
		}
		return str + "]";
	}
	
}
