package main;

import nanoHTTPD.util.ServerRunner;
import server.*;

public class Main {
	
	public static DataServer dataServ;
	
	public static void main(String[] args) {
		System.out.println("Starting Server . . .");
		ServerRunner.run(DataServer.class);
	}
}