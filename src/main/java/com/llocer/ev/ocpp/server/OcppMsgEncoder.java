package com.llocer.ev.ocpp.server;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.llocer.common.Log;

public class OcppMsgEncoder implements Encoder.Text<OcppMsg> {

	@Override
	public String encode( OcppMsg msg ) throws EncodeException {
		try {
		      String txt = OcppMsgMapper.writer.writeValueAsString(msg);
              Log.debug("MessageEncoder: %s", txt );
              return txt;
		} catch (JsonProcessingException e) {
			throw new EncodeException( msg, "MessageEncoder: JsonProcessingException", e );
		}
	}

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}
}
