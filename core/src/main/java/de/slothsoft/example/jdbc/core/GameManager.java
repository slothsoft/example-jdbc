package de.slothsoft.example.jdbc.core;

/**
 * The API interface clients should use, so they don't depend on the implementation.
 *
 * @author Stef Schulz
 * @since 1.0.0
 */

public interface GameManager {

	Game createGame(Game game) throws GameException;

	Game getGame(int id) throws GameException;

	Game updateGame(Game game) throws GameException;

	Game[] findGames() throws GameException;

	void deleteGame(int id) throws GameException;
}
