package com.nusantara.automate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

/**
 * Used for query to database
 * 
 * @author ari.patriana
 *
 */
public class DBConnection {

	@Value(value = "simple.datasource.url")
	private String url = "jdbc:oracle:thin:@10.10.105.41:1521:fasdb";
	
	@Value(value = "simple.datasource.username")
	private String username = "EAEPME";
	
	@Value(value = "simple.datasource.password")
	private String password = "EAEPME";
	
	@Value(value = "simple.datasource.driverClassName")
	private String driverClassName = "oracle.jdbc.driver.OracleDriver";
	
	private static DBConnection dbConnection;
	
	private Connection connection;
	
	private synchronized static DBConnection getConnection() {
		if (dbConnection == null) {
			dbConnection = new DBConnection();
			if (ConfigLoader.getConfigMap().size() > 0) {
				ContextLoader.setObjectWithCustom(dbConnection, ConfigLoader.getConfigMap());	
			}
		}
		return dbConnection;
	}
	
	private Connection connect() {
		if (connection == null) {
			//step1 load the driver class  
			try {
				Class.forName(driverClassName);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  
			  
			//step2 create  the connection object  
			try {
				connection = java.sql.DriverManager.getConnection(url, username, password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connection;
	}
	
	public static List<String[]> selectSimpleQuery(String simpleQuery, String[] columns) {
		List<String[]> results = new ArrayList<String[]>();
		//step3 create the statement object  
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			//step4 execute query  
			rs=stmt.executeQuery(simpleQuery);  
			while(rs.next()) {  
				String[] result = new String[columns.length];
				for (int i=0; i<columns.length; i++) {
					result[i] = rs.getString(columns[i]);
				}
				results.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
				if (stmt != null) try {stmt.close();} catch (Exception e) {}
				if (rs != null) try {rs.close();} catch (Exception e) {}
			}
		return results;
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Object[]> selectSimpleQuery(String simpleQuery, String[] columns, Class[] clazz) {
		List<Object[]> results = new ArrayList<Object[]>();
		//step3 create the statement object  
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			//step4 execute query  
			rs=stmt.executeQuery(simpleQuery);  
			while(rs.next()) {  
				Object[] result = new Object[columns.length];
				for (int i=0; i<columns.length; i++) {
					if (clazz[i].equals(Integer.class)) {
						result[i] = rs.getInt(columns[i]);
					} else if (clazz[i].equals(java.util.Date.class)) {
						result[i] = new Date(rs.getDate(columns[i]).getTime());
					} else if (clazz[i].equals(Long.class)) {
						result[i] = rs.getLong((columns[i]));
					} else {
						result[i] = rs.getString(columns[i]);						
					}
				}
				results.add(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}  finally {
			if (stmt != null) try {stmt.close();} catch (Exception e) {}
			if (rs != null) try {rs.close();} catch (Exception e) {}
		}
		return results;
	}
	
	public static void close() {
		try {
			if (!getConnection().connect().isClosed()) {
				getConnection().connect().close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
