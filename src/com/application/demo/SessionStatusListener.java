package com.application.demo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.fxcore2.*;

public class SessionStatusListener implements IO2GSessionStatus {
	private boolean connected;
	private boolean disconnected;
	private boolean error;
	
	public SessionStatusListener(){
		connected = false;
		disconnected = false;
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	public boolean isDisconnected(){
		return disconnected;
	}
	
	public boolean hasError(){
		return error;
	}
	@Override
	public void onLoginFailed(String error) {
		System.out.println(" Login error: "+error);
		this.error = true;
	}

	@Override
	public void onSessionStatusChanged(O2GSessionStatusCode status) {
      System.out.println("Status: " + status.toString());
    switch (status) {
    // Updating static variables according to the status
    case CONNECTED:
        connected = true;
        disconnected = false;
        break;
    case DISCONNECTED:
        connected = false;
        disconnected = true;
        break;
    }
    }
}
