package de.slothsoft.example.jdbc.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.slothsoft.example.jdbc.core.GameException;
import de.slothsoft.example.jdbc.core.GameManager;
import de.slothsoft.example.jdbc.impl.GameManagerImpl;

/**
 * This class is for creating a connection with our database.
 *
 * @author Stef Schulz
 * @since 1.0.0
 */

public class Database {

	private static final String DATABASE_DRIVER = "org.sqlite.JDBC";
	private static final String DATABASE_URL = "jdbc:sqlite:sample.db";

	private static Database instance = new Database();

	public static Database getInstance() {
		return instance;
	}

	private final Connection connection;

	private GameManager gameManager;

	private Database() {
		try {
			Class.forName(DATABASE_DRIVER);
			this.connection = DriverManager.getConnection(DATABASE_URL);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException("Could not find JDBC driver!", e);
		} catch (final SQLException e) {
			throw new RuntimeException("Could not open connection to database!", e);
		}
	}

	public synchronized GameManager getGameManager() throws GameException {
		if (this.gameManager == null) {
			this.gameManager = new GameManagerImpl(this.connection);
		}
		return this.gameManager;
	}

	public void destroy() {
		try {
			this.connection.close();
		} catch (final SQLException e) {
			throw new RuntimeException("Could not close connection!", e);
		}
	}

}
