package de.slothsoft.jdbc.core;

public class GameException extends Exception {

    private static final long serialVersionUID = 6810117440789300724L;

    private Code errorCode;

    public GameException() {
	super();
	this.errorCode = Code.UNKNOWN;
    }

    public GameException(Code errorCode, Throwable cause) {
	super(errorCode.name(), cause);
	this.errorCode = errorCode;
    }

    public GameException(Code errorCode) {
	super(errorCode.name());
	this.errorCode = errorCode;
    }

    public GameException(Throwable cause) {
	super(cause);
	this.errorCode = Code.UNKNOWN;
    }

    public Code getErrorCode() {
	return this.errorCode;
    }

    public static enum Code {
	UNKNOWN, INITIALIZATION_ERROR, INTERNAL_ERROR, NO_GAME_FOUND;
    }
}
