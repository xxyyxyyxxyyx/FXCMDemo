package com.application.demo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.fxcore2.*;

public class ResponseListener implements IO2GResponseListener {
	private String requestID;
	private O2GResponse response;

	public boolean hasResponse() {
		return response != null;
	}

	public void setRequestID(String requestID) {
		response = null;
		this.requestID = requestID;
	}

	public O2GResponse getResponse() {
		return response;
	}

	public void onRequestCompleted(String requestID, O2GResponse response) {
		if (this.requestID.equals(response.getRequestId())) {
			this.response = response;
		}
	}

	public void onRequestFailed(String sRequestID, String sError) {
		System.out.println("Request failed: " + sError);
	}

	public void onTablesUpdates(O2GResponse response) {
		// No implementation on table update. Since it is a console application, prefer require manual refresh instead
		
	}

}
