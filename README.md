# JDBC Example

- **Author:** [Stef Schulz](mailto:s.schulz@slothsoft.de)
- **Repository:** <https://github.com/slothsoft/example-jdbc>
- **Open Issues:** <https://github.com/slothsoft/example-jdbc/issues>

This project shows how to create a simple connection to a database using JDBC, all the while displaying some architectural best practices. 

**Content of this file:**

- [Getting Started](#getting-started)
- [Versions](#versions)
- [Tutorial](#tutorial)
- [License](#license)


## Getting Started

### Prerequisites

This example is compiled with **Java 11**, but you can probably change that in the *pom.xml* without losing functionality. There is still a **Java 7** version in the branches, if you need it. 


### Using the Example


Checkout this project and run the [Client](https://github.com/slothsoft/example-jdbc/blob/master/client/src/main/java/de/slothsoft/example/jdbc/client/Client.java). You can then move along the other classes to see how everything works or read the tutorial below.


##  Versions

| Version       |
| ------------- |
| [Java 11](https://github.com/slothsoft/example-jdbc) |
| [Java 7](https://github.com/slothsoft/example-jdbc/tree/java7) |


##  Tutorial

What I want to do today is to show how to create a simple connection to a database using JDBC, all the while displaying some architectural best practices. The example code will create, read, update and delete games into our own database.

This tutorial works for many different types of databases, most importantly relational databases (the ones using SQL). I decided to use SQLite, because I need it for my current project and it's the smallest implementation for the use case. You are free to use whatever you feel like, due to the power of JDBC.

### What is JDBC

Let's say you and your family where to describe the house you grew up in. Maybe your mom would describe the front garden she cared for in much more detail, your dad might tell the story of how hard it was to install the sound system, but you only remember the TV. Even the things all of you are describing, like the number of rooms, might vary in what words you use (maybe it's a living room for you, but your mom always called it a family room). All in all it's not easy to recognize all of you described the same building.

That's basically what is like using different databases. They focus on different things and use (at least slightly) different syntax. It's not easy swapping one for the other. But that's where JDBC comes into play.

JDBC is like a form all your family members have to fill out regarding your house. "Our house had ___ rooms. It's color was ___. It [had|did not have] a sound system in the living room." Now in this case all your descriptions would match. This is what JDBC does. It says which classes are located where and what they should do. All we do is use the packages `java.sql` and `javax.sql` and don't worry about the details.

### A Good Architecture

Speaking of houses: Each house must have a good architecture. Imagine having the toilet inside the kitchen! Some coding decisions are just as bad, so I'm now talking a bit about how to structure source code well.

**Classes** should have one use case. All the methods should circle around a specific topic. Try explaining what your class does: If you use a lot of "Ands" it probably does to much. Inside the class methods and fields can (and should be) tightly intertwined, but the public methods should be sparse and well defined.

The same holds true for entire modules. That's why its always a good idea to separate GUI from persistence. And the way to do it is via a dedicated module for the "contract" between the two. This contract consists of an interface like that:

```java
public interface GameManager {

    Game createGame(Game game) throws GameException;
    Game getGame(int id) throws GameException;
    Game updateGame(Game game) throws GameException;
    Game[] findGames() throws GameException;
    void deleteGame(int id) throws GameException;
}
```

```java
public class Game {

    private Integer id;
    private String name;
    private int releaseYear;

	// getter and setter
}
```

This basically lets the GUI say: "I don't care how and where my data is stored, but I know I can save and find my games." And the persistence layer says: "I don't care if my client is command line or with some fancy GUI, I'll concentrate on persisting the data I get the only way I know how to.

Since this module is the base for both GUI and persistence (or client and server) I'm naming it "core" in this example.

### Persistence Module

Lets create a second module and implement the interface. I'll name the new class `GameManagerImpl`.

The center class for a JDBC based persistence class is the `java.sql.Connection`. It is used for every action on the database. For now, we don't care where we get this connection, we'll just use it:

```java
public class GameManagerImpl implements GameManager {


	public GameManagerImpl(Connection connection) throws GameException {
		try {
			this.connection = connection;
			executeUpdate("CREATE TABLE IF NOT EXISTS game (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, releaseYear INT);");
		} catch (final Exception e) {
			throw new GameException(Code.INITIALIZATION_ERROR, e);
		}
	}
	
	protected void executeUpdate(String query) throws GameException {
		try {
			final Statement statement = this.connection.createStatement();
			statement.executeUpdate(query);
		} catch (final SQLException e) {
			throw new GameException(Code.INTERNAL_ERROR, e);
		}
	}

	// ...
}
```

Of course one of the first queries should be to initialize the SQL table we are going to store our games in. After that is done, queries are rather simple:

```java
	@Override
	public Game createGame(Game game) throws GameException {
		try {
			final ResultSet resultSet = executePreparedUpdate("INSERT INTO game (name, releaseYear) VALUES(?, ?)",
					game.getName(), game.getReleaseYear());
			return getGame(resultSet.getInt(1));
		} catch (final SQLException e) {
			throw new GameException(Code.INTERNAL_ERROR, e);
		}
	}

	@Override
	public Game getGame(int id) throws GameException {
		try {
			final ResultSet resultSet = executePrepared("SELECT id, name, releaseYear FROM game where id=?", id);
			if (!resultSet.next()) throw new GameException(Code.NO_GAME_FOUND);
			return convertToGame(resultSet);
		} catch (final SQLException e) {
			throw new GameException(Code.INTERNAL_ERROR, e);
		}
	}

	private Game convertToGame(ResultSet resultSet) throws SQLException {
		final Game game = new Game();
		game.setId(resultSet.getInt(1));
		game.setName(resultSet.getString(2));
		game.setReleaseYear(resultSet.getInt(3));
		return game;
	}

```

So what are these prepared statements? Lets say we have a query like this:

```java
String query = "SELECT * FROM game WHERE userId = " + userId + " AND name = '" + userInput + "';"; 
```

What would happen if the user were to enter any of these?

```java
String showAll = "' OR true; --";
String dropDatabase = "'; DROP DATABASE; --";
```

In the first case the user would see all games, even though clearly he should only see his own games. The second query would even drop the entire database! This practice is called "SQL injection" and should be averted at all costs.

Prepared queries are the way to go. These queries know where parameters are located and escapes them accordingly.

So now that you know how to fire queries the right way, take a look at 
[the finished implementation class](https://github.com/slothsoft/example-jdbc/blob/master/impl/src/main/java/de/slothsoft/example/jdbc/impl/GameManagerImpl.java). I tried to make the source code as readable as possible.

How do we know this implementation does what it says it does? We'll create a JUnit test for that. 

### Testing the Persistence Module

Up until now we didn't decide for a database, but for the JUnit test we have to. The best choice is always the database that is going to be used by the consumer of the persistence module. And if there are more than one of these, then we should set up a testing suite for each and every possible database driver. 

As I said, I'll use SQLite for this one, which is a simple declaration inside the *pom.xml*:

```xml
&lt;dependency&gt;
	&lt;groupId>org.xerial&lt;/groupId&gt;
	&lt;artifactId>sqlite-jdbc&lt;/artifactId&gt;
	&lt;version>3.23.1&lt;/version&gt;
	&lt;scope>test&lt;/scope&gt;
&lt;/dependency&gt;
```

After that, the actual testing class is a piece of cake:

```xml
public class GameManagerImplTest {

	private GameManager gameManager;

	@Before
	public void setUp() throws GameException, ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		this.gameManager = new GameManagerImpl(DriverManager.getConnection("jdbc:sqlite::memory:"));
	}

	@Test
	public void testCreate() throws GameException {
		final Game createdGame = this.gameManager.createGame(new Game("New Game", 2000));

		Assert.assertNotNull(createdGame);
		Assert.assertNotNull(createdGame.getId());
		Assert.assertEquals("New Game", createdGame.getName());
		Assert.assertEquals(2000, createdGame.getReleaseYear());
	}
}
```

So now that we know our persistence module works, we can go build a client!

### The Not so Fancy GUI

This is going to be the third module and in an ideal world it would only have dependencies to the core. You can achieve this via dependency injection or OSGI, but for this simple project we accept the dependency towards the implementation and just keep it centered in one class.

I named the class unoriginally `Database` - it does basically the same as the JUnit test.

```java
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
}
```

So now its time for the actual client. We will settle for this small class:

```java
public class Client {

	public static void main(String[] args) throws GameException {
		final GameManager gameManager = Database.getInstance().getGameManager();

		Game ednaAndHarvey = new Game();
		ednaAndHarvey.setName("Edna & Harvey: The Breakout");
		ednaAndHarvey.setReleaseYear(2008);

		ednaAndHarvey = gameManager.createGame(ednaAndHarvey);
		System.out.println("Created " + ednaAndHarvey.getName() + " with ID: " + ednaAndHarvey.getId() + "\n");

		System.out.println("Searching for games:");
		for (final Game game : gameManager.findGames()) {
			System.out.println("\tFound " + game.getName());
		}
		System.out.println();

		System.out.println("Finished demo client!");

		Database.getInstance().destroy();
	}

}
```

Note that the client should never ever use `GameManagerImpl`, but always the interface. This means you can always swap the implementation with little effort. 


### Finishing Touches

So what is this structure good for? Let's see how it handles changes.

If you want to change the database: Just change the Maven dependency in the client and the constant `Database.DATABASE_DRIVER` and `Database.DATABASE_URL`. That's it. 

If you want to change the entire implementation, lets say to a document database or an object oriented one: just put a second implementation module in your project and change the one class these classes get instantiated in.

And if you want to change the client? Well just do it. Noone is telling you to use a command line interface...

So in the end you should be able to structure a project with a database well, while using JDBC wrapping the actual implementation. If there are any questions, I'd love to hear them.


## License

This project is licensed under the MIT License - see the [MIT license](https://opensource.org/licenses/MIT) for details.

