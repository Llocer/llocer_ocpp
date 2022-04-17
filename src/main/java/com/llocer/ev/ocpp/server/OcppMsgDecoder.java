package com.llocer.ev.ocpp.server;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.llocer.common.Log;


public class OcppMsgDecoder implements Decoder.Text<OcppMsg> {

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public OcppMsg decode(String txt) throws DecodeException {
		try {
			return OcppMsgMapper.reader.readValue(txt);

		} catch (IOException e) {
			throw new DecodeException( txt, "jackson exception", e);
			
		}
	}

	@Override
	public boolean willDecode( String txt ) {
		try {
	    	Log.debug( "\n\n*** OcppMsgDecoder %s", txt );
			OcppMsgMapper.reader.readValue( txt );
			return true;
			
		} catch (IOException e) {
			Log.debug(e, "OcppMsgDecoder.willDecode");
			return false;
			
		}
	}


}
