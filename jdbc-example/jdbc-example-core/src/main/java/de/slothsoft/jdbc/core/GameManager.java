package de.slothsoft.jdbc.core;

public interface GameManager {

    Game createGame(Game game) throws GameException;

    Game getGame(int id) throws GameException;

    Game updateGame(Game game) throws GameException;

    Game[] findGames() throws GameException;

    void deleteGame(int id) throws GameException;
}
