package com.application.demo;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.fxcore2.O2GAccountRow;
import com.fxcore2.O2GLoginRules;
import com.fxcore2.O2GOfferRow;
import com.fxcore2.O2GOffersTableResponseReader;
import com.fxcore2.O2GRequest;
import com.fxcore2.O2GRequestFactory;
import com.fxcore2.O2GResponse;
import com.fxcore2.O2GResponseReaderFactory;
import com.fxcore2.O2GSession;
import com.fxcore2.O2GTableType;
import com.fxcore2.O2GTradeRow;
import com.fxcore2.O2GTransport;

public class Demo {

	public static void main(String[] args) {
		O2GSession session = null;

		try {
			System.out.println("---------- Welcome to FXCM Demo ----------------\nPlease enter your login details\n");
			System.out.print("Username: ");
			Scanner in = new Scanner(System.in);
			String userName = in.nextLine();
			System.out.print("Password: ");
			String password = in.nextLine();
			System.out.print("Choose connection type, press 'd' for demo or 'r' for real: ");
			String connection = in.nextLine().toLowerCase();
			if (!connection.equals("d")) {
				if (!connection.equals("r")) {
					throw new Exception("Exception: Invalid connection type");
				}
			}
			// Create Session
			session = O2GTransport.createSession();
			SessionStatusListener statusListener = new SessionStatusListener();
			ResponseListener responseListener = new ResponseListener();

			// Subscribe
			session.subscribeResponse(responseListener);
			session.subscribeSessionStatus(statusListener);

			// Login
			session.login(userName, password, "http://www.fxcorporate.com/Hosts.jsp", connection.equals("d")?"Demo":"Real");
			while (!statusListener.isConnected() && !statusListener.hasError()) {
				Thread.sleep(50);
			}

			// Create Offer, Account and Order instance
			Offer offer = new Offer(session, responseListener);
			Account account = new Account(session);
			Order order = new Order(session, responseListener);

			// Adding currency symbols for the application
			ArrayList<String> instruments = new ArrayList();
			instruments.add("EUR/USD");
			instruments.add("GBP/USD");
			instruments.add("USD/JPY");

			// List of all the offers
			ArrayList<O2GOfferRow> offers = offer.getOffers(instruments);

			// List of all the associated accounts
			ArrayList<O2GAccountRow> accounts = account.getAccounts();

			// List of all open trades
			ArrayList<O2GTradeRow> trades = order.getTrades();

			// Assuming the user has only one account
			O2GAccountRow currentAccount = accounts.get(0);

			boolean exitLoop = false;
			while (!exitLoop) {
				// Print Offers and open positions
				offer.printOffers(offers);
				order.printOrders(trades, offers);

				System.out.print(
						"\nPress 'o' to open order, 'c' to close order, 'r' to refresh dealing rates and open positions, 'q' to quit the demo: ");
				String menu = in.nextLine().toLowerCase();
				switch (menu) {
				// Open order
				case "o":
					System.out.print(
							"----- Choose dealing rates 'index' to create Market Order or press '0' to open all orders: ");
					int inputOpenOrder = Integer.parseInt(in.nextLine());
					if (inputOpenOrder < 0 || inputOpenOrder > offers.size()) {
						throw new Exception("Exception: Invalid dealing rates index");
					}
					System.out.print("----- Press 'b' to BUY and 's' to SELL: ");
					String buyOrSell = in.nextLine().toLowerCase();
					if (!buyOrSell.equals("b")) {
						if (!buyOrSell.equals("s")) {
							throw new Exception("Exception: Invalid buy/sell input");
						}
					}
					System.out.print("----- Input AMOUNT to " + (buyOrSell.equals("b") ? "BUY" : "SELL") + " : ");
					int amount = Integer.parseInt(in.nextLine());
					if (amount <= 0) {
						throw new Exception("Exception: Amount cannot be less than 1");
					}
					// Create order
					System.out.println("\nStatus: CREATING MARKET ORDER");
					if (inputOpenOrder == 0) {
						order.createOpenMarketOrderBulk(currentAccount, offers, buyOrSell, amount);
					} else {
						int index = inputOpenOrder;
						order.createOpenMarketOrder(currentAccount, offers.get(index - 1), buyOrSell, amount);
					}
					// Update for changes
					offers = offer.updateOffers(instruments);
					trades = order.getTrades();
					break;
				// Close order
				case "c":
					System.out.print(
							"----- Choose open position 'index' to close Market Order or press '0' to close all orders: ");
					int inputCloseOrder = Integer.parseInt(in.nextLine());
					if (inputCloseOrder < 0 || inputCloseOrder > trades.size()) {
						throw new Exception("Exception: Invalid open position index");
					}
					// Close orders
					System.out.println("\nStatus: CLOSING MARKET ORDER");
					if (inputCloseOrder == 0) {
						order.closeOpenMarketOrderBulk(currentAccount, trades);
					} else {
						int index = inputCloseOrder;
						order.closeOpenMarketOrder(currentAccount, trades.get(index - 1));
					}
					// Update for changes
					offers = offer.updateOffers(instruments);
					trades = order.getTrades();
					break;
				// Refresh dealing rates and open positions
				case "r":
					System.out.println("\nStatus: REFRESHING DEALING RATES AND OPEN POSITIONS");
					offers = offer.updateOffers(instruments);
					trades = order.getTrades();
					break;
				// Quit application
				case "q":
					session.logout();
					while (!statusListener.isDisconnected()) {
						Thread.sleep(50);
					}
					session.unsubscribeSessionStatus(statusListener);
					session.unsubscribeResponse(responseListener);
	                session.dispose();
					System.out.println("Goodbye!");
					exitLoop = true;
					break;
				default:
					System.out.println("\nError: Invalid Input");
					break;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			System.exit(0);
		}
	}
}
