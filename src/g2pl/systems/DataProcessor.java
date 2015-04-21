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
   
   static final String DB_URL = "jdbc:sqlite:g2pl.db";
   
   static String lockObject = "";
   
   public static void create_database()
   {
	    Connection c = null;
	    Statement stmt = null;
	    try {
	    	
	    	try{
	    	
				Class.forName("org.sqlite.JDBC");  
				c = DriverManager.getConnection(DB_URL);
	    	}catch(Exception e)
	    	{
	    		System.out.println("Unable to connect to database");
	    		System.exit(0);
	    	}
			  
			stmt = c.createStatement();
			String sql = 	"CREATE TABLE `g2pl_database` (" +
				"`item` VARCHAR(50) PRIMARY KEY NOT NULL," +
				"`value` INT(11) NOT NULL"+
				");";
			  
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();
			  
			System.out.println("create database successfully");	   
	      
	    } catch (Exception e) {
	    	// database exists
	    }
   }
   
   public static int get_key(String key)
   {
	   Connection conn = null;
	   Statement stmt = null;
	   int value = 0;
	   try{
		   
		   synchronized(lockObject)
		   {
		   
		      // Register JDBC driver
			   Class.forName("org.sqlite.JDBC");
	
		      // Open a connection
		      conn = DriverManager.getConnection(DB_URL);
		      conn.setAutoCommit(false);
	
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
			   Class.forName("org.sqlite.JDBC");
	
		      // Open a connection
		      conn = DriverManager.getConnection(DB_URL);
		      conn.setAutoCommit(false);
	
		      // Execute a query
		      stmt = conn.createStatement();
		      String sql = "insert or replace into g2pl_database (item, value) VALUES ('"+key+"', "+value+")";
		      int rs = stmt.executeUpdate(sql);
		      conn.commit();
		   
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
