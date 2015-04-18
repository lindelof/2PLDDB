/*******************************************************************************
 * Copyright (c) 2015
 * Author: Yidan(Evan) Zheng
 *     
 *     This file is part of 2PLDDB.
 *
 *     2PLDDB is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package g2pl.systems;

import java.sql.*;

public class DataProcessor {
	
   // JDBC driver name and database URL
   static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
   static final String DB_URL = "jdbc:mysql://localhost/g2pl";

   //  Database credentials
   static final String USER = "root";
   static final String PASS = "p@ssw0rd";
   
   static String lockObject = "";
   
   public static int get_key(String key)
   {
	   Connection conn = null;
	   Statement stmt = null;
	   int value = 0;
	   try{
		   
		   synchronized(lockObject)
		   {
		   
		      // Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
	
		      // Open a connection
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
	
		      // Execute a query
		      stmt = conn.createStatement();
		      String sql = "SELECT value FROM g2pl_database where item = \"" + key +"\"";
		      ResultSet rs = stmt.executeQuery(sql);
		      
		      if(rs.next())
		    	  value = rs.getInt("value");
		   
		      rs.close();
		      
		   }
	      
	   }catch(SQLException se){
		      se.printStackTrace();
		   }catch(Exception e){
		      e.printStackTrace();
		   }finally{
		      try{
		         if(stmt!=null)
		            conn.close();
		      }catch(SQLException se){
		      }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
	   
	   return value;
   }
   
   public static Boolean update_key(String key, int value) {

	   Connection conn = null;
	   Statement stmt = null;
	   try{
		   
		   synchronized(lockObject)
		   {
		   
		      // Register JDBC driver
		      Class.forName("com.mysql.jdbc.Driver");
	
		      // Open a connection
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
	
		      // Execute a query
		      stmt = conn.createStatement();
		      String sql = "insert into g2pl_database (item, value) VALUES ('"+key+"', "+value+") ON DUPLICATE KEY UPDATE value = "+value;
		      int rs = stmt.executeUpdate(sql);
		   
		      // Clean-up environment
		      stmt.close();
		      conn.close();
		      
		      if(rs > 0)
		    	  return true;
		   }
	      
	   }catch(SQLException se){
	      se.printStackTrace();
	   }catch(Exception e){
	      e.printStackTrace();
	   }finally{
	      try{
	         if(stmt!=null)
	            stmt.close();
	      }catch(SQLException se2){
	      }
	      
	      try{
	         if(conn!=null)
	            conn.close();
	      }catch(SQLException se){
	         se.printStackTrace();
	      }   
	      
	   }  
	   return false;
	}
	
}
