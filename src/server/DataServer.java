package server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.sql.*;
import nanoHTTPD.NanoHTTPD;
import userManagment.LoginKey;
import userManagment.User;
import userManagment.UserDirectory;
import java.io.File;
import java.io.FileInputStream;

public class DataServer extends NanoHTTPD {
	
	private static String sQLUrl = "jdbc:mysql://localhost:3306/webPlots?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
	private static String sQLUser = "peter";
	private static String sQLPass = "admin";
	
	private static String keyCookie = "loginKey";
	
	private static User DefaultUser = new User("Default","");
	
	public DataServer(String hostname, int port) {
		super(hostname, port);
	}
	public DataServer() {
		super("localhost", 80);
		UserDirectory.updateUsers();
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		Map<String, List<String>> params = decodeParameters(session.getQueryParameterString());
		String[] path = session.getUri().substring(1).split("/");
		CookieHandler cookies = session.getCookies();
		Method method = session.getMethod();
		Map<String, List<String>> postBody = new HashMap<String, List<String>>();
		String postData = "";
		
		Response.Status status = Response.Status.INTERNAL_ERROR;
		String mimeType = MIME_PLAINTEXT;
		
//		System.out.println("http://localhost:1080/" + printPath(path) + ";\t Method=" + method);
		
		User user = DefaultUser;
		String userKeyCookie = cookies.read(keyCookie);
		if(userKeyCookie != null) {
			user = UserDirectory.checkKey(userKeyCookie);
			if(user == null) {
				user = DefaultUser;
				cookies.set(keyCookie, "", 1);
				cookies.set("user", "-Delete-", 1);
			}
		}
		
		if(method.equals(Method.POST)) {
			try {
				final HashMap<String, String> map = new HashMap<String, String>();
				session.parseBody(map);
				postData = map.get("postData");
				postBody = decodeParameters(map.get("postData"));
			} catch(IOException e) {
				return newFixedLengthResponse(status, mimeType, "SERVER INTERNAL ERROR: IOExceptoin: " + e.getMessage());
			} catch(ResponseException e) {
				return newFixedLengthResponse(e.getStatus(), mimeType, e.getMessage());
			}
		}
		Connection conn;
		Statement stmt;
		try {
			conn = DriverManager.getConnection(sQLUrl, sQLUser, sQLPass);
			stmt = conn.createStatement();
		} catch (Exception e1) {
			e1.printStackTrace();
			return newFixedLengthResponse(status, mimeType, "Somthing went wrong with the SQL database");
		}
		if(method == Method.POST) {
			if(path.length > 0) {
				if(path[0].equals("login")) {
					if(postBody.containsKey("user") && postBody.containsKey("pass")) {
						LoginKey loginKey = UserDirectory.login(postBody.get("user").get(0), postBody.get("pass").get(0));
//						User tUser = UserDirectory;
//						if(tUser.checkPass(postBody.get("pass").get(0))) {
						if(loginKey != null) {
//							user = tUser;
							cookies.set(keyCookie,loginKey.key,1);
							return newFixedLengthResponse(Response.Status.OK, mimeType, "Login Succseful");
						}
						return newFixedLengthResponse(Response.Status.OK, mimeType, "Login Failed, Invalid username or password");
					} else if(postBody.containsKey("logout")) {
						UserDirectory.logout(userKeyCookie);
						cookies.delete(keyCookie);
						user = DefaultUser;
						return newFixedLengthResponse(Response.Status.OK, mimeType, "Logout Succseful");
					}
					System.out.println(postBody);
					return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"user\", \"pass\"} or {\"logout\"}");
				} else if(path[0].equals("admin")) {
					if(!user.checkPerm("admin")) {
						return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_HTML, PageLoader.getDefaultPage("Invalid Permissions; Must have permesion \"admin\""));
					}
					if(path.length == 2) {
						if(path[1].equals("newUser")) {
							if(!user.checkPerm("admin.usr")) {
								return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_HTML, PageLoader.getDefaultPage("Invalid Permissions; Must have permesion \"admin.usr\""));
							}
							if(!postBody.containsKey("name") || !postBody.containsKey("pass") || !postBody.containsKey("perm")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"name\", \"pass\", \"perm\"}");
							}
							User newUser = new User(postBody.get("name").get(0), postBody.get("pass").get(0));
							newUser.addPerm(postBody.get("perm").get(0));
							if(UserDirectory.addUser(newUser)) {
								System.out.println("User by name \""+newUser.getName()+"\" created with permissions [" + newUser.getPerms() + "]");
								return newFixedLengthResponse(Response.Status.OK, mimeType, "User by name \""+newUser.getName()+"\" created with permissions [" + newUser.getPerms() + "]");
							} else {
								return newFixedLengthResponse(Response.Status.OK, mimeType, "User by name \""+newUser.getName()+"\" already exists");
							}
						} else if(path[1].equals("perm")) {
							if(!postBody.containsKey("action") || !postBody.containsKey("perm") ||  !postBody.containsKey("user")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"action\", \"perm\", \"user\"}");
							}
							User u = UserDirectory.getUser(postBody.get("user").get(0));
							String m = postBody.get("action").get(0);
							String p = postBody.get("perm").get(0);
							if(m.equals("add")) {
								u.addPerm(p);
								UserDirectory.saveUser(u);
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Added permission \""+p+"\" to user \""+u.getName()+"\"");
							} else if(m.equals("remove")) {
								u.removePerm(p);
								UserDirectory.saveUser(u);
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Removed permission \""+p+"\" to user \""+u.getName()+"\"");
							} else {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Invalid action, must be \"add\" or \"remove\"");
							}
						} else if(path[1].equals("user")) {
							if(!user.checkPerm("admin.usr")) {
								return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_HTML, PageLoader.getDefaultPage("Invalid Permissions"));
							}
							if(!postBody.containsKey("action") || !postBody.containsKey("user")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"action\", \"user\"}");
							}
							String action = postBody.get("action").get(0);
							User u = UserDirectory.getUser(postBody.get("user").get(0));
							if(action.equals("setPass")) {
								if(!postBody.containsKey("pass")) {
									return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"pass\"}");
								}
								String nPas = postBody.get("pass").get(0);
								u.setPass(nPas);
								UserDirectory.saveUser(u);
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Set password for user \""+u.getName()+"\"");
							} else if(action.equals("addPerm")) {
								if(!postBody.containsKey("perm")) {
									return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"perm\"}");
								}
								String p = postBody.get("perm").get(0);
								if(!user.checkPerm(p)) {
									return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "Invalid Permissions; Must have permesion \""+p+"\"");
								}
								u.addPerm(p);
								UserDirectory.saveUser(u);
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Added permission \""+p+"\" to user \""+u.getName()+"\"");
							} else if(action.equals("removePerm")) {
								if(!postBody.containsKey("perm")) {
									return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"perm\"}");
								}
								String p = postBody.get("perm").get(0);
								if(!user.checkPerm(p)) {
									return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "Invalid Permissions; Must have permesion \""+p+"\"");
								}
								u.removePerm(p);
								UserDirectory.saveUser(u);
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Removed permission \""+p+"\" to user \""+u.getName()+"\"");
							}
						}
					}
				} else if(path[0].equals("plot")) {
					if(!user.checkPerm("plots")) {
						return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "Invalid Permissions; Must have permesion \"plots\"");
					}
					if(path.length == 3) {
						String id = path[1];
						if(path[2].equals("save")) {
							try {
								ResultSet rset = stmt.executeQuery("select plot from sharePlots where user = \""+user.getName()+"\";");
								while(rset.next()) {
									if(postBody.containsKey("title")) {
										stmt.executeUpdate("update plots set name = \"" + postBody.get("title").get(0) + "\" where id = \"" + id + "\";");
										return newFixedLengthResponse(Response.Status.OK, mimeType, "Updated plot title");
									} else if(postBody.containsKey("desc")) {
										stmt.executeUpdate("update plots set desciption = \"" + postBody.get("desc").get(0) + "\" where id = \"" + id + "\";");
										return newFixedLengthResponse(Response.Status.OK, mimeType, "Updated plot description");
									}
									String plot = rset.getString("plot");
									if(plot.equals(id)) {
										postData = postData.replace("\"", "\\\"");
										String strSelect = "update plots set data = \"" + postData + "\" where id = \"" + id + "\";";
										/*ResultSet rset = */stmt.execute(strSelect);
//										System.out.println(rset);
										return newFixedLengthResponse(Response.Status.OK, mimeType, "Saved plot");
									}
								}
								return newFixedLengthResponse(Response.Status.FORBIDDEN, mimeType, "You can not save that plot");
								
							} catch (SQLException ex) {
								ex.printStackTrace();
								return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL ERROR");
							}
						} else if(path[2].equals("share")) {
							if(!user.checkPerm("plots.share")) {
								return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "Invalid Permissions; Must have permesion \"plots.share\"");
							}
							try {
								ResultSet rset = stmt.executeQuery("select * from sharePlots where plot = \""+id+"\" and user = \""+user.getName()+"\"");
								if(!rset.next()) {
									return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "You must be able to edit the plot to share it!");
								}
								if(!postBody.containsKey("user")) {
									return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"user\"}");
								}
								if(!postBody.containsKey("action")) {
									return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"action\"}");
								}
								//share it here;
								if(postBody.get("action").get(0).equals("share")) {
									rset = stmt.executeQuery("select * from sharePlots where plot = \"" + id+"\" and user = \"" + postBody.get("user").get(0) + "\"");
									if(!rset.next()) {
										stmt.execute("insert into sharePlots values (\"" + path[1] + "\", \"" + postBody.get("user").get(0) + "\")");
									}
									return newFixedLengthResponse(Response.Status.OK, mimeType, "Plot shared with " + postBody.get("user").get(0));
								} else {
									stmt.execute("delete from sharePlots where plot=\"" + path[1] + "\" and user=\"" + postBody.get("user").get(0) + "\"");
									return newFixedLengthResponse(Response.Status.OK, mimeType, "Plot unshared with " + postBody.get("user").get(0));
								}
							} catch (Exception e) {
								e.printStackTrace();
								return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL ERROR");
							}
						}
					} else if(path.length == 2) {
						if(path[1].equals("new")) {
							if(!user.checkPerm("plots.create")) {
								return newFixedLengthResponse(Response.Status.UNAUTHORIZED, mimeType, "Invalid Permissions; Must have permesion \"plots.create\"");
							}
							if(!postBody.containsKey("name")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"name\"}");
							}
							if(!postBody.containsKey("desc")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"desc\"}");
							}
							String name = postBody.get("name").get(0);
							String desc = postBody.get("desc").get(0);
							try {
								String id = getSaltString(8);
								boolean bad = true;
								while(bad) {
									ResultSet rset = stmt.executeQuery("select name from plots where id = \""+id+"\"");
									if(rset.next()) {
										id = getSaltString(8);
										bad = true;
									} else {
										bad = false;
									}
								}
								stmt.execute("insert into plots (id, name, desciption, owner, data) values (\""+id+"\", \""+name+"\", \""+desc+"\", \""+user.getName()+"\", \"data={size:[50,50],lights:[],bars:[]}\")");
								stmt.execute("insert into sharePlots (plot, user) values (\""+id+"\", \""+user.getName()+"\");");
								
								
								return newFixedLengthResponse(Response.Status.OK, mimeType, "Created new plot with id \""+id+"\", named \""+name+"\"");
							} catch (SQLException ex) {
								ex.printStackTrace();
								return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL ERROR");
							}
						}
					}
				} else if(path[0].equals("user")) {
					if(user == DefaultUser) {
						return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "You must be looged in to make user changes");
					}
					if(path.length == 2) {
						if(path[1].equals("setPass")) {
							if(!postBody.containsKey("oPass")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"oPass\"}");
							}
							if(!postBody.containsKey("nPass")) {
								return newFixedLengthResponse(Response.Status.BAD_REQUEST, mimeType, "Missing body elements: {\"nPass\"}");
							}
							String oPass = postBody.get("oPass").get(0);
							String nPass = postBody.get("nPass").get(0);
							if(!user.checkPass(oPass)) {
								return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Current password must be provided");
							}
							UserDirectory.setPass(user, nPass);
							return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Set password for user " + user.getName());
						}
					}
				}
			}
			return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Invalid Post Request");
		} else {
			if(path.length > 0) {
				if(path[0].equals("")) {
					Response r = newFixedLengthResponse(Response.Status.TEMPORARY_REDIRECT, MIME_PLAINTEXT, "");
		            r.addHeader("Location", "/home");
		            return r;
				} else if(path[0].equals("style")) {
					return newFixedLengthResponse(Response.Status.OK, "text/css", PageLoader.getCSS(printPath(path,1)));
				} else if(path[0].equals("script")) {
					return newFixedLengthResponse(Response.Status.OK, "text/javascript", PageLoader.getJS(printPath(path,1)));
				} else if(path[0].equals("home")) {
					return newFixedLengthResponse(Response.Status.OK, MIME_HTML, PageLoader.getPage("home"));
				} else if(path[0].equals("favicon.ico")) {
					try {
						InputStream is = new FileInputStream(new File("base/favicon.ico"));
						return newChunkedResponse(Response.Status.OK, "image/x-icon", is);
					} catch (IOException e) {
						System.out.println("Faild to load favicon.ico");
						e.printStackTrace();
						return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to load icon");
					}
				} else if(path.length == 1 && path[0].equals("login")) {
					return newFixedLengthResponse(Response.Status.OK, MIME_HTML, PageLoader.getPage("login"));
				} else if(path[0].equals("admin")) {
					if(user == DefaultUser) {
			            return loginRedirect(path);
					}
					if(user.checkPerm("admin")) {
						if(path.length == 2) {
							if(path[1].equals("users")) {
								if(params.containsKey("list")) {
									return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, UserDirectory.printUsersJSON());
								} else {
									System.out.println(params);
								}
							}
						}
						return newFixedLengthResponse(Response.Status.OK, MIME_HTML, PageLoader.getPage(printPath(path)));
					} else {
						return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_HTML, PageLoader.getDefaultPage("Invalid Permissions; Must have permesion \"admin\""));
					}
				} else if(path[0].equals("perms")) {
//					System.out.println(user);
					return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, user.getPermsJSON());
				} else if(path[0].equals("user")) {
					if(user != DefaultUser) {
						return newFixedLengthResponse(Response.Status.OK, MIME_HTML, PageLoader.getPage(printPath(path)));
					} else {
						return loginRedirect(path);
					}
				} else if(path[0].equals("plot")) {
					if(user == DefaultUser) {
			            return loginRedirect(path);
					}
					if(!user.checkPerm("plots")) {
						return newFixedLengthResponse(Response.Status.UNAUTHORIZED, MIME_HTML, PageLoader.getDefaultPage("Invalid Permissions; Must have permesion \"plots\""));
					}
					if(path.length == 1) {
						return newFixedLengthResponse(Response.Status.OK, MIME_HTML, PageLoader.getPage("plot"));
					} else if(path.length == 2) {
						if(path[1].equals("list")) {
							try {
								String rsp = "[";

								ResultSet rset = stmt.executeQuery("select plot from sharePlots where user = \""+user.getName()+"\"");
								
								String plots = "";
								boolean first = true;
								int nPlots = 0;
								while(rset.next()) {
									nPlots++;
									if(!first) {
										plots += ", ";
									}
									first = false;
									plots += "\""+rset.getString("plot")+"\"";
								}
								if(nPlots > 1) {
									plots = "IN ("+plots+")";
								} else {
									plots = "= "+plots;
								}
								
								if(plots == "") {
									return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "");
								}
								rset = stmt.executeQuery("select id, owner, name, desciption from plots where id " + plots + ";");
								while(rset.next()) {
									String id = rset.getString("id");
									String owner = rset.getString("owner");
									String desc = rset.getString("desciption");
									String name = rset.getString("name");
									if(!rsp.equals("[")) {
										rsp += ",";
									}
									rsp += "{\"id\":\"" + id + "\",\"owner\":\"" + owner + "\",\"name\":\"" + name + "\",\"desc\":\"" + desc + "\"}";
								}
								rsp += "]";
								return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, rsp);
							} catch (SQLException ex) {
								ex.printStackTrace();
								return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL ERROR");
							}
						} else {
							Response r = newFixedLengthResponse(Response.Status.TEMPORARY_REDIRECT, MIME_PLAINTEXT, "");
				            r.addHeader("Location", "/plot/" + path[1] + "/edit");
				            return r;
						}
					} else if(path.length == 3) {
						try {
							String strSelect = "select * from plots where id = \"" + path[1] + "\";";
							ResultSet rset = stmt.executeQuery(strSelect);
							if(!rset.next()) {
								return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, PageLoader.getDefaultPage("Plot Not Found"));
							}
							if(path[2].equals("getEditors")) {
								rset.close();
								rset = stmt.executeQuery("select user from sharePlots where plot = \"" + path[1] + "\";");
								String users = "";
								while(rset.next()) {
									if(!users.equals("")) {
										users += ", ";
									}
									users += rset.getString("user");
								}
								return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, users);
							}
							String id = rset.getString("id");
							String owner = rset.getString("owner");
							String desc = rset.getString("desciption");
							String name = rset.getString("name");
							String data = rset.getString("data");
							String plot = "{\"id\":\"" + id + "\",\"owner\":\"" + owner + "\",\"name\":\"" + name + "\",\"desc\":\"" + desc + "\"}";
							String page = "";
							if(path[2].equals("edit")) {
								rset.close();
								rset = stmt.executeQuery("select * from sharePlots where plot = \"" + id + "\" and user = \"" + user.getName() + "\";");
								if(!rset.next()) {
									Response r = newFixedLengthResponse(Response.Status.TEMPORARY_REDIRECT, MIME_HTML, "");
						            r.addHeader("Location", "/plot/" + path[1] + "/veiw");
						            return r;
								}
								page = PageLoader.getPage("plotEditor");
							} else if(path[2].equals("veiw")) {
								page = PageLoader.getPage("plotVeiwer");
							} else {
								return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, PageLoader.getPage("endOfTheWorld"));
							}
							page = page.replace("%PlotData%",plot);
							page = page.replace("%Data%", data);
							return newFixedLengthResponse(Response.Status.OK, MIME_HTML, page);
						} catch (SQLException ex) {
							ex.printStackTrace();
							return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SQL ERROR");
						}
					}
				}
			}
			return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, PageLoader.getPage("endOfTheWorld"));
		}
	}
	
	@SuppressWarnings("unused")
	private String padTo(String input, int l) {
		if(input.length() >= l) {
			return input;
		}
		for(int i = input.length(); i < l; i++) {
			input += " ";
		}
		return input;
	}
	
	private String printPath(String[] path) {
		return printPath(path,0);
	}
	private String printPath(String[] path, int start) {
		String print = "";
		for(int i = start; i< path.length; i++) {
			if(i>start) {
				print += "/";
			}
			print += path[i];
		}
		return print;
	}
	
	protected String getSaltString(int l) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < l) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
	
	private Response loginRedirect(String[] path) {
		return loginRedirect(printPath(path));
	}
	private Response loginRedirect(String path) {
		Response r = newFixedLengthResponse(Response.Status.REDIRECT_SEE_OTHER, MIME_PLAINTEXT, "");
        r.addHeader("Location", "/login?target=/" + path);
        return r;
	}
}
