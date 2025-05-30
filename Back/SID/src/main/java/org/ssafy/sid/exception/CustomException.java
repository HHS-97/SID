package org.ssafy.sid.exception;

public class CustomException extends RuntimeException {
	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return errorCode.getDetail();
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
