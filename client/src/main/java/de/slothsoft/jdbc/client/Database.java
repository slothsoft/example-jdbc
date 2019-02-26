package de.slothsoft.jdbc.client;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import de.slothsoft.jdbc.core.GameException;
import de.slothsoft.jdbc.core.GameManager;
import de.slothsoft.jdbc.impl.GameManagerImpl;

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
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException("Could not find JDBC driver!", e);
	} catch (SQLException e) {
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
	} catch (SQLException e) {
	    throw new RuntimeException("Could not close connection!", e);
	}
    }

}
