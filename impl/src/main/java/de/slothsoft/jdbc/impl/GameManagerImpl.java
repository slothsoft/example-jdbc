package de.slothsoft.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import de.slothsoft.jdbc.core.Game;
import de.slothsoft.jdbc.core.GameException;
import de.slothsoft.jdbc.core.GameException.Code;
import de.slothsoft.jdbc.core.GameManager;

public class GameManagerImpl implements GameManager {

    private final Connection connection;

    public GameManagerImpl(Connection connection) throws GameException {
	try {
	    this.connection = connection;
	    executeUpdate("CREATE TABLE IF NOT EXISTS game (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, releaseYear INT);");
	} catch (Exception e) {
	    throw new GameException(Code.INITIALIZATION_ERROR, e);
	}
    }

    @Override
    public Game createGame(Game game) throws GameException {
	try {
	    ResultSet resultSet = executePreparedUpdate("INSERT INTO game (name, releaseYear) VALUES(?, ?)",
		    game.getName(), game.getReleaseYear());
	    return getGame(resultSet.getInt(1));
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    @Override
    public Game getGame(int id) throws GameException {
	try {
	    ResultSet resultSet = executePrepared("SELECT id, name, releaseYear FROM game where id=?", id);
	    if (!resultSet.next())
		throw new GameException(Code.NO_GAME_FOUND);
	    return convertToGame(resultSet);
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    private Game convertToGame(ResultSet resultSet) throws SQLException {
	Game game = new Game();
	game.setId(resultSet.getInt(1));
	game.setName(resultSet.getString(2));
	game.setReleaseYear(resultSet.getInt(3));
	return game;
    }

    @Override
    public Game updateGame(Game game) throws GameException {
	try {
	    ResultSet resultSet = executePreparedUpdate("UPDATE game SET name=?, releaseYear=? WHERE id=?",
		    game.getName(), game.getReleaseYear(), game.getId());
	    return getGame(resultSet.getInt(1));
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    @Override
    public Game[] findGames() throws GameException {
	try {
	    List<Game> games = new ArrayList<Game>();
	    ResultSet resultSet = executeQuery("SELECT id, name, releaseYear FROM game");
	    while (resultSet.next()) {
		games.add(convertToGame(resultSet));
	    }
	    return games.toArray(new Game[games.size()]);
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    @Override
    public void deleteGame(int id) throws GameException {
	executePreparedUpdate("DELETE FROM game WHERE id=?", id);
    }

    protected ResultSet executePreparedUpdate(String query, Object... parameters) throws GameException {
	try {
	    PreparedStatement statement = this.connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
	    for (int i = 0; i < parameters.length; i++) {
		statement.setObject(i + 1, parameters[i]);
	    }
	    statement.executeUpdate();
	    return statement.getGeneratedKeys();
	} catch (Exception e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    protected ResultSet executePrepared(String query, Object... parameters) throws GameException {
	try {
	    PreparedStatement statement = this.connection.prepareStatement(query);
	    for (int i = 0; i < parameters.length; i++) {
		statement.setObject(i + 1, parameters[i]);
	    }
	    return statement.executeQuery();
	} catch (Exception e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    protected void executeUpdate(String query) throws GameException {
	try {
	    Statement statement = this.connection.createStatement();
	    statement.executeUpdate(query);
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }

    protected ResultSet executeQuery(String query) throws GameException {
	try {
	    Statement statement = this.connection.createStatement();
	    return statement.executeQuery(query);
	} catch (SQLException e) {
	    throw new GameException(Code.INTERNAL_ERROR, e);
	}
    }
}
