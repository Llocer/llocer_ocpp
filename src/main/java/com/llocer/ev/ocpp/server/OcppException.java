package com.llocer.ev.ocpp.server;

import com.llocer.ev.ocpp.server.OcppMsg.CallError;
import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

public class OcppException extends Exception {
	private static final long serialVersionUID = -2059109141555240000L;
	public final OcppErrorCode errorCode; 
	public final String errorDescription; 
	public final Object errorDetails;

	public OcppException( OcppErrorCode errorCode, String errorDescription, Object errorDetails ) {
		super( errorDescription );
		this.errorCode = errorCode; 
		this.errorDescription = errorDescription; 
		this.errorDetails = errorDetails;
	}
	
	public OcppException( OcppError error ) {
		this( error.getErrorCode(), error.getErrorDescription(), null );
	}
	
	CallError toCallError( String messageId ) {
		CallError res = new CallError();
	    res.messageId = messageId;
	    res.errorCode = errorCode;
	    res.errorDescription = errorDescription;
	    res.errorDetails = errorDetails;
	    return res;
	}
}

