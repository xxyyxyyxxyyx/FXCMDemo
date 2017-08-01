package com.application.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.application.demo.Account;
import com.application.demo.ResponseListener;
import com.application.demo.SessionStatusListener;
import com.fxcore2.*;

public class AccountTest {
	private static final String USERNAME = "D102913785";
	private static final String PASSWORD = "2647";
	private static final String ACCOUNT_ID = "2903556";
	private static O2GSession session;
	private static SessionStatusListener statusListener;

	@BeforeClass
	public static void setup() throws Exception {
		session = O2GTransport.createSession();
		statusListener = new SessionStatusListener();
		session.subscribeSessionStatus(statusListener);
		session.login(USERNAME, PASSWORD, "http://www.fxcorporate.com/Hosts.jsp", "Demo");
		while (!statusListener.isConnected()) {
			Thread.sleep(50);
		}
	}

	@Test
	public void testGetAccounts() throws Exception {

		Account account = new Account(session);
		ArrayList<O2GAccountRow> accounts = account.getAccounts();
		// Check if Account row is returned
		assertTrue(accounts.get(0) instanceof O2GAccountRow);
		// Check if the correct account row is returned
		assertEquals(ACCOUNT_ID, accounts.get(0).getAccountID());

	}
	
	@AfterClass
	public static void teardown() throws Exception{
		session.logout();
		while (!statusListener.isDisconnected()) {
			Thread.sleep(50);
		}
		session.unsubscribeSessionStatus(statusListener);
		session.dispose();
	}
	

}
