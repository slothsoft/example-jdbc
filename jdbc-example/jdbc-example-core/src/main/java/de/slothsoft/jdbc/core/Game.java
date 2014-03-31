package de.slothsoft.jdbc.core;

import java.util.Objects;

public class Game {

    private Integer id;
    private String name;
    private int releaseYear;

    public Game() {
	this(null, 0);
    }

    public Game(String name, int releaseYear) {
	this.name = name;
	this.releaseYear = releaseYear;
    }

    public Integer getId() {
	return this.id;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public String getName() {
	return this.name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public int getReleaseYear() {
	return this.releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
	this.releaseYear = releaseYear;
    }

    @Override
    public int hashCode() {
	return Objects.hash(this.id);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Game other = (Game) obj;
	if (!Objects.equals(this.id, other.id))
	    return false;
	return true;
    }

}
