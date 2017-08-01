package com.application.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.application.demo.Offer;
import com.application.demo.ResponseListener;
import com.application.demo.SessionStatusListener;
import com.fxcore2.IO2GResponseListener;
import com.fxcore2.O2GAccountRow;
import com.fxcore2.O2GOfferRow;
import com.fxcore2.O2GSession;
import com.fxcore2.O2GTransport;

public class OfferTest {
	private static final String USERNAME = "D102913785";
	private static final String PASSWORD = "2647";
	private static O2GSession session;
	private static SessionStatusListener statusListener;
	private static IO2GResponseListener responseListener;
	private static Offer offer;

	@BeforeClass
	public static void setup() throws Exception {
		session = O2GTransport.createSession();
		statusListener = new SessionStatusListener();
		responseListener = new ResponseListener();
		session.subscribeSessionStatus(statusListener);
		session.subscribeSessionStatus(statusListener);
		session.login(USERNAME, PASSWORD, "http://www.fxcorporate.com/Hosts.jsp", "Demo");
		while (!statusListener.isConnected()) {
			Thread.sleep(50);
		}
		offer = new Offer(session, responseListener);
	}
	
	@Test
	public void testGetOffers(){
		ArrayList<String> instruments = new ArrayList();
		instruments.add("EUR/USD");
		ArrayList<O2GOfferRow> offers = offer.getOffers(instruments);
		// Check if an Offer row is returned
		assertTrue(offers.get(0) instanceof O2GOfferRow);
		// Check if the offer is for the instrument sent
		assertEquals("EUR/USD",offers.get(0).getInstrument());
	}
	
	@Test
	public void testUpdateOffers(){
		ArrayList<String> instruments = new ArrayList();
		instruments.add("EUR/USD");
		ArrayList<O2GOfferRow> offers = offer.getOffers(instruments);
		// Check that an Offer row is returned
		assertTrue(offers.get(0) instanceof O2GOfferRow);
		// Check that the returned offer corresponds to the instrument/symbol sent
		assertEquals("EUR/USD",offers.get(0).getInstrument());
	}
	
	@AfterClass
	public static void teardown() throws Exception{
		session.logout();
		while (!statusListener.isDisconnected()) {
			Thread.sleep(50);
		}
		session.unsubscribeSessionStatus(statusListener);
		session.unsubscribeResponse(responseListener);
		session.dispose();
	}

}
