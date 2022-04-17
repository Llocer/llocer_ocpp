package com.llocer.ev.ocpp.server;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.llocer.common.Log;
import com.llocer.ev.ocpp.server.OcppMsg.CallError;
import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

class OcppMsgMapper{
	
	/*
	 * Instant
	 */
	
    private static DateTimeFormatter fmtSerializer = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'").withZone( ZoneId.of( "UTC" ) );

    public static class InstantSerializer extends JsonSerializer<Instant> {
		@Override
		public void serialize( Instant value, JsonGenerator gen, SerializerProvider serializers ) throws IOException {
	        String s = fmtSerializer.format(value);
	        gen.writeString( s );
		}
	}
	
	private static DateTimeFormatter fmtDeserializer = 
			(new DateTimeFormatterBuilder())
			.appendPattern("yyyy-MM-dd'T'HH:mm:ss")
			.appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
			.appendLiteral('Z')
			.toFormatter()
			.withZone( ZoneId.of( "UTC" ) );        

	public static class InstantDeserializer extends JsonDeserializer<Instant> {
		@Override
		public Instant deserialize( JsonParser jp, DeserializationContext ctxt )
				throws IOException, JsonProcessingException {
	        String s = jp.readValueAs( String.class );
	        Instant res = fmtDeserializer.parse( s, Instant::from );
	        return res;
		}
	}

	/*
	 * OcppMsp
	 */
	
	private static CallError makeException( String messageId, OcppErrorCode errorCode ) {
		CallError res = new CallError();
		res.messageId = messageId;
		res.errorCode = OcppErrorCode.Exception;
		res.exceptionErrorCode = errorCode;
		return res;
	}
	
	private static class OcppMsgSerializer extends JsonSerializer<OcppMsg> {
		@Override
		public void serialize( OcppMsg msg, JsonGenerator jgen, SerializerProvider serializers ) throws IOException {
	    	jgen.writeStartArray();
	    	if( msg instanceof OcppMsg.Call ) {
	    		OcppMsg.Call tmsg = (OcppMsg.Call)msg;
	        	jgen.writeNumber( 2 );
	           	jgen.writeString( tmsg.messageId );
	        	jgen.writeString( tmsg.action.toString() ); 
	    		if( tmsg.payload == null ) {
	    			jgen.writeStartObject();
	    			jgen.writeEndObject();
	    		} else {
	    			jgen.writeObject( tmsg.payload );
	    		}
	        	
	    	} else if( msg instanceof OcppMsg.CallResult ) {
	    		OcppMsg.CallResult tmsg = (OcppMsg.CallResult)msg;
	        	jgen.writeNumber( 3 );
	           	jgen.writeString( tmsg.messageId );
	    		if( tmsg.payload == null ) {
	    			jgen.writeStartObject();
	    			jgen.writeEndObject();
	    		} else {
	    			jgen.writeObject( tmsg.payload );
	    		}
	    		
	    	} else if( msg instanceof OcppMsg.CallError ) {
	    		OcppMsg.CallError tmsg = (OcppMsg.CallError)msg;
	        	jgen.writeNumber( 4 );
	           	jgen.writeString( tmsg.messageId );
	    		jgen.writeString( tmsg.errorCode.toString() ); 
	    		jgen.writeString( tmsg.errorDescription == null ? "" : tmsg.errorDescription );
	    		if( tmsg.errorDetails == null ) {
	    			jgen.writeStartObject(); 
	    			jgen.writeEndObject();

	    		} else {
	    			jgen.writeObject( tmsg.errorDetails );

	    		}
	    	} else {
	    		throw new IllegalArgumentException();
	    		
	    	}
	    		
			jgen.writeEndArray();
		}
	}
	
	private static class OcppMsgDeserializer extends JsonDeserializer<OcppMsg> {
		@Override
		public OcppMsg deserialize( JsonParser jp, DeserializationContext ctxt )
				throws IOException, JsonProcessingException {
			ObjectMapper mapper = (ObjectMapper) jp.getCodec();
			JsonNode node = mapper.readTree(jp);

			int messageTypeId = node.get(0).asInt();
			String messageId = node.get(1).asText();
			
			switch( messageTypeId ) {
			case 2: {
				OcppMsg.Call msg = new OcppMsg.Call();
				String action_s = node.get(2).asText();
				try {
					msg.action = OcppAction.valueOf( action_s );
					
				} catch( IllegalArgumentException e ) {
					return makeException( messageId, OcppErrorCode.PropertyConstraintViolation );
					
				}

				msg.messageId = messageId;
				try {
					msg.payload = msg.action.decodeRequest( node.get(3) );
				} catch (Exception e) {
					return makeException( messageId, OcppErrorCode.FormatViolation );
				}

				return msg;
			}
			
			case 3:{
				OcppMsg.CallResult msg = new OcppMsg.CallResult();
				msg.messageId = messageId;
				msg.payload = node.get(2); // Deserialize in WebSocketEndPoint
				return msg;
			}
				
			case 4: {
				OcppMsg.CallError msg = new OcppMsg.CallError();
				msg.messageId = messageId;
				
				String errorCode_s = node.get(3).asText();
				try {
					msg.errorCode = OcppErrorCode.valueOf( errorCode_s );
				} catch ( Exception e ) {
					Log.error( "Unknown error code = %s", errorCode_s );
					msg.errorCode = OcppErrorCode.GenericError;
				}
				
				msg.errorDescription = node.get(4).asText();
				msg.errorDetails = node.get(5);
				
				return msg;
			}
			
			default: { // invalid messageTypeId
				return makeException( messageId, OcppErrorCode.MessageTypeNotSupported );
			} }
		}
	}
	
	/*
	 * main
	 */
	
	private static ObjectMapper initMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// determines whether generator will automatically close underlying output target that is NOT owned by the generator.
		mapper.configure( JsonGenerator.Feature.AUTO_CLOSE_TARGET, false );

		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer( Instant.class, new InstantSerializer() ); 
		module.addDeserializer( Instant.class, new InstantDeserializer() ); 
		module.addSerializer( OcppMsg.class, new OcppMsgSerializer() ); 
		module.addDeserializer( OcppMsg.class, new OcppMsgDeserializer() ); 
		mapper.registerModule( module );

		return mapper;
	}
	
	static final ObjectMapper mapper = initMapper();
	static final ObjectWriter writer = mapper.writerFor(OcppMsg.class);
	static final ObjectReader reader = mapper.readerFor(OcppMsg.class);
}

