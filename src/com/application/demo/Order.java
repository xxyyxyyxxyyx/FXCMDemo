package com.application.demo;

import java.util.ArrayList;

import com.fxcore2.*;

public class Order {
	private O2GSession session;
	private IO2GResponseListener responseListener;

	public Order(O2GSession session, IO2GResponseListener responseListener) {
		this.session = session;
		this.responseListener = responseListener;
	}

	// Creates a single Open Market Order
	public void createOpenMarketOrder(O2GAccountRow account, O2GOfferRow offer, String buyOrSell, int amount)
			throws Exception {
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE, Constants.Orders.TrueMarketOpen);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID, account.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, offer.getOfferID());
		if (buyOrSell.equals("b"))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
		else if (buyOrSell.equals("s"))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, account.getBaseUnitSize() * amount);
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}

	}

	// Creates multiple Open Market Orders and return orderIds of all the
	// created orders. OrderIds needed for use in Unit testing the function
	public ArrayList<String> createOpenMarketOrderBulk(O2GAccountRow account, ArrayList<O2GOfferRow> offers,
			String buyOrSell, int amount) throws Exception {
		ArrayList<String> orderIDs = new ArrayList<>();
		for (O2GOfferRow offer : offers) {
			createOpenMarketOrder(account, offer, buyOrSell, amount);
			// Read the response
			O2GResponse response = ((ResponseListener) responseListener).getResponse();
			O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
			O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);
			// Add orderId of the order to ArrayList
			orderIDs.add(responseReader.getOrderID());
		}

		return orderIDs;

	}

	// Closes a single Open Market Order
	public void closeOpenMarketOrder(O2GAccountRow account, O2GTradeRow trade) throws Exception {
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GValueMap valuemap = requestFactory.createValueMap();
		valuemap.setString(O2GRequestParamsEnum.COMMAND, Constants.Commands.CreateOrder);
		valuemap.setString(O2GRequestParamsEnum.ORDER_TYPE, Constants.Orders.TrueMarketClose);
		valuemap.setString(O2GRequestParamsEnum.ACCOUNT_ID, account.getAccountID());
		valuemap.setString(O2GRequestParamsEnum.OFFER_ID, trade.getOfferID());
		valuemap.setString(O2GRequestParamsEnum.TRADE_ID, trade.getTradeID());
		if (trade.getBuySell().equals(Constants.Buy))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Sell);
		if (trade.getBuySell().equals(Constants.Sell))
			valuemap.setString(O2GRequestParamsEnum.BUY_SELL, Constants.Buy);
		valuemap.setInt(O2GRequestParamsEnum.AMOUNT, trade.getAmount());
		O2GRequest request = requestFactory.createOrderRequest(valuemap);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}

	}

	// Closes multiple Open Market Orders. ClosedOrderIds needed for Unit testing
	public ArrayList<String> closeOpenMarketOrderBulk(O2GAccountRow account, ArrayList<O2GTradeRow> trades) throws Exception {
		ArrayList<String> closedOrderIDs = new ArrayList<>();
		for (O2GTradeRow trade : trades) {
			closeOpenMarketOrder(account, trade);
			// Read the response
			O2GResponse response = ((ResponseListener) responseListener).getResponse();
			O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
			O2GOrderResponseReader responseReader = readerFactory.createOrderResponseReader(response);
			// Add  order Id of the close order to ArrayList
			closedOrderIDs.add(responseReader.getOrderID());
		}
		return closedOrderIDs;
	}

	// Gets a list of all Open Positions
	public ArrayList<O2GTradeRow> getTrades() throws Exception {
		// List for storing all the open positions
		ArrayList<O2GTradeRow> trades = new ArrayList<>();
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
		for (int i = 0; i < tradesResponseReader.size(); i++) {
			trades.add(tradesResponseReader.getRow(i));
		}
		return trades;
	}

	// Prints Open Positions
	public void printOrders(ArrayList<O2GTradeRow> trades, ArrayList<O2GOfferRow> offers) {
		System.out.println("\nOpen Positions");
		System.out.println(
				"----------------------------------------------------------------------------------------------");
		System.out.format("%-8s%-10s%-15s%-15s%-10s%-5s%-15s%-15s\n", "Index", "Account", "Symbol", "Usd Mr", "Amount",
				"S/B", "Open", "Close");
		System.out.println(
				"----------------------------------------------------------------------------------------------");
		// Index for each of the open positions
		int index = 1;
		for (O2GTradeRow trade : trades) {
			// Get the offer associated with the trade for current Buy or Sell
			// price and instrument symbol
			O2GOfferRow offer = findOffer(offers, trade.getOfferID());
			System.out.format("%-8d%-10s%-15s%-15.2f%-10d%-5s%-15.5f%-15.5f\n", index, trade.getAccountID(),
					offer.getInstrument(), trade.getUsedMargin(), trade.getAmount() / 1000, trade.getBuySell(),
					trade.getOpenRate(), trade.getBuySell().equals(Constants.Buy) ? offer.getBid() : offer.getAsk());
			index++;
		}
	}

	// Helper function that gets the offer associated with the given offerID
	private O2GOfferRow findOffer(ArrayList<O2GOfferRow> offers, String offerID) {
		for (O2GOfferRow offer : offers) {
			if (offer.getOfferID().equals(offerID)) {
				return offer;
			}
		}
		return null;
	}
}
