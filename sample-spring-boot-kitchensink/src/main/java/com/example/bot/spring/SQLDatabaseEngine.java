package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {
	@Override
	String search(String text) throws Exception {
		
		String result = null;

		try {
			Connection connection = getConnection();
			PreparedStatement stmt = connection.prepareStatement(
					"select keyword, response from dialogue" );
			
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
				if (text.toLowerCase().contains(rs.getString(1))) {
					result = rs.getString(2);
					
					break;
				}
			}
			
			rs.close();
			stmt.close();
			
			if (result != null) {
				
				//update hit
				stmt = connection.prepareStatement(
						"update dialogue set hit = hit + 1 where response = ?" );
				stmt.setString(1, result);
				stmt.executeUpdate();
				stmt.close();
				
				//get hit
				stmt = connection.prepareStatement(
						"select hit from dialogue where response = ?");
				stmt.setString(1, result);
				rs = stmt.executeQuery();
				if (rs.next()) {
					result += "\nhit(s): " + rs.getInt(1);
				}
			}
			
			connection.close();
			
		} catch (Exception e) {
			log.info("Exception while reading data from database: {}", e.toString());
		}
		
		if (result != null) {
			return result;
		}

		throw new Exception("NOT FOUND");
	}
	
	
	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}

}
