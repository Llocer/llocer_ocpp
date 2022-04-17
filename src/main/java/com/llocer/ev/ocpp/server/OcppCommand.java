package com.llocer.ev.ocpp.server;

import java.util.function.Consumer;

public class OcppCommand {
	public final OcppMsg.Call request; 
	public final Consumer<OcppCommand> callback;
	public Object answer = null; 
	public OcppMsg.CallError error = null; // null if success
	
	OcppCommand( OcppMsg.Call request, Consumer<OcppCommand> callback ) {
		this.request = request;
		this.callback = callback;
	}
}
