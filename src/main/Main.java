package main;

import nanoHTTPD.util.ServerRunner;
import server.*;

public class Main {
	
	public static DataServer dataServ;
	
	public static void main(String[] args) {
		dataServ = new DataServer("localhost", 1080, new DataStorage());
		ServerRunner.run(DataServer.class);
	}
}
