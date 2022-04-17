package com.llocer.ev.ocpp.server;

import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

enum OcppErrorEnum implements OcppError{
	Timeout( OcppErrorCode.GenericError,  "Timeout" ),
	PropertyConstraintViolation( OcppErrorCode.PropertyConstraintViolation, "Property constraint violation" ),
	Internal( OcppErrorCode.InternalError, "Internal error" );
	
	private final OcppErrorCode errorCode;
	private final String errorDescription;
	
	OcppErrorEnum( OcppErrorCode errorCode, String errorDescription ) {
		this.errorCode = errorCode;
		this.errorDescription = errorDescription;
	}
	
	@Override
	public OcppErrorCode getErrorCode() {
		return errorCode;
	}
	
	@Override
	public String getErrorDescription() {
		return errorDescription;
	}
	
}

