package com.llocer.ev.ocpp.server;

public class OcppMsg {
	
	public String messageId; 
	
	/*
	 * static 
	 */
	
	public enum OcppErrorCode {
		// syntax:
		FormatViolation,
		ProtocolError,
		RpcFrameworkError,
		
		// semantics:
		TypeConstraintViolation,
		OccurrenceConstraintViolation,
		MessageTypeNotSupported, // error in messageTypeId
		PropertyConstraintViolation,
		
		// execution:
		NotImplemented, //	Requested Action is not known by receiver
		NotSupported, // Requested Action is recognized but not supported by the receiver
		SecurityError,
		InternalError,
		GenericError,
		
		// custom (must not be sent):
		Exception
	}
	
	public static class Call extends OcppMsg {
		OcppAction action = null; 
		Object payload = null;
	}
	
	public static class CallResult extends OcppMsg {
		Object payload;
	}
	
	public static class CallError extends OcppMsg {
		OcppErrorCode errorCode = null; 
		String errorDescription = null; 
		Object errorDetails = null;
		OcppErrorCode exceptionErrorCode = null;
	}
}
