package com.llocer.ev.ocpp.server;

import java.io.EOFException;
import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.llocer.common.Log;
import com.llocer.ev.ocpp.server.OcppMsg.CallError;
import com.llocer.ev.ocpp.server.OcppMsg.CallResult;
import com.llocer.ev.ocpp.server.OcppMsg.OcppErrorCode;

@ServerEndpoint(
	value="/cso/ocpp/{csId}",
	encoders = { OcppMsgEncoder.class },
	decoders = { OcppMsgDecoder.class },
	subprotocols = { "ocpp2.0" }
)
public class OcppEndpoint {

	/*
	 * static 
	 */
	private static String configFile = "/opt/llocer/ev/etc/ocpp.conf";


	private static class Controller implements Runnable {
		private OcppEndpoint wsep;

		public Controller( OcppEndpoint wsep ) {
			this.wsep = wsep;
		}

		@Override
		public void run() {
//			Log.debug( "OcppEndpoint.Controller running." );
			
			try {
				if( wsep.waitingMsg != null && wsep.waitingTimestamp + config.maxMessageWaitingInterval < System.currentTimeMillis() ) {
					// expired message
					OcppMsg error = makeCallError( wsep.waitingMsg.request.messageId, OcppErrorEnum.Timeout );
					wsep.onMessage( error, wsep.session );
				}
				
				if( wsep.receivedTimestamp + 2000L*config.heartbeatInterval < System.currentTimeMillis() ) {
					// no heartbeat nor other message received
					this.wsep.session.close();
				}

			} catch (Exception e) {
				Log.error( e );
				
			}
		}
	};
	
	public static class Config {
		public int maxMessageWaitingInterval = 60000; // millis
		public int heartbeatInterval = 300; // seconds
		public long wsControllerInterval = 60L; // seconds
	};

	private static Config initConfig() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor( Config.class );
		
