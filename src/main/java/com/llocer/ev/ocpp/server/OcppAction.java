package com.llocer.ev.ocpp.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.llocer.ev.ocpp.msgs20.OcppAuthorizeRequest;
import com.llocer.ev.ocpp.msgs20.OcppAuthorizeResponse;
import com.llocer.ev.ocpp.msgs20.OcppBootNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppBootNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppClearedChargingLimitRequest;
import com.llocer.ev.ocpp.msgs20.OcppClearedChargingLimitResponse;
import com.llocer.ev.ocpp.msgs20.OcppFirmwareStatusNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppFirmwareStatusNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppGet15118EVCertificateRequest;
import com.llocer.ev.ocpp.msgs20.OcppGet15118EVCertificateResponse;
import com.llocer.ev.ocpp.msgs20.OcppGetCertificateStatusRequest;
import com.llocer.ev.ocpp.msgs20.OcppGetCertificateStatusResponse;
import com.llocer.ev.ocpp.msgs20.OcppGetVariablesRequest;
import com.llocer.ev.ocpp.msgs20.OcppGetVariablesResponse;
import com.llocer.ev.ocpp.msgs20.OcppHeartbeatRequest;
import com.llocer.ev.ocpp.msgs20.OcppHeartbeatResponse;
import com.llocer.ev.ocpp.msgs20.OcppLogStatusNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppLogStatusNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppMeterValuesRequest;
import com.llocer.ev.ocpp.msgs20.OcppMeterValuesResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyChargingLimitRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyChargingLimitResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyCustomerInformationRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyCustomerInformationResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyDisplayMessagesRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyDisplayMessagesResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEVChargingNeedsRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEVChargingNeedsResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEVChargingScheduleRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEVChargingScheduleResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEventRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyEventResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyMonitoringReportRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyMonitoringReportResponse;
import com.llocer.ev.ocpp.msgs20.OcppNotifyReportRequest;
import com.llocer.ev.ocpp.msgs20.OcppNotifyReportResponse;
import com.llocer.ev.ocpp.msgs20.OcppPublishFirmwareStatusNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppPublishFirmwareStatusNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppReportChargingProfilesRequest;
import com.llocer.ev.ocpp.msgs20.OcppReportChargingProfilesResponse;
import com.llocer.ev.ocpp.msgs20.OcppReservationStatusUpdateRequest;
import com.llocer.ev.ocpp.msgs20.OcppReservationStatusUpdateResponse;
import com.llocer.ev.ocpp.msgs20.OcppSecurityEventNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppSecurityEventNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppSetChargingProfileRequest;
import com.llocer.ev.ocpp.msgs20.OcppSetChargingProfileResponse;
import com.llocer.ev.ocpp.msgs20.OcppSignCertificateRequest;
import com.llocer.ev.ocpp.msgs20.OcppSignCertificateResponse;
import com.llocer.ev.ocpp.msgs20.OcppStatusNotificationRequest;
import com.llocer.ev.ocpp.msgs20.OcppStatusNotificationResponse;
import com.llocer.ev.ocpp.msgs20.OcppTransactionEventRequest;
import com.llocer.ev.ocpp.msgs20.OcppTransactionEventResponse;

public enum OcppAction {
	GetVariables( OcppGetVariablesRequest.class, OcppGetVariablesResponse.class ),
	Heartbeat( OcppHeartbeatRequest.class, OcppHeartbeatResponse.class ),
	StatusNotification( OcppStatusNotificationRequest.class, OcppStatusNotificationResponse.class ),
	TransactionEvent( OcppTransactionEventRequest.class, OcppTransactionEventResponse.class ),
	Authorize( OcppAuthorizeRequest.class, OcppAuthorizeResponse.class ),
	SetChargingProfile( OcppSetChargingProfileRequest.class, OcppSetChargingProfileResponse.class ), 
	SecurityEventNotification( OcppSecurityEventNotificationRequest.class, OcppSecurityEventNotificationResponse.class ), 
	SignCertificate( OcppSignCertificateRequest.class, OcppSignCertificateResponse.class ), 
	BootNotification( OcppBootNotificationRequest.class, OcppBootNotificationResponse.class ), 
	NotifyReport( OcppNotifyReportRequest.class, OcppNotifyReportResponse.class ), 
	ReservationStatusUpdate( OcppReservationStatusUpdateRequest.class, OcppReservationStatusUpdateResponse.class ), 
	MeterValues( OcppMeterValuesRequest.class, OcppMeterValuesResponse.class ), 
	ReportChargingProfiles( OcppReportChargingProfilesRequest.class, OcppReportChargingProfilesResponse.class ), 
	NotifyChargingLimit( OcppNotifyChargingLimitRequest.class, OcppNotifyChargingLimitResponse.class ), 
	ClearedChargingLimit( OcppClearedChargingLimitRequest.class, OcppClearedChargingLimitResponse.class ), 
	NotifyEVChargingNeeds( OcppNotifyEVChargingNeedsRequest.class, OcppNotifyEVChargingNeedsResponse.class ), 
	NotifyEVChargingSchedule( OcppNotifyEVChargingScheduleRequest.class, OcppNotifyEVChargingScheduleResponse.class ), 
	FirmwareStatusNotification( OcppFirmwareStatusNotificationRequest.class, OcppFirmwareStatusNotificationResponse.class ), 
	PublishFirmwareStatusNotification( OcppPublishFirmwareStatusNotificationRequest.class, OcppPublishFirmwareStatusNotificationResponse.class ), 
	Get15118EVCertificate( OcppGet15118EVCertificateRequest.class, OcppGet15118EVCertificateResponse.class ), 
	GetCertificateStatus( OcppGetCertificateStatusRequest.class, OcppGetCertificateStatusResponse.class ), 
	LogStatusNotification( OcppLogStatusNotificationRequest.class, OcppLogStatusNotificationResponse.class ), 
	NotifyMonitoringReport( OcppNotifyMonitoringReportRequest.class, OcppNotifyMonitoringReportResponse.class ), 
	NotifyEvent( OcppNotifyEventRequest.class, OcppNotifyEventResponse.class ), 
	NotifyCustomerInformation( OcppNotifyCustomerInformationRequest.class, OcppNotifyCustomerInformationResponse.class ), 
	NotifyDisplayMessages( OcppNotifyDisplayMessagesRequest.class, OcppNotifyDisplayMessagesResponse.class );
	
    static Map<Class<?>, OcppAction> initRequestMap() {
	    final Map<Class<?>, OcppAction> res = new HashMap<Class<?>, OcppAction>();
        for(final OcppAction action : OcppAction.values()) {
        	res.put( action.requestClass, action );
        }
       return Collections.unmodifiableMap(res);
    }
    private static final Map<Class<?>, OcppAction> requestMap = initRequestMap();
    
	private final Class<?> requestClass;
	private final Class<?> responseClass;
		
	OcppAction( Class<?> requestClass, Class<?> responseClass ) {
		this.requestClass = requestClass;
		this.responseClass = responseClass;
	}
	
	static OcppAction fromRequestClass(Class<?> cl) {
        return requestMap.get(cl);
    }
	
	public Object decodeRequest( JsonNode payload ) throws Exception {
		if( requestClass == null || payload == null ) return payload;
		return OcppMsgMapper.mapper.treeToValue( payload, requestClass );
	}
	
	public Object decodeResponse( JsonNode payload ) throws Exception {
		if( responseClass == null || payload == null ) return payload;
		return OcppMsgMapper.mapper.treeToValue( payload, responseClass );
	}
}
