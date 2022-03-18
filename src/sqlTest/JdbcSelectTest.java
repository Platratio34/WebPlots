package sqlTest;

import java.sql.*;  // Using 'Connection', 'Statement' and 'ResultSet' classes in java.sql package
 
public class JdbcSelectTest {
	
	private static String url = "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
	private static String user = "peter";
	private static String pass = "admin";
	
	public static void main(String[] args) {
		select();
	}
   
   public static void select() {
	   try (
		         // Step 1: Construct a database 'Connection' object called 'conn'
		         Connection conn = DriverManager.getConnection(url, user, pass);   // For MySQL only
		               // The format is: "jdbc:mysql://hostname:port/databaseName", "username", "password"
		 
		         // Step 2: Construct a 'Statement' object called 'stmt' inside the Connection created
		         Statement stmt = conn.createStatement();
		      ) {
		         // Step 3: Write a SQL query string. Execute the SQL query via the 'Statement'.
		         //  The query result is returned in a 'ResultSet' object called 'rset'.
		         String strSelect = "select title, price, qty from books";
		         System.out.println("The SQL statement is: " + strSelect + "\n"); // Echo For debugging
		 
		         ResultSet rset = stmt.executeQuery(strSelect);
		 
		         // Step 4: Process the 'ResultSet' by scrolling the cursor forward via next().
		         //  For each row, retrieve the contents of the cells with getXxx(columnName).
		         System.out.println("The records selected are:");
		         int rowCount = 0;
		         // Row-cursor initially positioned before the first row of the 'ResultSet'.
		         // rset.next() inside the whole-loop repeatedly moves the cursor to the next row.
		         // It returns false if no more rows.
		         while(rset.next()) {   // Repeatedly process each row
		            String title = rset.getString("title");  // retrieve a 'String'-cell in the row
		            double price = rset.getDouble("price");  // retrieve a 'double'-cell in the row
		            int    qty   = rset.getInt("qty");       // retrieve a 'int'-cell in the row
		            System.out.println(title + ", " + price + ", " + qty);
		            ++rowCount;
		         }
		         System.out.println("Total number of records = " + rowCount);
		 
		      } catch(SQLException ex) {
		         ex.printStackTrace();
		      }  // Step 5: Close conn and stmt - Done automatically by try-with-resources (JDK 7)
   }

   public static void update() {
	   try(
		   // Step 1: Allocate a database 'Connection' object
		   Connection conn = DriverManager.getConnection(url, user, pass);    // for MySQL only
	
	       // Step 2: Allocate a 'Statement' object in the Connection
	       Statement stmt = conn.createStatement();
	    ) {
	       // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
	       //   which returns an int indicating the number of rows affected.
	       // Increase the price by 7% and qty by 1 for id=1001
	       String strUpdate = "update books set price = price*1.07, qty = qty+1 where id = 1001";
	       System.out.println("The SQL statement is: " + strUpdate + "\n");  // Echo for debugging
	       int countUpdated = stmt.executeUpdate(strUpdate);
	       System.out.println(countUpdated + " records affected.\n");
	
	       // Step 3 & 4 (again): Issue a SELECT (via executeQuery()) to check the UPDATE.
	       String strSelect = "select * from books where id = 1001";
	       System.out.println("The SQL statement is: " + strSelect + "\n");  // Echo for debugging
	       ResultSet rset = stmt.executeQuery(strSelect);
	       while(rset.next()) {   // Move the cursor to the next row
	          System.out.println(rset.getInt("id") + ", "
	                  + rset.getString("author") + ", "
	                  + rset.getString("title") + ", "
	                  + rset.getDouble("price") + ", "
	                  + rset.getInt("qty"));
	       }
	    } catch(SQLException ex) {
	       ex.printStackTrace();
	  }  // Step 5: Close conn and stmt - Done automatically by try-with-resources
   }
}