		try {
			return reader.readValue( new File( configFile ));
		} catch (Exception e) {
			Log.warning( "unable to read config file "+ configFile );
			return new Config();
		}
	}
	
	public static final Config config = initConfig();
	private static final Map<String,OcppAgent> agents = new HashMap<String,OcppAgent>();
	private static final ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1);
	
	
	/*
	 * fields 
	 */
	
	private Session session; // javax.websocket.Session
	private OcppAgent agent;
	
	// outgoing messages
	private boolean handlingMsg = false;
	
	private final Deque<OcppCommand> outQueue = new ConcurrentLinkedDeque<OcppCommand>();
	private OcppCommand waitingMsg = null;
	private long waitingTimestamp = Long.MAX_VALUE;
	
	// heartbeat and timeouts
	private long receivedTimestamp = Long.MIN_VALUE;
	private ScheduledFuture<?> controller = null;

	/*
	 * methods
	 */
	
	public static void putAgent( OcppAgent agent ) {
		agents.put( agent.getId(), agent );
	}

	public static OcppAgent getAgent( String id ) {
		return agents.get( id );
	}

	private void send( OcppMsg msg ) throws Exception {
		session.getBasicRemote().sendObject( msg  );
    }

	private void sendWaitingMsg() {
		try {
			waitingTimestamp = System.currentTimeMillis();
			send( waitingMsg.request );
			
		} catch( Exception e ) {
			Log.error( e );
			OcppMsg.CallError error = makeCallError( waitingMsg.request.messageId, OcppErrorEnum.Timeout );
			this.onMessage( error, session );

		}
	}
	
	private OcppCommand checkAnswerOrError( String messageId ) throws Exception {
		synchronized( outQueue ) {
			if( waitingMsg == null ) return null;
			if( !waitingMsg.request.messageId.equals( messageId ) ) return null;
			OcppCommand res = waitingMsg;
			waitingMsg = null;
			return res; 
			// the next element in the queue, if any, will be sent when finishedHandlingMsg is called.
		}
	}
	
	private void finishedHandlingMsg() {
		synchronized( outQueue ) {
			this.handlingMsg = false;
			
			if( waitingMsg != null ) return;
			waitingMsg = outQueue.pollFirst(); // next to sent
			if( waitingMsg == null ) return;
		}
		
		sendWaitingMsg();
	}
	
	private void sendAction( OcppAction action, Object payload, Consumer<OcppCommand> callback ) {
		
		OcppMsg.Call msg = new OcppMsg.Call();
		msg.messageId = UUID.randomUUID().toString(); 
		msg.action = action;
		msg.payload = payload;
		
		OcppCommand command = new OcppCommand( msg, callback );
		
		synchronized( outQueue ) {
			if( waitingMsg != null || handlingMsg ) {
				outQueue.addLast( command );
				return;
			}

			waitingMsg = command;
		}
		
		sendWaitingMsg();
    }
	
	public void sendAction( Object payload, Consumer<OcppCommand> callback ) {
		OcppAction action = OcppAction.fromRequestClass( payload.getClass() );
		if( action == null ) {
			Log.error( "Error: unkwown action class=%s", payload.getClass() );
			return;
		}
		
		sendAction( action, payload, callback );
	}

	public final void sendAction( Object payload ) {
		sendAction( payload, null );
	}
	
    /*
     * websocket overrides
     */
    
	@OnOpen
	public void onOpen( Session session,  EndpointConfig endpointConfig ) {
		try {
			if( this.controller == null ) {
				this.controller  = scheduler.scheduleAtFixedRate( new Controller(this), config.wsControllerInterval, config.wsControllerInterval, TimeUnit.SECONDS );
			}
			
			String csId  = session.getRequestParameterMap().get("csId").get(0);
			Log.debug( "OcppEndpoint.onOpen: csId=%s", csId );

			this.session = session;
			
			this.agent = agents.get( csId );
			if( this.agent == null ) {
				Log.debug( "OcppEndpoint.onOpen: unknown agentId = %s", csId );
				session.close();
				return;
			}
			this.agent.onOcppEndpointConnected( this );
			Log.debug( "OcppEndpoint.onOpen: done." );
		
		} catch( Exception e ) {
			Log.error( e );
			
		}
	}
    
	@OnMessage
	public void onMessage( OcppMsg msg, Session session ) {
		Log.debug( "OcppEndpoint.onMessage ..." );
		
		synchronized( this.outQueue ) {
			this.handlingMsg = true;
		}

		try {
			this.receivedTimestamp = System.currentTimeMillis();

			if( msg instanceof OcppMsg.Call ) {
				OcppMsg.Call tmsg = (OcppMsg.Call)msg;
				OcppMsg answer = handleCallMessage( tmsg );
				send( answer );

			} else if( msg instanceof OcppMsg.CallResult ) {
				OcppMsg.CallResult tmsg = (OcppMsg.CallResult)msg;
				handleCallResultMessage( tmsg );

			} else if( msg instanceof OcppMsg.CallError ) {
				OcppMsg.CallError tmsg = (OcppMsg.CallError)msg;
				handleCallErrorMessage( tmsg );

			} else { // should not happen
				throw new IllegalArgumentException();

			}

		} catch( Exception exc ) {
			Log.error(exc);

		} finally {
			finishedHandlingMsg();

		}
	}
    
	@OnError
    public void onError( Throwable exc ) {	
    	if( exc instanceof EOFException ) return;
    	Log.error(exc);
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason ) {
    	//    	Log.debug( "OcppEndpoint.onClose" );
		this.controller.cancel( true );
		this.controller = null;
    	this.session = null;
    	
    	if( this.agent != null ) {
    		this.agent.onOcppEndpointConnected( null );
    	}

    	synchronized( this.outQueue ) {
    		if( this.waitingMsg != null ) {
    			this.outQueue.addFirst(waitingMsg);
    			this.waitingMsg = null;
    		}
    		
    		if( this.agent != null ) {
    			for( OcppCommand msg : this.outQueue ) {
    				try {
    					this.agent.onOcppCallError( msg );
    				} catch (Exception exc) {
    					Log.error(exc);
    				}
    			}
    		}
    		
    		this.outQueue.clear();
    	}
    }
    
    /*
     * message handlers
     */
    
	private static CallError makeCallError( String messageId, OcppErrorItf error ) {
		CallError res = new CallError();
	    res.messageId = messageId;
	    res.errorCode = error.getErrorCode();
	    res.errorDescription = error.getErrorDescription();
	    res.errorDetails = error.getErrorDetails();
	    return res;
	}
    
    private OcppMsg handleCallMessage( OcppMsg.Call msg ) throws Exception {
    	Log.debug( "OcppEndpoint.onMessage: %s", msg.action );

    	try {
    		Object answer = agent.onOcppCall( msg.action, msg.payload );

    		if( answer instanceof OcppErrorItf ) {
    			
    			return makeCallError( msg.messageId, (OcppErrorItf)answer );

    		} 

			CallResult res = new CallResult();
		    res.messageId = msg.messageId;
		    res.payload = answer;
		    return res;

    	} catch( OcppException exc ) {
    		return exc.toCallError( msg.messageId );

    	} catch ( Exception e ) {
    		Log.error( e );
    		return makeCallError( msg.messageId, OcppErrorEnum.Internal );

    	}
    }

	private void handleCallResultMessage( OcppMsg.CallResult msg ) throws Exception {

		OcppCommand command = this.checkAnswerOrError( msg.messageId );
		if( command == null ) return; // old message
		
		msg.payload = command.request.action.decodeResponse( (JsonNode) msg.payload );
		
		command.answer = msg.payload;
		agent.onOcppCallResult( command );
		
		if( command.callback != null ) {
			command.callback.accept( command );
		}

	}

	private void handleCallErrorMessage( OcppMsg.CallError msg ) throws Exception {
		if( msg.errorCode == OcppErrorCode.Exception ) {
			// Error from deserializer
			msg.errorCode = msg.exceptionErrorCode;
			send( msg );
			return;
		}
	
		OcppCommand command = this.checkAnswerOrError(msg.messageId);
		if( command == null ) return; // old message
		
		command.error = msg;
		agent.onOcppCallResult( command );
		
		if( command.callback != null ) {
			command.callback.accept( command );
		}

	}
}
