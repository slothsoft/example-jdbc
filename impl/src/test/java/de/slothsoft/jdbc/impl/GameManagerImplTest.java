package de.slothsoft.jdbc.impl;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.slothsoft.jdbc.core.Game;
import de.slothsoft.jdbc.core.GameException;
import de.slothsoft.jdbc.core.GameManager;
import de.slothsoft.jdbc.impl.GameManagerImpl;

public class GameManagerImplTest {

    private GameManager gameManager;

    @Before
    public void setUp() throws GameException, ClassNotFoundException, SQLException {
	Class.forName("org.sqlite.JDBC");
	this.gameManager = new GameManagerImpl(DriverManager.getConnection("jdbc:sqlite::memory:"));
    }

    @Test
    public void testCreate() throws GameException {
	Game createdGame = this.gameManager.createGame(new Game("New Game", 2000));

	Assert.assertNotNull(createdGame);
	Assert.assertNotNull(createdGame.getId());
	Assert.assertEquals("New Game", createdGame.getName());
	Assert.assertEquals(2000, createdGame.getReleaseYear());
    }

    @Test
    public void testGet() throws GameException {
	Game game = this.gameManager.createGame(new Game("Gotten Game", 2000));
	Game gottenGame = this.gameManager.getGame(game.getId());

	Assert.assertNotNull(gottenGame);
	Assert.assertEquals(game.getId(), gottenGame.getId());
	Assert.assertEquals("Gotten Game", gottenGame.getName());
	Assert.assertEquals(2000, gottenGame.getReleaseYear());
    }

    @Test
    public void testUpdate() throws GameException {
	Game createdGame = this.gameManager.createGame(new Game("New Game", 2000));
	createdGame.setName("Updated Game");
	createdGame.setReleaseYear(2014);
	Game updatedGame = this.gameManager.updateGame(createdGame);

	Assert.assertNotNull(updatedGame);
	Assert.assertEquals(createdGame.getId(), updatedGame.getId());
	Assert.assertEquals("Updated Game", updatedGame.getName());
	Assert.assertEquals(2014, updatedGame.getReleaseYear());
    }

    @Test
    public void testFind() throws GameException {
	this.gameManager.createGame(new Game("Game 1", 2001));
	this.gameManager.createGame(new Game("Game 2", 2002));
	this.gameManager.createGame(new Game("Game 3", 2003));

	Game[] foundGames = this.gameManager.findGames();

	Assert.assertNotNull(foundGames);

	Assert.assertNotNull(foundGames[0]);
	Assert.assertNotNull(foundGames[0].getId());
	Assert.assertEquals("Game 1", foundGames[0].getName());
	Assert.assertEquals(2001, foundGames[0].getReleaseYear());

	Assert.assertNotNull(foundGames[1]);
	Assert.assertNotNull(foundGames[1].getId());
	Assert.assertEquals("Game 2", foundGames[1].getName());
	Assert.assertEquals(2002, foundGames[1].getReleaseYear());

	Assert.assertNotNull(foundGames[2]);
	Assert.assertNotNull(foundGames[2].getId());
	Assert.assertEquals("Game 3", foundGames[2].getName());
	Assert.assertEquals(2003, foundGames[2].getReleaseYear());
    }

    @Test
    public void testDelete() throws GameException {
	Game game = this.gameManager.createGame(new Game("Game 1", 2001));

	Assert.assertEquals(1, this.gameManager.findGames().length);

	this.gameManager.deleteGame(game.getId());

	Assert.assertEquals(0, this.gameManager.findGames().length);
    }
}
