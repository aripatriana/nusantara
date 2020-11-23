package com.nusantara.automate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nusantara.automate.exception.ReachTimeoutException;
import com.nusantara.automate.util.Sleep;

/**
 * Used for query to database
 * 
 * @author ari.patriana
 *
 */
public class DBConnection {

	private static Logger log = LoggerFactory.getLogger(DBConnection.class);
	
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
			ContextLoader.setObjectWithCustom(dbConnection, ConfigLoader.getConfigMap());	
		}
		return dbConnection;
	}
	
	private Connection connect() {
		if (connection == null) {
			//step1 load the driver class  
			try {
				Class.forName(driverClassName);
			} catch (ClassNotFoundException e1) {
				log.error("ERROR ", e1);
			}  
			  
			//step2 create  the connection object  
			try {
				connection = java.sql.DriverManager.getConnection(url, username, password);
			} catch (SQLException e) {
				log.error("ERROR ", e);
			}
		}
		return connection;
	}
	
	public static void executeUpdate(String query) {
		Statement stmt = null;
		try {
			stmt = getConnection().connect().createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			log.error("ERROR ", e);
		} finally {
			if (stmt != null) try {stmt.close();} catch (Exception e) {}
		}
	}
	
	public static List<String[]> selectSimpleQuery(String simpleQuery, String[] columns) {
		List<String[]> results = new ArrayList<String[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement();
			rs=stmt.executeQuery(simpleQuery);  
			while(rs.next()) {  
				String[] result = new String[columns.length];
				for (int i=0; i<columns.length; i++) {
					result[i] = rs.getString(columns[i]);
				}
				results.add(result);
			}
		} catch (SQLException e) {
			log.error("ERROR ", e);
		} finally {
				if (stmt != null) try {stmt.close();} catch (Exception e) {}
				if (rs != null) try {rs.close();} catch (Exception e) {}
			}
		return results;
	}
	
	
	public static List<String[]> selectSimpleQueryAndWait(String simpleQuery, String[] columns, int timeoutInSecond) throws ReachTimeoutException {
		List<String[]> resultList = selectSimpleQuery(simpleQuery, columns);
		long start = System.currentTimeMillis();
		while(resultList == null || resultList.isEmpty()) {
			resultList = selectSimpleQuery(simpleQuery, columns);
			
			long diff = System.currentTimeMillis() - start;
			if (diff > (timeoutInSecond * 1000)) {
				throw new ReachTimeoutException("Reach time out after " + timeoutInSecond + " seconds for " + simpleQuery);
			}
			Sleep.wait(1000);
		}
		return resultList;
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Object[]> selectSimpleQuery(String simpleQuery, String[] columns, Class[] clazz) {
		List<Object[]> results = new ArrayList<Object[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = getConnection().connect().createStatement(); 
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
			log.error("ERROR ", e);
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
				dbConnection = null;
			}
		} catch (SQLException e) {
			log.error("ERROR ", e);
		}
	}
}
