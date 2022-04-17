package com.llocer.ev.ocpp.server;

public interface OcppAgent {

	String getId();

	void onOcppEndpointConnected( OcppEndpoint ocppEndpoint );

	Object onOcppCall( OcppAction action, Object payload ) throws Exception;

	void onOcppCallError( OcppCommand msg ) throws Exception;

	void onOcppCallResult( OcppCommand command ) throws Exception;
}
