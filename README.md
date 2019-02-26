JDBC Example
============

I'm trying to always learn new stuff by writing about it over at http://slothsoft.de/

Since ZIP files are a bit unflexible and somewhat annoying to handle I want to put the source code for my tutorials
in this GIT repository.

This example shows how to create a simple connection to a database using JDBC, all the while displaying some architectural best practices. Some detailed walkthrough is located there: http://slothsoft.de/en/content/accessing-database-using-jdbc








# Gaugebar Example

- **Author:** [Stef Schulz](mailto:s.schulz@slothsoft.de)
- **Repository:** <https://github.com/slothsoft/example-gaugebar>
- **Open Issues:** <https://github.com/slothsoft/example-gaugebar/issues>

This example shows percent value inside a gaugebar like this:

![Screenshot](https://raw.githubusercontent.com/slothsoft/example-gaugebar/master/readme/100percent.png)

**Content of this file:**

- [Getting Started](#getting-started)
- [Versions](#versions)
- [Tutorial](#tutorial)
- [License](#license)


## Getting Started

### Prerequisites

You need at least **Java 11** or above to run the code. But a **Java 7** version is still in the tags, if you need it. 

### Using the Example

Checkout this project and run the [MainApplication](https://github.com/slothsoft/example-gaugebar/blob/master/src/main/java/de/slothsoft/gaugebar/MainApplication.java). The other two classes (`GaugeBar` and `GaugeBarSkin`) are the ones you want to copy if you like what you see.


##  Versions

| Version       |
| ------------- |
| [Java 11](https://github.com/slothsoft/example-gaugebar) |
| [Java 7](https://github.com/slothsoft/example-gaugebar/tree/java7) |


##  Tutorial

Yes, you can just copy the classes. But if you want to know why I implemented them the way I did, read the following paragraphs.

Because today I want to show how to create custom controls with JavaFX, while honoring separation of layout and logic. Additionally I'll show how to use programmatic skinning and a CSS file.

### The Control

The gauge bar is like the speedometer on a car - it shows a value on an arc from 0 to a max value.

### Basic Logic

It should not really matter if we want to create the logic or the layout first, I'll usually start with logic. So after setting up a JavaFX project (you are free to copy this reactor) we'll add a very simple gauge class looks like this:

```java
public class GaugeBar extends Control {

    int maxValue = 100;
    int value = this.maxValue;
    
    public void setMaxValue(int maxValue) {
        if (maxValue < this.value)
            throw new IllegalArgumentException("Max value must be bigger than value!");
        this.maxValue = maxValue;
    }

    // other getters and setters
}
```

Since we named this "the logic, it might a good idea to add some unit tests. Yes, even if the getter and setter are simple. Hell, **because** they are.

 The tests work as some kind of contract in this case. If we assume that value cannot be negative for the layout, we should throw an `IllegalArgumentException` and test it gets thrown. That way, if we one day decide to remove the exception we get test errors and hopefully get reminded that our layout cannot display negative values. The same goes double for other people working on our code.

### Making it Pretty

Now that we have our control logic, we will add our layout class. JavaFX uses `javafx.scene.control.Skin` for that, an interface with only three methods to be overriden:

```java
public class GaugeBarSkin implements Skin<Gaugebar> {
    private final GaugeBar gaugeBar;
    private Group rootNode;
    
    public GaugeBarSkin(GaugeBar gaugeBar) {
        this.gaugeBar = gaugeBar;
    }

    @Override
    public GaugeBar getSkinnable() {
        return this.gaugeBar;
    }
    
    @Override
    public Node getNode() {
        if (this.rootNode == null) {
            this.rootNode = new Group();
           redraw();
        }
        return this.rootNode;
    }
    
    protected void redraw() {
        List<node> rootChildren = new ArrayList<node>();
        rootChildren.add(createBackground());
        rootChildren.add(createGauge());
        rootChildren.add(createTicks());
        rootChildren.add(createGaugeBlend());
        rootChildren.add(createBorder());
        this.rootNode.getChildren().setAll(rootChildren);
    }
        
    // all the createXYZ() methods

    @Override
    public void dispose() {
        // nothing to do
    }
}
```

I might be especially stupid, but it took me a long time to figure out that you should always return the same node in getNode() and modify it according to your wishes. All the node children are observable lists, so modifying them will throw events and the GUI will react to them.

Speaking of which... how does the control interact with the skin? First it should set the skin somewhere, for simplicity I choose the constructor:

```java
    public GaugeBar() {
        setSkin(new GaugeBarSkin(this));
    }
```

After that, the gauge bar could inform the skin on each change personally. But that would be stupid and unnecessary. What if we added a new skin? Both skins would need to implement a common interface, that would have many, many methods, because each skin could react to different property changes. There is a much cleaner way, that is called the listener pattern.

Every property change on the Gaugebar should be honored by firing an event. Each skin can now add itself to the events it wants to react to. In practice it looks like this:

```java
public class GaugeBar extends Control {
    public static final EventType<Event> EVENT_TYPE_CHANGE_MAX_VALUE = new EventType<>(EventType.ROOT,
			"de.slothsoft.gaugebar.GaugeBar.EVENT_TYPE_CHANGE_MAX_VALUE");
    
    public void setMaxValue(int maxValue) {
        if (maxValue < this.value)
            throw new IllegalArgumentException("Max value must be bigger than value!");
        this.maxValue = maxValue;
        fireEvent(new Event(Integer.valueOf(maxValue), this, EVENT_TYPE_CHANGE_MAX_VALUE));
    }

    // ...
}
public class GaugeBarSkin implements Skin<GaugeBar> {

    public GaugeBarSkin(GaugeBar gaugeBar) {
        this.gaugeBar = gaugeBar;
        this.gaugeBar.addEventHandler(GaugeBar.EVENT_TYPE_CHANGE_VALUE, event -> redraw());
    }
}
```

This appoach decouples the skin from the control. That way we could add an entirely new skin (e.g. for a healthbar or a simple label showing "80%"), but we are also free to extend the control and still use the same skin.

### How to Implement the Drawing

I'm not going to copy the entire skin code, if you want it, you can download the entire project. I think if you are here it's probably because you want to create your very own control yourself, so my code won't help you anyway.

So how to go about drawing a complex figure? Well for starters it's somewhat like programming: You need to break up your control into smaller parts. Imagine the gauge bar above (if you can't, scroll up) - my first reacting was "I'll never be able to draw something like that". Now take a look at this dissection:

![Dissection](https://raw.githubusercontent.com/slothsoft/example-gaugebar/master/readme/dissection.png)

It's almost too easy, isn't it? The only real problem was to calculate the angle of the arc correctly.

### Adding CSS

To add support for styling via CSS files, we have to add CSS classes to specific nodes. For now, we will only make the green part stylable. First we add the CSS class "gauge" to the skin:


```java
private Node createGauge() {
    Stop[] stops = new Stop[] { new Stop(0, Color.LIGHTGREEN), new Stop(1, Color.DARKGREEN) };
    Circle circle = new Circle(this.size, this.size, this.size - 2 * GAUGE_BORDER);
    circle.setFill(new LinearGradient(1, 0, 0.5, 1, true, CycleMethod.NO_CYCLE, stops));
    circle.getStyleClass().add(\"gauge\"); // <- CSS class
    return circle;
}
```

Now we create a CSS file like this:

```css
.gauge {
    -fx-fill: #FF0000;
}
```

And make it known to the JavaFX scene:

```java
Scene scene = new Scene(group, 240, 120);
scene.getStylesheets().add("style.css"); // etc
```

When we start our application now, our green gauge will be red. You can test this out by uncommenting the line in `[MainApplication](https://github.com/slothsoft/example-gaugebar/blob/master/src/main/java/de/slothsoft/gaugebar/MainApplication.java)` (it's line 56 right now, but that might change).


### Summary

In the end we should have a control with nicely tested logic and one skin that could be customized via CSS. We know how to go about painting complex figures and we used the listener pattern. If there are still questions, I'd love to hear them. And if you want to see the entire project, that's why it's on GitHub in the first place.


## License

This project is licensed under the MIT License - see the [MIT license](https://opensource.org/licenses/MIT) for details.











('node', 'article', 0, 117, 117, 'und', 0, 'What I want to do today is to show how to create a simple connection to a database using JDBC, all the while displaying some architectural best practices. The source code is stored via GitHub (link at the end).\r\n\r\nThis tutorial works for many different types of databases, most importantly relational databases (these using SQL). I decided to use SQLite, because I need it for my current project and it\'s the smallest implementation for the use case. You are free to use whatever you feel like, due to the power of JDBC.\r\n\r\n<!--break-->\r\n\r\n<h2>What is JDBC</h2>\r\n\r\nLet\'s say you and your family where to describe the house you grew up in. Maybe your mom would describe the front garden she cared for in much more detail, your dad might tell the story of how hard it was to install the sound system, but you only remember the TV. Even the things all of you are describing, like the number of rooms, might vary in what words you use (maybe it\'s a living room for you, but your mom always called it a family room). All in all it\'s not easy to recognize all of you described the same building.\r\n\r\nThat\'s basically what is like using different databases. They focus on different things and use (at least slightly) different syntax. It\'s not easy swapping one for the other. But that\'s where JDBC comes into play.\r\n\r\nJDBC is like a form all your family members have to fill out regarding your house. \"Our house had ___ rooms. It\'s color was ___. It [had|did not have] a sound system in the living room.\" Now in this case all your descriptions would match. This is what JDBC does. It says which classes are located where and what they should do. All we do is use the packages <tt>java.sql</tt> and <tt>javax.sql</tt> and don\'t worry about the details.\r\n\r\n<h2>A Good Architecture</h2>\r\n\r\nSpeaking of houses: Each house must have a good architecture. Imagine having the toilet inside the kitchen! Some coding decisions are just as bad, so I\'m now talking a bit about how to structure source code well.\r\n\r\n<b>Classes</b> should have one use case. All the methods should circle around a specific topic. Try explaining what your class does: If you use a lot of \"Ands\" it probably does to much. Inside the class methods and fields can (and should be) tightly intervined, but the public methods should be sparse and well defined.\r\n\r\nThe same holds true for entire modules. That\'s why its always a good idea to separate GUI from persistence. And the way to do it is via a dedicated module for the \"contract\" between the two. This contract consists of an interface like that:\r\n\r\n<pre title=\"GameManager.java\" class=\"brush: java\">\r\npublic interface GameManager {\r\n\r\n    Game createGame(Game game) throws GameException;\r\n    Game getGame(int id) throws GameException;\r\n    Game updateGame(Game game) throws GameException;\r\n    Game[] findGames() throws GameException;\r\n    void deleteGame(int id) throws GameException;\r\n}\r\n</pre>\r\n<pre title=\"Game.java\" class=\"brush: java\">\r\npublic class Game {\r\n\r\n    private Integer id;\r\n    private String name;\r\n    private int releaseYear;\r\n\r\n	// getter and setter\r\n}\r\n</pre>\r\n\r\nThis basically tells lets the GUI say: \"I don\'t care how and where my data is stored, but I know I can save and find my games.\" And the persistence layer says: \"I don\'t care if my client is command line or with some fancy GUI, I\'ll concentrate on persisting the data I get the only way I know how to.\r\n\r\nSince this module is the base for both GUI and persistence (or client and server) I\'m naming it \"core\" in this example.\r\n\r\n<h2>Persistence Module</h2>\r\n\r\nLets create a second module and implement the interface. I\'ll name the new class <tt>GameManagerImpl</tt>.\r\n\r\nThe center class for a JDBC based persistence class is the <tt>java.sql.Connection</tt>. It is used for every action on the database. For now, we don\'t care where we get this connection, we\'ll just use it:\r\n\r\n<pre title=\"GameManagerImpl.java\" class=\"brush: java\">\r\npublic class GameManagerImpl implements GameManager {\r\n\r\n    private final Connection connection;\r\n\r\n    public GameManagerImpl(Connection connection) throws SQLException {\r\n		this.connection = connection;\r\n		initTable();\r\n    }\r\n	\r\n	private void initTable() {\r\n		Statement statement = this.connection.createStatement();\r\n		statement.executeQuery(\"CREATE TABLE IF NOT EXISTS game (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, releaseYear INT);\");\r\n	}\r\n	\r\n	// ...\r\n}\r\n</pre>\r\n\r\nOf course one of the first queries should be to initialize the SQL table we are going to store our games in. After that is done, queries are rather simple:\r\n\r\n<pre title=\"GameManagerImpl.java\" class=\"brush: java\">\r\n    @Override\r\n    public Game createGame(Game game) throws GameException {\r\n		try {	\r\n			PreparedStatement statement = this.connection.prepareStatement(\r\n				\"INSERT INTO game (name, releaseYear) VALUES(?, ?)\");\r\n			statement.setObject(1, game.getName());\r\n			statement.setObject(2, game.getReleaseYear());\r\n			return getGame(statement.executeQuery().getInt(1));\r\n		} catch (SQLException e) {\r\n			throw new GameException(Code.INTERNAL_ERROR, e);\r\n		}\r\n    }\r\n	\r\n    @Override\r\n    public Game getGame(int id) throws GameException {\r\n		try {\r\n			PreparedStatement statement = this.connection.prepareStatement(\r\n				\"SELECT id, name, releaseYear FROM game where id = ?\");\r\n			statement.setObject(1, id);\r\n			ResultSet resultSet = statement.executeQuery();		\r\n			if (!resultSet.next())\r\n				throw new GameException(Code.NO_GAME_FOUND);\r\n			return convertToGame(resultSet);\r\n		} catch (SQLException e) {\r\n			throw new GameException(Code.INTERNAL_ERROR, e);\r\n		}\r\n    }\r\n\r\n    private Game convertToGame(ResultSet resultSet) throws SQLException {\r\n		Game game = new Game();\r\n		game.setId(resultSet.getInt(1));\r\n		game.setName(resultSet.getString(2));\r\n		game.setReleaseYear(resultSet.getInt(3));\r\n		return game;\r\n    }\r\n	\r\n</pre>\r\n\r\nSo what are these prepared statements? Lets say we have a query like this:\r\n\r\n<pre class=\"brush: java\">\r\nString query = \"SELECT * FROM game WHERE userId = \" + userId + \" AND name = \'\" + userInput + \"\';\"; \r\n</pre>\r\n\r\nWhat would happen if the user where to enter any of these?\r\n<pre class=\"brush: java\">\r\nString showAll = \"\' OR true; --\";\r\nString dropDatabase = \"\'; DROP DATABASE; --\";\r\n</pre> \r\n\r\nIn the first case the user would see all games, even though clearly he should only see his own games. The second query would even drop the entire database! This practice is called \"SQL injection\" and should be averted at all costs.\r\n\r\nPrepared queries are the way to go. These queries know where parameters are located and escapes them accordingly.\r\n\r\nSo now that you know how to fire queries the right way, take a look at <a href=\"https://github.com/slothsoft/jdbc-example/blob/master/jdbc-example/jdbc-example-impl/src/main/java/de/slothsoft/jdbc/impl/GameManagerImpl.java\">the finished implementation class</a>. I tried to make the source code as readable as possible.\r\n\r\nHow do we know this implementation does what it says it does? We\'ll create a JUnit test for that. \r\n\r\n<h2>Testing the Persistence Module</h2>\r\n\r\nUp until now we didn\'t decide for a database, but for the JUnit test we have to. The best choice is always the database that is going to be used by the consumer of the persistence module. And if there are more than one of these, then we should set up a testing suite for each and every possible database driver. \r\n\r\nAs I said, I\'ll use SQLite for this one, which is a simple declaration inside the <i>pom.xml</i>:\r\n<pre class=\"brush: xml\">\r\n&lt;dependency&gt;\r\n	&lt;groupId>org.xerial&lt;/groupId&gt;\r\n	&lt;artifactId>sqlite-jdbc&lt;/artifactId&gt;\r\n	&lt;version>3.7.2&lt;/version&gt;\r\n	&lt;scope>test&lt;/scope&gt;\r\n&lt;/dependency&gt;\r\n</pre> \r\n\r\nAfter that, the actual testing class is a piece of cake:\r\n\r\n<pre title=\"GameManagerImplTest.java\" class=\"brush: java\">\r\npublic class GameManagerImplTest {\r\n\r\n    private GameManager gameManager;\r\n\r\n    @Before\r\n    public void setUp() throws GameException, ClassNotFoundException, SQLException {\r\n		// with that we load the JDBC driver\r\n		Class.forName(\"org.sqlite.JDBC\");\r\n		// this creates a new GameManager with a database in memory\r\n		this.gameManager = new GameManagerImpl(DriverManager.getConnection(\"jdbc:sqlite::memory:\"));\r\n    }\r\n\r\n    @Test\r\n    public void testCreate() throws GameException {\r\n		Game createdGame = this.gameManager.createGame(new Game(\"New Game\", 2000));\r\n\r\n		Assert.assertNotNull(createdGame);\r\n		Assert.assertNotNull(createdGame.getId());\r\n		Assert.assertEquals(\"New Game\", createdGame.getName());\r\n		Assert.assertEquals(2000, createdGame.getReleaseYear());\r\n    }\r\n}\r\n</pre>\r\n\r\nSo now that we know our persistence module works, we can go build a client!\r\n\r\n<h2>The Not so Fancy GUI</h2>\r\n\r\nThis is going to be the third module and in an ideal world it would only have dependencies to the core. You can achieve this via dependency injection or OSGI, but for this simple project we accept the dependency towards the implementation and just keep it centered in one class.\r\n\r\nI namend the class unoriginally <tt>Database</tt> - it does basicly the same as the JUnit test.\r\n\r\n<pre title=\"Database.java\" class=\"brush: java\">\r\npublic class Database {\r\n\r\n    private static final String DATABASE_DRIVER = \"org.sqlite.JDBC\";\r\n    private static final String DATABASE_URL = \"jdbc:sqlite:sample.db\";\r\n\r\n    private final Connection connection;\r\n    private GameManager gameManager;\r\n\r\n    private Database() {\r\n		try {\r\n			Class.forName(DATABASE_DRIVER);\r\n			this.connection = DriverManager.getConnection(DATABASE_URL);\r\n		} catch (ClassNotFoundException e) {\r\n			throw new RuntimeException(\"Could not find JDBC driver!\", e);\r\n		} catch (SQLException e) {\r\n			throw new RuntimeException(\"Could not open connection to database!\", e);\r\n		}\r\n    }\r\n\r\n    public synchronized GameManager getGameManager() throws GameException {\r\n		if (this.gameManager == null) {\r\n			this.gameManager = new GameManagerImpl(this.connection);\r\n		}\r\n		return this.gameManager;\r\n    }\r\n}\r\n</pre>\r\n\r\nSo now its time for the actual client. We will settle for this small class:\r\n\r\n<pre title=\"Database.java\" class=\"brush: java\">\r\npublic class Client {\r\n\r\n    public static void main(String[] args) throws GameException, ClassNotFoundException {\r\n		GameManager gameManager = Database.getInstance().getGameManager();\r\n\r\n		Game ednaAndHarvey = new Game();\r\n		ednaAndHarvey.setName(\"Edna & Harvey: The Breakout\");\r\n		ednaAndHarvey.setReleaseYear(2008);\r\n\r\n		ednaAndHarvey = gameManager.createGame(ednaAndHarvey);\r\n		System.out.println(\"Created \" + ednaAndHarvey.getName() + \" with ID: \" + ednaAndHarvey.getId() + \"\\n\");\r\n\r\n		Database.getInstance().destroy();\r\n    }\r\n}\r\n</pre>\r\n\r\nNote that the client should never ever use <tt>GameManagerImpl</tt>, but always the interface. This means you can always swap the implementation with little effort. \r\n\r\n\r\n<h2>Finishing Touches</h2>\r\n\r\nSo what is this structure good for? Let\'s see how it handles changes.\r\n\r\nIf you want to change the database: Just change the Maven dependency in the client and the constant <tt>Database.DATABASE_DRIVER</tt> and <tt>Database.DATABASE_URL</tt>. That\'s it. \r\n\r\nIf you want to change the entire implementation, lets say to a document database or an object oriented one: just put a second implementation module in your project and change the one class these classes get instantiated.\r\n\r\nAnd if you want to change the client? Well just do it. Noone is telling you to use a command line interface...\r\n\r\nSo in the end you should be able to structure a project with a database well, while using JDBC wrapping the actual implementation. If you want to see the entire project, I put it on <a href=\"https://github.com/slothsoft/jdbc-example\">GitHub</a>. If there are any questions, I\'d love to hear them.', '', 'full_html'),
