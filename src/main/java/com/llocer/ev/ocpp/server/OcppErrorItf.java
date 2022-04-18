package com.llocer.ev.ocpp.server;

import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

public interface OcppErrorItf {
	OcppErrorCode getErrorCode();
	String getErrorDescription();
	default String getErrorDetails() { return null; }
}

