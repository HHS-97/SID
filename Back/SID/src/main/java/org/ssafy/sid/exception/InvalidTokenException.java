package org.ssafy.sid.exception;

public class InvalidTokenException extends RuntimeException{
	public InvalidTokenException(String message){
		super(message);
	}
}
