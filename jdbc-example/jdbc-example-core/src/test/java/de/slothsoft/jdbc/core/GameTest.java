package de.slothsoft.jdbc.core;

import org.junit.Assert;
import org.junit.Test;

import de.slothsoft.jdbc.core.Game;

public class GameTest {

    @Test
    public void testEquals() {
	Game game1 = new Game("Game", 2014);
	game1.setId(1);
	Game game2 = new Game("Game", 2014);
	game2.setId(1);

	Assert.assertEquals(game1, game2);
	Assert.assertEquals(game1.hashCode(), game2.hashCode());
    }

    @Test
    public void testEqualsWithNullId() {
	Game game1 = new Game("Game", 2014);
	Game game2 = new Game("Game", 2014);

	Assert.assertEquals(game1, game2);
	Assert.assertEquals(game1.hashCode(), game2.hashCode());
    }

    @Test
    public void testNotEqualsById() {
	Game game1 = new Game("Game", 2014);
	game1.setId(1);
	Game game2 = new Game("Game", 2014);
	game2.setId(2);

	Assert.assertFalse(game1.equals(game2));
	Assert.assertFalse(game1.hashCode() == game2.hashCode());
    }

}
