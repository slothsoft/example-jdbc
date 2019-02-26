package de.slothsoft.example.jdbc.client;

import de.slothsoft.example.jdbc.core.Game;
import de.slothsoft.example.jdbc.core.GameException;
import de.slothsoft.example.jdbc.core.GameManager;

/**
 * This class is our "client". It's consuming our {@link GameManager} without ever using
 * an implementation.
 *
 * @author Stef Schulz
 * @since 1.0.0
 */

public class Client {

	public static void main(String[] args) throws GameException {
		final GameManager gameManager = Database.getInstance().getGameManager();

		Game ednaAndHarvey = new Game();
		ednaAndHarvey.setName("Edna & Harvey: The Breakout");
		ednaAndHarvey.setReleaseYear(2008);

		ednaAndHarvey = gameManager.createGame(ednaAndHarvey);
		System.out.println("Created " + ednaAndHarvey.getName() + " with ID: " + ednaAndHarvey.getId() + "\n");

		Game fallout = new Game();
		fallout.setName("Fallout");
		fallout.setReleaseYear(1997);

		fallout = gameManager.createGame(fallout);
		System.out.println("Created " + fallout.getName() + " with ID: " + fallout.getId() + "\n");

		fallout.setName("Fallout: New Vegas");
		fallout.setReleaseYear(2010);

		fallout = gameManager.updateGame(fallout);
		System.out.println("Updating to " + fallout.getName() + " with ID: " + fallout.getId() + "\n");

		System.out.println("Searching for games:");
		for (final Game game : gameManager.findGames()) {
			System.out.println("\tFound " + game.getName());
		}
		System.out.println();

		System.out.println("Deleting games...\n");
		gameManager.deleteGame(ednaAndHarvey.getId());
		gameManager.deleteGame(fallout.getId());

		System.out.println("Searching for games:");
		for (final Game game : gameManager.findGames()) {
			System.out.println("\tFound " + game.getName());
		}
		System.out.println();

		System.out.println("Finished demo client!");

		Database.getInstance().destroy();
	}

}
