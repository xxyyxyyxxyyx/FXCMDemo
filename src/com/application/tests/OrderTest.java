package com.application.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.application.demo.Account;
import com.application.demo.Offer;
import com.application.demo.Order;
import com.application.demo.ResponseListener;
import com.application.demo.SessionStatusListener;
import com.fxcore2.IO2GResponseListener;
import com.fxcore2.O2GAccountRow;
import com.fxcore2.O2GClosedTradeRow;
import com.fxcore2.O2GClosedTradesTable;
import com.fxcore2.O2GClosedTradesTableResponseReader;
import com.fxcore2.O2GOfferRow;
import com.fxcore2.O2GOffersTableResponseReader;
import com.fxcore2.O2GOrderResponseReader;
import com.fxcore2.O2GOrderRow;
import com.fxcore2.O2GOrdersTableResponseReader;
import com.fxcore2.O2GRequest;
import com.fxcore2.O2GRequestFactory;
import com.fxcore2.O2GResponse;
import com.fxcore2.O2GResponseReaderFactory;
import com.fxcore2.O2GSession;
import com.fxcore2.O2GTableType;
import com.fxcore2.O2GTradeRow;
import com.fxcore2.O2GTradesTableResponseReader;
import com.fxcore2.O2GTransport;

public class OrderTest {
	private static final String USERNAME = "D102913785";
	private static final String PASSWORD = "2647";
	private static O2GSession session;
	private static SessionStatusListener statusListener;
	private static IO2GResponseListener responseListener;
	private static ArrayList<O2GOfferRow> offers;
	private static Order order;
	private static O2GAccountRow account;

	@BeforeClass
	public static void setup() throws Exception {
		session = O2GTransport.createSession();
		statusListener = new SessionStatusListener();
		responseListener = new ResponseListener();
		session.subscribeSessionStatus(statusListener);
		session.subscribeResponse(responseListener);
		session.login(USERNAME, PASSWORD, "http://www.fxcorporate.com/Hosts.jsp", "Demo");
		while (!statusListener.isConnected()) {
			Thread.sleep(50);
		}
		ArrayList<String> instruments = new ArrayList();
		instruments.add("EUR/USD");
		instruments.add("GBP/USD");

		// Get offers for instruments
		offers = new Offer(session, responseListener).getOffers(instruments);
		
		order = new Order(session, responseListener);
		// Get the first Account row (Assuming user has only one account)
		account = new Account(session).getAccounts().get(0);

	}

	@Test
	public void testCreateOpenMarketOrder() throws Exception {
		// Get offer for "EUR/USD"
		O2GOfferRow offer = offers.get(0);
		// Create a buy market order for the above offer with Amount 1
		order.createOpenMarketOrder(account, offer, "b", 1);
		O2GResponse response = ((ResponseListener) responseListener).getResponse();
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);
		// Order id of the created order
		String orderID = responseReader.getOrderID();

