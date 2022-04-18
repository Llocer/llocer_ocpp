package com.llocer.ev.ocpp.server;

import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

public class OcppError implements OcppErrorItf {
	private final OcppErrorCode errorCode;
	private final String errorDescription;
	private final String errorDetails;
	
	public OcppError( OcppErrorCode errorCode, String errorDescription, String errorDetails ) {
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
		this.errorDetails = errorDetails;
	}

	public OcppError( OcppErrorCode errorCode, String errorDescription ) {
		this( errorCode, errorDescription, null );
	}
	
	@Override
	public OcppErrorCode getErrorCode() { return errorCode; }
	
	@Override
	public String getErrorDescription() { return errorDescription; }
	
	@Override
	public String getErrorDetails() { return errorDetails; } 
}

