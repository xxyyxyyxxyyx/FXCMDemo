package com.application.demo;
import java.util.ArrayList;
import java.util.Scanner;

import com.fxcore2.*;

public class Offer {
	private O2GSession session;
	private IO2GResponseListener responseListener;

	public Offer(O2GSession session, IO2GResponseListener responseListener) {
		this.session = session;
		this.responseListener = responseListener;
	}

	// Gets the list of all the offers associated with the given currency
	// symbols
	public ArrayList<O2GOfferRow> getOffers(ArrayList<String> instruments) {
		// Array for storing all the offer rows
		ArrayList<O2GOfferRow> offers = new ArrayList<O2GOfferRow>();
		
		O2GLoginRules loginRules = session.getLoginRules();
		O2GResponse response = loginRules.getTableRefreshResponse(O2GTableType.OFFERS);
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GOffersTableResponseReader offersResponseReader = readerFactory.createOffersTableReader(response);
		// Get the offer associated with each symbol
		for (String instrument : instruments) {
			for (int i = 0; i < offersResponseReader.size(); i++) {
				O2GOfferRow offer = offersResponseReader.getRow(i);
				if (offer.getInstrument().equals(instrument)) {
					offers.add(offer);
					break;
				}

			}
		}
		return offers;

	}

	// Updates the list of offers associated with the currency symbols
	public ArrayList<O2GOfferRow> updateOffers(ArrayList<String> instruments) throws Exception {
		// Array for storing offer rows
		ArrayList<O2GOfferRow> offers = new ArrayList<O2GOfferRow>();
		O2GRequestFactory requestFactory = session.getRequestFactory();
		O2GRequest request = requestFactory.createRefreshTableRequest(O2GTableType.OFFERS);
		((ResponseListener) responseListener).setRequestID(request.getRequestId());
		session.sendRequest(request);
		// Wait for response
		while (!((ResponseListener) responseListener).hasResponse()) {
			Thread.sleep(50);
		}
		O2GResponse response = ((ResponseListener) responseListener).getResponse();
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GOffersTableResponseReader offersResponseReader = readerFactory.createOffersTableReader(response);
		// Get the offer associated with each symbol
		for (String instrument : instruments) {
			for (int i = 0; i < offersResponseReader.size(); i++) {
				O2GOfferRow offer = offersResponseReader.getRow(i);
				if (offer.getInstrument().equals(instrument)) {
					offers.add(offer);
					break;
				}

			}

		}

		return offers;

	}

	// Prints the offers
	public void printOffers(ArrayList<O2GOfferRow> offers) {
		System.out.println("\nDealing Rates");
		System.out.println("------------------------------------------------------------------------------------");
		System.out.format("%-8s%-10s%-15s%-15s%-15s%-15s\n", "Index", "Symbol", "Sell", "Buy", "High", "Low");
		System.out.println("------------------------------------------------------------------------------------");
		int index = 1;
		for (O2GOfferRow offer : offers) {

			System.out.format("%-8d%-10s%-15.5f%-15.5f%-15.5f%-15.5f\n", index, offer.getInstrument(), offer.getBid(),
					offer.getAsk(), offer.getHigh(), offer.getLow());
			index++;
		}

	}
}