		// Get snapshot of open positions
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.TRADES);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}
		response = ((ResponseListener) responseListener).getResponse();
		O2GTradesTableResponseReader tradesResponseReader = readerFactory.createTradesTableReader(response);
		boolean hasOrderID = false;
		// Check if the open positions contains the order created above
		for (int i = 0; i < tradesResponseReader.size(); i++) {
			O2GTradeRow row = tradesResponseReader.getRow(i);
			if (row.getOpenOrderID().equals(orderID)) {
				hasOrderID = true;
				break;
			}
		}
		assertTrue(hasOrderID);

	}

	@Test
	public void testCreateOpenMarketOrderBulk() throws Exception {
		/*
		 * Create buy market order for the two offers with Amount 1 and get
		 * their order ids
		 */
		ArrayList<String> orderIDs = order.createOpenMarketOrderBulk(account, offers, "b", 1);

		// Get snapshot of open positions
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.TRADES);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}
		O2GResponse response = ((ResponseListener) responseListener).getResponse();
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GTradesTableResponseReader tradesResponseReader = readerFactory.createTradesTableReader(response);
		boolean hasOrderIDs = false;

		// Check if the open positions contains the orders created above
		for (String orderID : orderIDs) {
			// Set hasOrderIds as false for each order
			hasOrderIDs = false;
			for (int i = 0; i < tradesResponseReader.size(); i++) {
				O2GTradeRow row = tradesResponseReader.getRow(i);
				if (row.getOpenOrderID().equals(orderID)) {
					hasOrderIDs = true;
					break;
				}
			}

		}
		assertTrue(hasOrderIDs);

	}

	@Test
	public void testCloseOpenMarketOrder() throws Exception {
		// Get all open positions
		ArrayList<O2GTradeRow> trades = order.getTrades();
		// Choosing to close first order from open positions
		order.closeOpenMarketOrder(account, trades.get(0));

		O2GResponse response = ((ResponseListener) responseListener).getResponse();
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);
		// Order id of the closed order
		String closedOrderID = responseReader.getOrderID();
		// Get snapshot of closed positions
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.CLOSED_TRADES);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}
		response = ((ResponseListener) responseListener).getResponse();
		readerFactory = session.getResponseReaderFactory();
		O2GClosedTradesTableResponseReader closedTradesResponseReader = readerFactory
				.createClosedTradesTableReader(response);
		boolean hasClosedOrderID = false;
		// Check if the closed positions contains the closed order id above
		for (int i = 0; i < closedTradesResponseReader.size(); i++) {
			O2GClosedTradeRow row = closedTradesResponseReader.getRow(i);
			if (row.getCloseOrderID().equals(closedOrderID)) {
				hasClosedOrderID = true;
				break;
			}
		}
		assertTrue(hasClosedOrderID);

	}
	
	@Test
	public void testGetTrades() throws Exception {
		// Get all open positions
		ArrayList<O2GTradeRow> trades = order.getTrades();
		if(trades.size() == 0){
			// Creating a bulk order
			order.createOpenMarketOrderBulk(account, offers, "b", 1);
			trades = order.getTrades();
		}
		// Check that a Trade row is returned
		assertTrue(trades.get(0) instanceof O2GTradeRow);
		ArrayList<String> offerIDs = new ArrayList<>();
		// Get all offer ids from offers list
		for(O2GOfferRow offer : offers){
			offerIDs.add(offer.getOfferID());
		}
		// Check that all the trades returned have the order ids from the offers list
		for (O2GTradeRow trade : trades) {
			if (!offerIDs.contains(trade.getOfferID())) {
				fail();
			}
		}
	}
	
	@Test
	public void testCloseOpenMarketOrderBulk() throws Exception {
		// Get all open positions
		ArrayList<O2GTradeRow> trades = order.getTrades();
		// Closing all open positions
		ArrayList<String> closedOrderIDs = order.closeOpenMarketOrderBulk(account, trades);
		// Get snapshot of closed positions
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.CLOSED_TRADES);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}
		O2GResponse response = ((ResponseListener) responseListener).getResponse();
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GClosedTradesTableResponseReader closedTradesResponseReader = readerFactory
				.createClosedTradesTableReader(response);
		boolean hasClosedOrderIDs = false;
		// Check if the closed positions contains all the closed order ids in
		// the ArrayList
		for (String closedOrderID : closedOrderIDs) {
			// Set hasClosedOrderIds as false for each closed order
			hasClosedOrderIDs = false;
			for (int i = 0; i < closedTradesResponseReader.size(); i++) {
				O2GClosedTradeRow row = closedTradesResponseReader.getRow(i);
				if (row.getCloseOrderID().equals(closedOrderID)) {
					hasClosedOrderIDs = true;
					break;
				}
			}

		}
		assertTrue(hasClosedOrderIDs);

	}
	
	

	@AfterClass
	public static void teardown() throws Exception {
		session.logout();
		while (!statusListener.isDisconnected()) {
			Thread.sleep(50);
		}
		session.unsubscribeSessionStatus(statusListener);
		session.unsubscribeResponse(responseListener);
		session.dispose();
	}
}
